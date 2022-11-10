/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator.emadlgen;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.math._symboltable.MathStatementsSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.emadl._cocos.EMADLCocos;
import de.monticore.lang.monticar.emadl.generator.backend.Backend;
import de.monticore.lang.monticar.emadl.generator.modularcnn.ComposedNetworkHandler;
import de.monticore.lang.monticar.generator.EMAMGenerator;
import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.MathCommandRegister;
import de.monticore.lang.monticar.generator.cmake.CMakeConfig;
import de.monticore.lang.monticar.generator.cpp.*;
import de.monticore.lang.monticar.generator.cpp.converter.TypeConverter;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperFactory;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperStandaloneApi;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.Splitters;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

public class EMADLGenerator implements EMAMGenerator {

    private boolean generateCMake = false;
    private CMakeConfig cMakeConfig = new CMakeConfig("");
    private GeneratorCPP emamGen;

    private GeneratorPythonWrapperStandaloneApi pythonWrapper;
    private Backend backend;
    private EMADLFileHandler emadlFileHandler;
    private EMADLTagging emadlTaggingHandler;
    private EMADLCNNHandler emadlCNNHandler;

    private String composedNetworkFilePath = "";



    private boolean useDgl = false;
    private Map<String, ArchitectureSymbol> processedArchitecture;


    public EMADLGenerator(Backend backend, String composedNetworkFilePath) {
        this.backend = backend;
        this.composedNetworkFilePath = composedNetworkFilePath;
        emamGen = new GeneratorCPP();
        emamGen.useArmadilloBackend();
        emamGen.setGenerationTargetPath("./target/generated-sources-emadl/");
        GeneratorPythonWrapperFactory pythonWrapperFactory = new GeneratorPythonWrapperFactory();
        pythonWrapper = new GeneratorPythonWrapperStandaloneApi();
        emadlFileHandler = new EMADLFileHandler(this, composedNetworkFilePath);
        emadlTaggingHandler = new EMADLTagging(this);
        emadlCNNHandler = new EMADLCNNHandler(this, processedArchitecture, pythonWrapper, this.composedNetworkFilePath);
    }

    protected Map<String, ArchitectureSymbol> getProcessedArchitecture() {
        return this.processedArchitecture;
    }

    public EMADLFileHandler getEmadlFileHandler() {
        return emadlFileHandler;
    }

    protected EMADLTagging getEmadlTaggingHandler() {
        return emadlTaggingHandler;
    }

    protected boolean getUseDgl() { return useDgl; }

    protected void setUseDgl(boolean useDgl){
        this.useDgl = useDgl;
    }

    protected Backend getBackend() {return this.backend;}

    protected String getComposedNetworkFilePath(){
        return this.composedNetworkFilePath;
    }

    protected GeneratorCPP getEmamGen() {
        return emamGen;
    }


    public String getGenerationTargetPath() {
        return getEmamGen().getGenerationTargetPath();
    }

    public void setGenerationTargetPath(String generationTargetPath){
        if (!(generationTargetPath.substring(generationTargetPath.length() - 1).equals("/"))){
            getEmamGen().setGenerationTargetPath(generationTargetPath + "/");
        }
        else {
            getEmamGen().setGenerationTargetPath(generationTargetPath);
        }
    }

    //TODO: CNN parsing/generation
    public void generate(String modelPath, String qualifiedName, String pythonPath, String forced, boolean doCompile, String useDgl) throws IOException, TemplateException {
        Log.info("Generator start", "GENERATION");
        processedArchitecture = new HashMap<>();
        emadlFileHandler.setModelsPath( modelPath );
        emadlFileHandler.setPythonPath(pythonPath);
        setUseDgl(useDgl.equals("y"));

        //TODO: Check Tagging
        TaggingResolver symtab = emadlTaggingHandler.getSymTabAndTaggingResolver();

        //TODO: Check component resolving
        EMAComponentInstanceSymbol instance = resolveComponentInstanceSymbol(qualifiedName, symtab);
        try {
            // copy the AdaNet files to
            emadlFileHandler.copyPythonFilesFromResource("AdaNet");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //TODO: Check file generation
        emadlFileHandler.generateFiles(symtab, instance, pythonPath, forced);

        if (doCompile) {
            if (!generateCMake) // do it either way
                emadlFileHandler.generateCMakeFiles(instance);
            //TODO: Check compilation
            compile();
        }
        processedArchitecture = null;
        Log.info("Geneator end", "GENERATION");
    }


    //TODO: Check here how component resolution works
    public EMAComponentInstanceSymbol resolveComponentInstanceSymbol(String qualifiedName, TaggingResolver symtab) {
        String simpleName = Names.getSimpleName(qualifiedName);
        Log.info("ResolveSimpleName: " + simpleName, "RESOLVER");
        if (!Character.isUpperCase(simpleName.charAt(0))) {
            String packageName = qualifiedName.substring(0, qualifiedName.length() - simpleName.length() - 1);
            qualifiedName = Names.getQualifiedName(packageName, StringUtils.capitalize(simpleName));
        }

        //TODO: Update resolution point of Networks
        EMAComponentSymbol component = symtab.<EMAComponentSymbol>resolve(qualifiedName, EMAComponentSymbol.KIND).orElse(null);

        //Resolution done here
        List<String> splitName = Splitters.DOT.splitToList(qualifiedName);
        String componentName = splitName.get(splitName.size() - 1);
        String instanceName = componentName.substring(0, 1).toLowerCase() + componentName.substring(1);

        if (component == null){
            String errMsg = "Component with name '" + componentName + "' does not exist.";
            Log.error(errMsg);
            throw new RuntimeException(errMsg);
        }

        Scope c1 = component.getEnclosingScope();
        Optional<EMAComponentInstanceSymbol> c2 = c1.<EMAComponentInstanceSymbol>resolve(instanceName, EMAComponentInstanceSymbol.KIND);
        EMAComponentInstanceSymbol c3 = c2.get();
        return c3;
    }

    protected void compile() throws IOException {
        File tempScript = emadlFileHandler.createTempScript();
        try {
            ProcessBuilder pb;
            if (!SystemUtils.IS_OS_WINDOWS)
                pb = new ProcessBuilder("bash", tempScript.toString());
            else
                pb = new ProcessBuilder("cmd", tempScript.toString());
            pb.inheritIO();
            Process process = pb.start();
            int returnCode = process.waitFor();
            if(returnCode != 0) {
                String errMsg = "During compilation, an error occured. See above for more details.";
                Log.error(errMsg);
                throw new RuntimeException(errMsg);
            }
        }catch(Exception e){
            String errMsg ="During compilation, the following error occured: '" + e.toString() + "'";
            Log.error(errMsg);
            throw new RuntimeException(errMsg);
        } finally {
            tempScript.delete();
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    protected boolean isAlreadyTrained(String trainingHash, EMAComponentInstanceSymbol componentInstance) {
        try {
            EMAComponentSymbol component = componentInstance.getComponentType().getReferencedSymbol();
            String componentConfigFilename = component.getFullName().replaceAll("\\.", "/");

            String checkFilePathString = getGenerationTargetPath() + componentConfigFilename + ".training_hash";
            Path checkFilePath = Paths.get(checkFilePathString);
            if (Files.exists(checkFilePath)) {
                List<String> hashes = Files.readAllLines(checkFilePath);
                for (String hash : hashes) {
                    if (hash.equals(trainingHash)) {
                        return true;
                    }
                }
            }

            return false;
        }
        catch(Exception e) {
            return false;
        }
    }

    protected List<FileContent> generateStrings(TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol, Set<EMAComponentInstanceSymbol> allInstances, String forced) {
        if (componentInstanceSymbol != null) {
            getCmakeConfig().getCMakeListsViewModel().setCompName(componentInstanceSymbol.getFullName().replace('.', '_').replace('[', '_').replace(']', '_'));
        }

        List<FileContent> fileContents = new ArrayList<>();
        // Add Helpers
        if (emamGen.usesArmadilloBackend()) {
            fileContents.add(ArmadilloHelper.getArmadilloHelperFileContent());
        }
        emamGen.searchForCVEverywhere(componentInstanceSymbol, taggingResolver);
        if (ConversionHelper.isUsedCV()) {
            fileContents.add(ConversionHelper.getConversionHelperFileContent(emamGen.isGenerateTests()));
        }

        processedArchitecture = new HashMap<>();
        generateComponent(fileContents, allInstances, taggingResolver, componentInstanceSymbol);

        String instanceName = componentInstanceSymbol.getComponentType().getFullName().replaceAll("\\.", "_");
        Log.info("Instance name: " + instanceName,"GENERATE_STRINGS");
        fileContents.addAll(emadlCNNHandler.generateCNNTrainer(allInstances, instanceName));
        TypesGeneratorCPP tg = new TypesGeneratorCPP();
        fileContents.addAll(tg.generateTypes(TypeConverter.getTypeSymbols()));

//        if (cnnArchGenerator.isCMakeRequired()) {
//            cnnArchGenerator.setGenerationTargetPath(getGenerationTargetPath());
//            Map<String, String> cmakeContentsMap = cnnArchGenerator.generateCMakeContent(componentInstanceSymbol.getFullName());
//            for (String fileName : cmakeContentsMap.keySet()){
//                fileContents.add(new FileContent(cmakeContentsMap.get(fileName), fileName));
//            }
//        }

        if (emamGen.shouldGenerateMainClass()) {
            //fileContents.add(emamGen.getMainClassFileContent(componentInstanceSymbol, fileContents.get(0)));
        } else if (emamGen.shouldGenerateSimulatorInterface()) {
            fileContents.addAll(SimulatorIntegrationHelper.getSimulatorIntegrationHelperFileContent());
        }

        emadlFileHandler.fixArmadilloImports(fileContents);

        processedArchitecture = null;
        return fileContents;
    }

    protected void generateComponent(List<FileContent> fileContents,
                                     Set<EMAComponentInstanceSymbol> allInstances,
                                     TaggingResolver taggingResolver,
                                     EMAComponentInstanceSymbol componentInstanceSymbol) {
        emamGen.addSemantics(taggingResolver, componentInstanceSymbol);

        allInstances.add(componentInstanceSymbol);
        EMAComponentSymbol emaComponentSymbol = componentInstanceSymbol.getComponentType().getReferencedSymbol();

        /* remove the following two lines if the component symbol full name bug with generic variables is fixed */
        emaComponentSymbol.setFullName(null);
        emaComponentSymbol.getFullName();
        /* */

        //TODO: fix composed Net Null pointer problem here
        ComposedNetworkHandler composedNetworkHandler = new ComposedNetworkHandler(this.composedNetworkFilePath);
        if(composedNetworkHandler.isComposedNet(componentInstanceSymbol)) composedNetworkHandler.refreshInformation(allInstances);
        //composedNetworkHandler.processComponentInstances(allInstances);

        //Optional<ArchitectureSymbol> architecture = componentInstanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
        //if(composedNetworkHandler.isComposedNet(componentInstanceSymbol)) composedNetworkHandler.refreshInformation(allInstances);
        Optional<ArchitectureSymbol> architecture = composedNetworkHandler.resolveArchitectureSymbolOfInstance(componentInstanceSymbol);


        // set the path to AdaNet python files
        architecture.ifPresent(architectureSymbol -> {architectureSymbol.setAdaNetUtils(emadlFileHandler.getAdaNetUtils());});
        Optional<MathStatementsSymbol> mathStatements = emaComponentSymbol.getSpannedScope().resolve("MathStatements", MathStatementsSymbol.KIND);

        EMADLCocos.checkAll(componentInstanceSymbol);



        if (architecture.isPresent()) {

            emadlCNNHandler.getCnnArchGenerator().check(architecture.get());
            String dPath = emadlFileHandler.getDataPath(taggingResolver, emaComponentSymbol, componentInstanceSymbol);
            String wPath = emadlFileHandler.getWeightsPath(emaComponentSymbol, componentInstanceSymbol);
            HashMap layerPathParameterTags = emadlTaggingHandler.getLayerPathParameterTags(taggingResolver, emaComponentSymbol, componentInstanceSymbol);
            layerPathParameterTags.putAll(emadlTaggingHandler.getLayerArtifactParameterTags(taggingResolver, emaComponentSymbol, componentInstanceSymbol));
            architecture.get().setDataPath(dPath);
            architecture.get().setWeightsPath(wPath);
            architecture.get().processLayerPathParameterTags(layerPathParameterTags);
            architecture.get().setComponentName(emaComponentSymbol.getFullName());
            architecture.get().setUseDgl(getUseDgl());
            if(!emadlFileHandler.getCustomFilesPath().equals("")) {
                architecture.get().setCustomPyFilesPath(emadlFileHandler.getCustomFilesPath() + "python/" + Backend.getBackendString(this.backend).toLowerCase());
            }
            emadlCNNHandler.generateCNN(fileContents, taggingResolver, componentInstanceSymbol, architecture.get());
            if (processedArchitecture != null) {
                processedArchitecture.put(architecture.get().getComponentName(), architecture.get());
            }
        } /*else if (composedNetworkHandler.isComposedNet(emaComponentSymbol.getName())) {

            String dPath = emadlFileHandler.getDataPath(taggingResolver, emaComponentSymbol, componentInstanceSymbol);
            String wPath = emadlFileHandler.getWeightsPath(emaComponentSymbol, componentInstanceSymbol);
            HashMap layerPathParameterTags = emadlTaggingHandler.getLayerPathParameterTags(taggingResolver, emaComponentSymbol, componentInstanceSymbol);
            layerPathParameterTags.putAll(emadlTaggingHandler.getLayerArtifactParameterTags(taggingResolver, emaComponentSymbol, componentInstanceSymbol));

        }*/
        else if (mathStatements.isPresent()){
            generateMathComponent(fileContents, taggingResolver, componentInstanceSymbol, mathStatements.get());
        }
        else {
            generateSubComponents(fileContents, allInstances, taggingResolver, componentInstanceSymbol);
        }
    }





    protected String transformComponent(String component, String predictorClassName, String applyBeamSearchMethod, String executeMethod, ArchitectureSymbol architecture) {
        //insert includes
        component = component.replaceFirst("using namespace",
                "#include \"" + predictorClassName + ".h" + "\"\n" +
                        "#include \"CNNTranslator.h\"\n" +
                        "using namespace");

        //insert network attribute for predictor of each network
        String networkAttributes = "public:";

        int i = 0;
        for (NetworkInstructionSymbol networkInstruction : architecture.getNetworkInstructions()) {
            networkAttributes += "\n" + predictorClassName + "_" + i + " _predictor_" + i + "_;";

            ++i;
        }

        component = component.replaceFirst("public:", networkAttributes);

        //insert BeamSearch method
        //component = component.replaceFirst("void init\\(\\)", applyBeamSearchMethod + "\nvoid init()");

        //insert execute method
        component = component.replaceFirst("void execute\\(\\)\\s\\{\\s\\}",
                "void execute(){\n" + executeMethod + "\n}");
        return component;
    }

    protected void generateMathComponent(List<FileContent> fileContents, TaggingResolver taggingResolver, EMAComponentInstanceSymbol EMAComponentSymbol, MathStatementsSymbol mathStatementsSymbol) {
        fileContents.add(new FileContent(
                emamGen.generateString(taggingResolver, EMAComponentSymbol, mathStatementsSymbol),
                EMAComponentSymbol));
    }

    protected void generateSubComponents(List<FileContent> fileContents, Set<EMAComponentInstanceSymbol> allInstances, TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol) {
        fileContents.add(new FileContent(emamGen.generateString(taggingResolver, componentInstanceSymbol, (MathStatementsSymbol) null), componentInstanceSymbol));
        String lastNameWithoutArrayPart = "";
        for (EMAComponentInstanceSymbol instanceSymbol : componentInstanceSymbol.getSubComponents()) {
            int arrayBracketIndex = instanceSymbol.getName().indexOf("[");
            boolean generateComponentInstance = true;
            if (arrayBracketIndex != -1) {
                generateComponentInstance = !instanceSymbol.getName().substring(0, arrayBracketIndex).equals(lastNameWithoutArrayPart);
                lastNameWithoutArrayPart = instanceSymbol.getName().substring(0, arrayBracketIndex);
                Log.info(lastNameWithoutArrayPart, "Without:");
                Log.info(generateComponentInstance + "", "Bool:");
            }
            if (generateComponentInstance) {
                generateComponent(fileContents, allInstances, taggingResolver, instanceSymbol);
            }
        }
    }



    protected void stopGeneratorIfWarning() {
        for (int i = 0; i < Log.getFindings().size(); i++) {
            if (Log.getFindings().get(i).toString().matches("Filepath '(.)*' does not exist!")) {
                throw new RuntimeException(Log.getFindings().get(i).toString());
            } else if (Log.getFindings().get(i).toString()
                    .equals("DatapathType is incorrect, must be of Type: HDF5 or LMDB")) {
                throw new RuntimeException(Log.getFindings().get(i).toString());
            }
        }
    }

    @Override
    public List<FileContent> generateStrings(TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol) {
        return this.generateStrings(taggingResolver, componentInstanceSymbol, new HashSet<>(), "UNSET");
    }

    @Override
    public List<File> generateFiles(TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol) throws IOException {
        return this.emadlFileHandler.generateFiles(taggingResolver, componentInstanceSymbol, "", "UNSET");
    }

    @Override
    public CMakeConfig getCmakeConfig() {
        mergeCMakeConfigs();
        return cMakeConfig;
    }

    private void mergeCMakeConfigs() {
        emamGen.getCmakeConfig().getCMakeListsViewModel().getCmakeCommandList()
                .stream().forEach(s -> cMakeConfig.addCMakeCommand(s));
        emamGen.getCmakeConfig().getCMakeListsViewModel().getCmakeCommandListEnd()
                .stream().forEach(s -> cMakeConfig.addCMakeCommandEnd(s));
        emamGen.getCmakeConfig().getCMakeListsViewModel().getCmakeLibraryLinkageList()
                .stream().forEach(s -> cMakeConfig.addCmakeLibraryLinkage(s));
        emamGen.getCmakeConfig().getCMakeListsViewModel().getModuleDependencies()
                .stream().forEach(s -> cMakeConfig.addModuleDependency(s));

        emadlCNNHandler.getCnnArchGenerator().getCmakeConfig().getCMakeListsViewModel().getCmakeCommandList()
                .stream().forEach(s -> cMakeConfig.addCMakeCommand(s));
        emadlCNNHandler.getCnnArchGenerator().getCmakeConfig().getCMakeListsViewModel().getCmakeCommandListEnd()
                .stream().forEach(s -> cMakeConfig.addCMakeCommandEnd(s));
        emadlCNNHandler.getCnnArchGenerator().getCmakeConfig().getCMakeListsViewModel().getCmakeLibraryLinkageList()
                .stream().forEach(s -> cMakeConfig.addCmakeLibraryLinkage(s));
        emadlCNNHandler.getCnnArchGenerator().getCmakeConfig().getCMakeListsViewModel().getModuleDependencies()
                .stream().forEach(s -> cMakeConfig.addModuleDependency(s));
    }

    @Override
    public boolean isGenerateCMake() {
        return generateCMake;
    }

    @Override
    public void setGenerateCMake(boolean b) {
        generateCMake = b;
    }

    @Override
    public boolean useAlgebraicOptimizations() {
        return emamGen.useAlgebraicOptimizations();
    }

    @Override
    public void setUseAlgebraicOptimizations(boolean b) {
        emamGen.setUseAlgebraicOptimizations(b);
    }

    @Override
    public boolean useThreadingOptimizations() {
        return emamGen.useThreadingOptimizations();
    }

    @Override
    public void setUseThreadingOptimization(boolean b) {
        emamGen.setUseThreadingOptimization(b);
    }

    @Override
    public MathCommandRegister getMathCommandRegister() {
        return emamGen.getMathCommandRegister();
    }

    @Override
    public void setMathCommandRegister(MathCommandRegister mathCommandRegister) {
        emamGen.setMathCommandRegister(mathCommandRegister);
    }
}