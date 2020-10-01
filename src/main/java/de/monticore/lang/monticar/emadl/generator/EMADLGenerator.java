/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstantiationSymbol;
import de.monticore.lang.math._symboltable.MathStatementsSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.cnnarch.generator.CNNArchGenerator;
import de.monticore.lang.monticar.cnnarch.generator.CNNTrainGenerator;
import de.monticore.lang.monticar.cnnarch.generator.DataPathConfigParser;
import de.monticore.lang.monticar.cnnarch.generator.WeightsPathConfigParser;
import de.monticore.lang.monticar.cnnarch.gluongenerator.CNNTrain2Gluon;
import de.monticore.lang.monticar.cnnarch.gluongenerator.annotations.ArchitectureAdapter;
import de.monticore.lang.monticar.cnnarch.gluongenerator.preprocessing.PreprocessingComponentParameterAdapter;
import de.monticore.lang.monticar.cnnarch.gluongenerator.preprocessing.PreprocessingPortChecker;
import de.monticore.lang.monticar.cnntrain._cocos.CNNTrainCocos;
import de.monticore.lang.monticar.cnntrain._symboltable.ConfigurationSymbol;
import de.monticore.lang.monticar.cnntrain._symboltable.LearningMethod;
import de.monticore.lang.monticar.cnntrain._symboltable.PreprocessingComponentSymbol;
import de.monticore.lang.monticar.emadl._cocos.DataPathCocos;
import de.monticore.lang.monticar.emadl._cocos.EMADLCocos;
import de.monticore.lang.monticar.emadl.tagging.dltag.DataPathSymbol;
import de.monticore.lang.monticar.generator.EMAMGenerator;
import de.monticore.lang.monticar.emadl.tagging.dltag.LayerPathParameterSymbol;
import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.MathCommandRegister;
import de.monticore.lang.monticar.generator.cmake.CMakeConfig;
import de.monticore.lang.monticar.generator.cmake.CMakeFindModule;
import de.monticore.lang.monticar.generator.cpp.ArmadilloHelper;
import de.monticore.lang.monticar.generator.cpp.GeneratorCPP;
import de.monticore.lang.monticar.generator.cpp.SimulatorIntegrationHelper;
import de.monticore.lang.monticar.generator.cpp.TypesGeneratorCPP;
import de.monticore.lang.monticar.generator.cpp.converter.TypeConverter;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperFactory;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperStandaloneApi;
import de.monticore.lang.monticar.generator.pythonwrapper.symbolservices.data.ComponentPortInformation;
import de.monticore.lang.tagging._symboltable.TagSymbol;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.Splitters;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import java.io.File;
import java.util.stream.Collectors;

public class EMADLGenerator implements EMAMGenerator {

    private boolean generateCMake = true;
    private CMakeConfig cMakeConfig = new CMakeConfig("");
    private GeneratorCPP emamGen;
    private CNNArchGenerator cnnArchGenerator;
    private CNNTrainGenerator cnnTrainGenerator;
    private GeneratorPythonWrapperStandaloneApi pythonWrapper;
    private Backend backend;

    private String modelsPath;

    private Map<String, ArchitectureSymbol> processedArchitecture;

    public EMADLGenerator(Backend backend) {
        this.backend = backend;
        emamGen = new GeneratorCPP();
        emamGen.useArmadilloBackend();
        emamGen.setGenerationTargetPath("./target/generated-sources-emadl/");
        GeneratorPythonWrapperFactory pythonWrapperFactory = new GeneratorPythonWrapperFactory();
        pythonWrapper = new GeneratorPythonWrapperStandaloneApi();
        cnnArchGenerator = backend.getCNNArchGenerator();
        cnnTrainGenerator = backend.getCNNTrainGenerator();
    }

    public String getModelsPath() {
        return modelsPath;
    }

    public void setModelsPath(String modelsPath) {
        if (!(modelsPath.substring(modelsPath.length() - 1).equals("/"))){
            this.modelsPath = modelsPath + "/";
        }
        else {
            this.modelsPath = modelsPath;
        }
    }

    public void setGenerationTargetPath(String generationTargetPath){
        if (!(generationTargetPath.substring(generationTargetPath.length() - 1).equals("/"))){
            getEmamGen().setGenerationTargetPath(generationTargetPath + "/");
        }
        else {
            getEmamGen().setGenerationTargetPath(generationTargetPath);
        }
    }

    public String getGenerationTargetPath(){
        return getEmamGen().getGenerationTargetPath();
    }

    public GeneratorCPP getEmamGen() {
        return emamGen;
    }

    public void generate(String modelPath, String qualifiedName, String pythonPath, String forced, boolean doCompile) throws IOException, TemplateException {
        processedArchitecture = new HashMap<>();
        setModelsPath( modelPath );
        TaggingResolver symtab = EMADLAbstractSymtab.createSymTabAndTaggingResolver(getModelsPath());
        EMAComponentInstanceSymbol instance = resolveComponentInstanceSymbol(qualifiedName, symtab);


        generateFiles(symtab, instance, pythonPath, forced);

        if (doCompile) {
            compile();
        }
        processedArchitecture = null;
    }

    private EMAComponentInstanceSymbol resolveComponentInstanceSymbol(String qualifiedName, TaggingResolver symtab) {
        EMAComponentSymbol component = symtab.<EMAComponentSymbol>resolve(qualifiedName, EMAComponentSymbol.KIND).orElse(null);

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

    public void compile() throws IOException {
        File tempScript = createTempScript();
        try {
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
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

    public File createTempScript() throws IOException{
        File tempScript = File.createTempFile("script", null);
        try{
            Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
                tempScript));
            PrintWriter printWriter = new PrintWriter(streamWriter);

            printWriter.println("#!/bin/bash");
            printWriter.println("cd " + getGenerationTargetPath());
            printWriter.println("mkdir -p build");
            printWriter.println("cd build");
            printWriter.println("cmake ..");
            printWriter.println("make");

            printWriter.close();
        }catch(Exception e){
            System.out.println(e);
        }

        return tempScript;
    }

    public String getChecksumForFile(String filePath) throws IOException {
        Path wiki_path = Paths.get(filePath);
        MessageDigest md5;

        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(Files.readAllBytes(wiki_path));
            byte[] digest = md5.digest();

            return DatatypeConverter.printHexBinary(digest).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "No_Such_Algorithm_Exception";
        }
    }

    public String getChecksumForLargerFile(String filePath) throws IOException {
        try {
            return (new File(filePath)).lastModified() + "";
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception_calculating_hash_large_file";
        }
    }

    public List<File> generateFiles(TaggingResolver taggingResolver, EMAComponentInstanceSymbol EMAComponentSymbol, String pythonPath, String forced) throws IOException {
        Set<EMAComponentInstanceSymbol> allInstances = new HashSet<>();
        List<FileContent> fileContents = generateStrings(taggingResolver, EMAComponentSymbol, allInstances, forced);
        List<File> generatedFiles = new ArrayList<>();

        for (FileContent fileContent : fileContents) {
            generatedFiles.add(emamGen.generateFile(fileContent));
        }

        // train
        Map<String, String> fileContentMap = new HashMap<>();
        for(FileContent f : fileContents) {
            fileContentMap.put(f.getFileName(), f.getFileContent());
        }

        List<FileContent> fileContentsTrainingHashes = new ArrayList<>();
        List<String> newHashes = new ArrayList<>();
        for (EMAComponentInstanceSymbol componentInstance : allInstances) {
            Optional<ArchitectureSymbol> architecture = componentInstance.getSpannedScope().resolve("", ArchitectureSymbol.KIND);

            if(!architecture.isPresent()) {
                continue;
            }

            if(forced.equals("n")) {
                continue;
            }

            String configFilename = getConfigFilename(componentInstance.getComponentType().getFullName(), componentInstance.getFullName(), componentInstance.getName());
            String emadlPath = getModelsPath() + configFilename + ".emadl";
            String cnntPath = getModelsPath() + configFilename + ".cnnt";

            String emadlHash = getChecksumForFile(emadlPath);
            String cnntHash = getChecksumForFile(cnntPath);

            String componentConfigFilename = componentInstance.getComponentType().getReferencedSymbol().getFullName().replaceAll("\\.", "/");

            String b = backend.getBackendString(backend);
            String trainingDataHash = "";
            String testDataHash = "";

            if (architecture.get().getDataPath() != null) {
                if (b.equals("CAFFE2")) {
                    trainingDataHash = getChecksumForLargerFile(architecture.get().getDataPath() + "/train_lmdb/data.mdb");
                    testDataHash = getChecksumForLargerFile(architecture.get().getDataPath() + "/test_lmdb/data.mdb");
            	}else{
                    trainingDataHash = getChecksumForLargerFile(architecture.get().getDataPath() + "/train.h5");
                    testDataHash = getChecksumForLargerFile(architecture.get().getDataPath() + "/test.h5");
            	}
			}
            String trainingHash = emadlHash + "#" + cnntHash + "#" + trainingDataHash + "#" + testDataHash;

            boolean alreadyTrained = newHashes.contains(trainingHash) || isAlreadyTrained(trainingHash, componentInstance);
            if(alreadyTrained && !forced.equals("y")) {
                Log.warn("Training of model " + componentInstance.getFullName() + " skipped");
            }
            else {
                String parsedFullName = componentInstance.getFullName().substring(0, 1).toLowerCase() + componentInstance.getFullName().substring(1).replaceAll("\\.", "_");
                String trainerScriptName = "CNNTrainer_" + parsedFullName + ".py";
                String trainingPath = getGenerationTargetPath() + trainerScriptName;
                if(Files.exists(Paths.get(trainingPath))){
                    ProcessBuilder pb = new ProcessBuilder(Arrays.asList(pythonPath, trainingPath)).inheritIO();
                    Process p = pb.start();

                    int exitCode = 0;
                    try {
                        exitCode = p.waitFor();
                    }
                    catch(InterruptedException e) {
                        String errMsg = "Training aborted: exit code " + Integer.toString(exitCode);

                        Log.error(errMsg);
                        throw new RuntimeException(errMsg);
                    }

                    if(exitCode != 0) {
                        String errMsg = "Training failed: exit code " + Integer.toString(exitCode);

                        Log.error(errMsg);
                        throw new RuntimeException(errMsg);
                    }

                    fileContentsTrainingHashes.add(new FileContent(trainingHash, componentConfigFilename + ".training_hash"));
                    newHashes.add(trainingHash);
                }
                else{
                    System.out.println("Trainingfile " + trainingPath + " not found.");
                }
            }

        }

        for (FileContent fileContent : fileContentsTrainingHashes) {
            generatedFiles.add(emamGen.generateFile(fileContent));
        }

        if (isGenerateCMake())
            generateCMakeFiles(EMAComponentSymbol);

        return generatedFiles;
    }

    protected List<File> generateCMakeFiles(EMAComponentInstanceSymbol componentInstanceSymbol) {
        List<File> files = new ArrayList<>();
        if(componentInstanceSymbol != null) {
            getCmakeConfig().getCMakeListsViewModel().setCompName(componentInstanceSymbol.getFullName().replace('.', '_').replace('[', '_').replace(']', '_'));
        }
        List<FileContent> contents = getCmakeConfig().generateCMakeFiles();
        try {
            for (FileContent content : contents)
                files.add(generateFile(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    public File generateFile(FileContent fileContent) throws IOException {
        File f = new File(getGenerationTargetPath() + fileContent.getFileName());
        Log.info(f.getName(), "FileCreation:");
        boolean contentEqual = false;
        //Actually slower than just saving and overwriting the file
        /*if (f.exists()) {
            String storedFileContent = new String(Files.readAllBytes(f.toPath()));
            if (storedFileContent.equals(fileContent.getFileContent())) {
                contentEqual = true;
            }
        } else*/
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            if (!f.createNewFile()) {
                Log.error("File could not be created");
            }
        }

        if (!contentEqual) {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f));
            bufferedWriter.write(fileContent.getFileContent(), 0, fileContent.getFileContent().length());
            bufferedWriter.close();
        }
        return f;
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    private boolean isAlreadyTrained(String trainingHash, EMAComponentInstanceSymbol componentInstance) {
        try {
            EMAComponentSymbol component = componentInstance.getComponentType().getReferencedSymbol();
            String componentConfigFilename = component.getFullName().replaceAll("\\.", "/");

            String checkFilePathString = getGenerationTargetPath() + componentConfigFilename + ".training_hash";
            Path checkFilePath = Paths.get( checkFilePathString);
            if(Files.exists(checkFilePath)) {
                List<String> hashes = Files.readAllLines(checkFilePath);
                for(String hash : hashes) {
                    if(hash.equals(trainingHash)) {
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

    public List<FileContent> generateStrings(TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol, Set<EMAComponentInstanceSymbol> allInstances, String forced){
        if(componentInstanceSymbol != null) {
            getCmakeConfig().getCMakeListsViewModel().setCompName(componentInstanceSymbol.getFullName().replace('.', '_').replace('[', '_').replace(']', '_'));
        }

        List<FileContent> fileContents = new ArrayList<>();
        processedArchitecture = new HashMap<>();

        generateComponent(fileContents, allInstances, taggingResolver, componentInstanceSymbol);

        String instanceName = componentInstanceSymbol.getComponentType().getFullName().replaceAll("\\.", "_");
        fileContents.addAll(generateCNNTrainer(allInstances, instanceName));

        fileContents.add(ArmadilloHelper.getArmadilloHelperFileContent());
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

        fixArmadilloImports(fileContents);

        processedArchitecture = null;
        return fileContents;
    }

    protected String getDataPath(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance){
        List<TagSymbol> instanceTags = new LinkedList<>();

        boolean isChildComponent = instance.getEnclosingComponent().isPresent();

        if (isChildComponent) {
            // get all instantiated components of parent
            List<EMAComponentInstantiationSymbol> instantiationSymbols = (List<EMAComponentInstantiationSymbol>) instance
                    .getEnclosingComponent().get().getComponentType().getReferencedSymbol().getSubComponents();

            // filter corresponding instantiation of instance and add tags
            instantiationSymbols.stream().filter(e -> e.getName().equals(instance.getName())).findFirst()
                    .ifPresent(symbol -> instanceTags.addAll(taggingResolver.getTags(symbol, DataPathSymbol.KIND)));
        }

        // instance tags have priority
        List<TagSymbol> tags = !instanceTags.isEmpty() ? instanceTags
                : (List<TagSymbol>) taggingResolver.getTags(component, DataPathSymbol.KIND);

        String dataPath;

        if (!tags.isEmpty()) {
            DataPathSymbol dataPathSymbol = (DataPathSymbol) tags.get(0);
            DataPathCocos.check(dataPathSymbol);

            dataPath = dataPathSymbol.getPath();

            // TODO: Replace warinings with errors, until then use this method
            stopGeneratorIfWarning();
            Log.warn("Tagging info for DataPath symbol was found, ignoring data_paths.txt: " + dataPath);
        }
        else {
            Path dataPathDefinition = Paths.get(getModelsPath(), "data_paths.txt");
            if (dataPathDefinition.toFile().exists()) {
                DataPathConfigParser newParserConfig = new DataPathConfigParser(getModelsPath() + "data_paths.txt");
                dataPath = newParserConfig.getDataPath(component.getFullName());
            } else {
                Log.warn("No data path definition found in " + dataPathDefinition + " found: "
                        + "Set data path to default ./data path");
                dataPath = "data";
            }
        }

        return dataPath;
    }

    protected String getWeightsPath(EMAComponentSymbol component, EMAComponentInstanceSymbol instance){
        String weightsPath;

        Path weightsPathDefinition = Paths.get(getModelsPath(), "weights_paths.txt");
        if (weightsPathDefinition.toFile().exists()) {
            WeightsPathConfigParser newParserConfig = new WeightsPathConfigParser(getModelsPath() + "weights_paths.txt");
            weightsPath = newParserConfig.getWeightsPath(component.getFullName());
        } else {
            Log.info("No weights path definition found in " + weightsPathDefinition + ": "
                    + "No pretrained weights will be loaded.", "EMADLGenerator");
            weightsPath = null;
        }
        return weightsPath;
    }

    protected HashMap getLayerPathParameterTags(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance){
        List<TagSymbol> instanceTags = new LinkedList<>();

        boolean isChildComponent = instance.getEnclosingComponent().isPresent();

        if (isChildComponent) {
            // get all instantiated components of parent
            List<EMAComponentInstantiationSymbol> instantiationSymbols = (List<EMAComponentInstantiationSymbol>) instance
                    .getEnclosingComponent().get().getComponentType().getReferencedSymbol().getSubComponents();

            // filter corresponding instantiation of instance and add tags
            instantiationSymbols.stream().filter(e -> e.getName().equals(instance.getName())).findFirst()
                    .ifPresent(symbol -> instanceTags.addAll(taggingResolver.getTags(symbol, LayerPathParameterSymbol.KIND)));
        }

        List<TagSymbol> tags = !instanceTags.isEmpty() ? instanceTags
                : (List<TagSymbol>) taggingResolver.getTags(component, LayerPathParameterSymbol.KIND);

        HashMap layerPathParameterTags = new HashMap();
        if (!tags.isEmpty()) {
            for(TagSymbol tag: tags) {
                LayerPathParameterSymbol layerPathParameterSymbol = (LayerPathParameterSymbol) tag;
                layerPathParameterTags.put(layerPathParameterSymbol.getId(), layerPathParameterSymbol.getPath());
            }
            // TODO: Replace warinings with errors, until then use this method
            stopGeneratorIfWarning();
            Log.warn("Tagging info for LayerPathParameter symbols was found.");
        }
        return layerPathParameterTags;
    }

    protected void generateComponent(List<FileContent> fileContents,
                                     Set<EMAComponentInstanceSymbol> allInstances,
                                     TaggingResolver taggingResolver,
                                     EMAComponentInstanceSymbol componentInstanceSymbol){
        allInstances.add(componentInstanceSymbol);
        EMAComponentSymbol EMAComponentSymbol = componentInstanceSymbol.getComponentType().getReferencedSymbol();

        /* remove the following two lines if the component symbol full name bug with generic variables is fixed */
        EMAComponentSymbol.setFullName(null);
        EMAComponentSymbol.getFullName();
        /* */

        Optional<ArchitectureSymbol> architecture = componentInstanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
        Optional<MathStatementsSymbol> mathStatements = EMAComponentSymbol.getSpannedScope().resolve("MathStatements", MathStatementsSymbol.KIND);

        EMADLCocos.checkAll(componentInstanceSymbol);

        if (architecture.isPresent()){
            cnnArchGenerator.check(architecture.get());
            String dPath = getDataPath(taggingResolver, EMAComponentSymbol, componentInstanceSymbol);
            String wPath = getWeightsPath(EMAComponentSymbol, componentInstanceSymbol);
            HashMap layerPathParameterTags = getLayerPathParameterTags(taggingResolver, EMAComponentSymbol, componentInstanceSymbol);
            architecture.get().setDataPath(dPath);
            architecture.get().setWeightsPath(wPath);
            architecture.get().processLayerPathParameterTags(layerPathParameterTags);
            architecture.get().setComponentName(EMAComponentSymbol.getFullName());
            generateCNN(fileContents, taggingResolver, componentInstanceSymbol, architecture.get());
            if (processedArchitecture != null) {
                processedArchitecture.put(architecture.get().getComponentName(), architecture.get());
            }
        }
        else if (mathStatements.isPresent()){
            generateMathComponent(fileContents, taggingResolver, componentInstanceSymbol, mathStatements.get());
        }
        else {
            generateSubComponents(fileContents, allInstances, taggingResolver, componentInstanceSymbol);
        }
    }

    private void fixArmadilloImports(List<FileContent> fileContents){
        for (FileContent fileContent : fileContents){
            fileContent.setFileContent(fileContent.getFileContent()
                    .replaceFirst("#include \"armadillo.h\"",
                            "#include \"armadillo\""));
        }
    }

    public void generateCNN(List<FileContent> fileContents, TaggingResolver taggingResolver, EMAComponentInstanceSymbol instance, ArchitectureSymbol architecture){
        List<FileContent> contents = cnnArchGenerator.generateStrings(taggingResolver, architecture);
        String fullName = instance.getFullName().replaceAll("\\.", "_");

        //get the components execute method
        String executeKey = "execute_" + fullName;
        List<String> executeMethods = getContentOf(contents, executeKey);
        if (executeMethods.size() != 1){
            throw new IllegalStateException("execute method of " + fullName + " not found");
        }
        String executeMethod = executeMethods.get(0);
        contents.remove(executeKey);

        List<String> applyBeamSearchMethods = getContentOf(contents, "BeamSearch_" + fullName);
        String applyBeamSearchMethod = null;
        if (applyBeamSearchMethods.size() == 1){
            applyBeamSearchMethod = applyBeamSearchMethods.get(0);
        }

        String component = emamGen.generateString(taggingResolver, instance, (MathStatementsSymbol) null);
        FileContent componentFileContent = new FileContent(
                transformComponent(component, "CNNPredictor_" + fullName,
                        applyBeamSearchMethod,
                        executeMethod,
                        architecture),
                instance);

        fileContents.addAll(contents);
        fileContents.add(componentFileContent);
        fileContents.add(new FileContent(readResource("CNNTranslator.h", Charsets.UTF_8), "CNNTranslator.h"));
    }

    private List<String> getContentOf(Collection<FileContent> fileContents, String fileName) {
        return fileContents
                .stream()
                .filter(fc -> fc.getFileName().equals(fileName))
                .map(FileContent::getFileContent)
                .collect(Collectors.toList());
    }

    protected String transformComponent(String component, String predictorClassName, String applyBeamSearchMethod, String executeMethod, ArchitectureSymbol architecture){
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

    public void generateMathComponent(List<FileContent> fileContents, TaggingResolver taggingResolver, EMAComponentInstanceSymbol EMAComponentSymbol, MathStatementsSymbol mathStatementsSymbol){
        fileContents.add(new FileContent(
                emamGen.generateString(taggingResolver, EMAComponentSymbol, mathStatementsSymbol),
                EMAComponentSymbol));
    }

    public void generateSubComponents(List<FileContent> fileContents, Set<EMAComponentInstanceSymbol> allInstances, TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol){
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

    private String getConfigFilename(String mainComponentName, String componentFullName, String componentName) {
        String trainConfigFilename;
        String mainComponentConfigFilename = mainComponentName.replaceAll("\\.", "/");
        String componentConfigFilename = componentFullName.replaceAll("\\.", "/");
        String instanceConfigFilename = componentFullName.replaceAll("\\.", "/") + "_"  + componentName;
        if (Files.exists(Paths.get( getModelsPath() + instanceConfigFilename + ".cnnt"))) {
            trainConfigFilename = instanceConfigFilename;
        }
        else if (Files.exists(Paths.get( getModelsPath() + componentConfigFilename + ".cnnt"))){
            trainConfigFilename = componentConfigFilename;
        }
        else if (Files.exists(Paths.get( getModelsPath() + mainComponentConfigFilename + ".cnnt"))){
            trainConfigFilename = mainComponentConfigFilename;
        }
        else{
            Log.error("Missing configuration file. " +
                    "Could not find a file with any of the following names (only one needed): '"
                    + getModelsPath() + instanceConfigFilename + ".cnnt', '"
                    + getModelsPath() + componentConfigFilename + ".cnnt', '"
                    + getModelsPath() + mainComponentConfigFilename + ".cnnt'." +
                    " These files denote respectively the configuration for the single instance, the component or the whole system.");
            return null;
        }
        return trainConfigFilename;
    }

    public List<FileContent> generateCNNTrainer(Set<EMAComponentInstanceSymbol> allInstances, String mainComponentName) {
        List<FileContent> fileContents = new ArrayList<>();
        for (EMAComponentInstanceSymbol componentInstance : allInstances) {
            EMAComponentSymbol component = componentInstance.getComponentType().getReferencedSymbol();
            Optional<ArchitectureSymbol> architecture = component.getSpannedScope().resolve("", ArchitectureSymbol.KIND);

            if (architecture.isPresent()) {
                String trainConfigFilename = getConfigFilename(mainComponentName, component.getFullName(), component.getName());

                //should be removed when CNNTrain supports packages
                cnnTrainGenerator.setGenerationTargetPath(getGenerationTargetPath());
                if (cnnTrainGenerator instanceof CNNTrain2Gluon) {
                    ((CNNTrain2Gluon) cnnTrainGenerator).setRootProjectModelsDir(getModelsPath());
                }
                List<String> names = Splitter.on("/").splitToList(trainConfigFilename);
                trainConfigFilename = names.get(names.size()-1);
                Path modelPath = Paths.get(getModelsPath() + Joiner.on("/").join(names.subList(0,names.size()-1)));
                ConfigurationSymbol configuration = cnnTrainGenerator.getConfigurationSymbol(modelPath, trainConfigFilename);

                // Annotate train configuration with architecture
                final String fullConfigName = String.join(".", names);
                ArchitectureSymbol correspondingArchitecture = this.processedArchitecture.get(fullConfigName);
                assert correspondingArchitecture != null : "No architecture found for train " + fullConfigName + " configuration!";
                configuration.setTrainedArchitecture(
                        new ArchitectureAdapter(correspondingArchitecture.getName(), correspondingArchitecture));
                CNNTrainCocos.checkTrainedArchitectureCoCos(configuration);

                // Resolve critic network if critic is present
                if (configuration.getCriticName().isPresent()) {
                    String fullCriticName = configuration.getCriticName().get();
                    int indexOfFirstNameCharacter = fullCriticName.lastIndexOf('.') + 1;
                    fullCriticName = fullCriticName.substring(0, indexOfFirstNameCharacter)
                            + fullCriticName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullCriticName.substring(indexOfFirstNameCharacter + 1);

                    TaggingResolver symtab = EMADLAbstractSymtab.createSymTabAndTaggingResolver(getModelsPath());
                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(fullCriticName, symtab);
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> critic = instanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                    if (!critic.isPresent()) {
                        String errMsg = "During the resolving of critic component: Critic component "
                                + fullCriticName + " does not have a CNN implementation but is required to have one";

                        Log.error(errMsg);
                        throw new RuntimeException(errMsg);
                    }
                    critic.get().setComponentName(fullCriticName);
                    configuration.setCriticNetwork(new ArchitectureAdapter(fullCriticName, critic.get()));
                    CNNTrainCocos.checkCriticCocos(configuration);
                }

                // Resolve discriminator network if discriminator is present
                if (configuration.getDiscriminatorName().isPresent()) {
                    String fullDiscriminatorName = configuration.getDiscriminatorName().get();
                    int indexOfFirstNameCharacter = fullDiscriminatorName.lastIndexOf('.') + 1;
                    fullDiscriminatorName = fullDiscriminatorName.substring(0, indexOfFirstNameCharacter)
                            + fullDiscriminatorName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullDiscriminatorName.substring(indexOfFirstNameCharacter + 1);

                    TaggingResolver symtab = EMADLAbstractSymtab.createSymTabAndTaggingResolver(getModelsPath());
                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(fullDiscriminatorName, symtab);
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> discriminator = instanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                    if (!discriminator.isPresent()) {
                        String errMsg ="During the resolving of critic component: Critic component "
                                + fullDiscriminatorName + " does not have a CNN implementation but is required to have one";
                        Log.error(errMsg);
                        throw new RuntimeException(errMsg);
                    }
                    discriminator.get().setComponentName(fullDiscriminatorName);
                    configuration.setDiscriminatorNetwork(new ArchitectureAdapter(fullDiscriminatorName, discriminator.get()));
                }

                // Resolve QNetwork if present
                if (configuration.getQNetworkName().isPresent()) {
                    String fullQNetworkName = configuration.getQNetworkName().get();
                    int indexOfFirstNameCharacter = fullQNetworkName.lastIndexOf('.') + 1;
                    fullQNetworkName = fullQNetworkName.substring(0, indexOfFirstNameCharacter)
                            + fullQNetworkName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullQNetworkName.substring(indexOfFirstNameCharacter + 1);

                    TaggingResolver symtab = EMADLAbstractSymtab.createSymTabAndTaggingResolver(getModelsPath());
                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(fullQNetworkName, symtab);
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> qnetwork = instanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                    if (!qnetwork.isPresent()) {
                        String errMsg = "During the resolving of qnetwork component: qnetwork component "
                                + fullQNetworkName + " does not have a CNN implementation but is required to have one";
                        Log.error(errMsg);
                        throw new RuntimeException(errMsg);
                    }
                    qnetwork.get().setComponentName(fullQNetworkName);
                    configuration.setQNetwork(new ArchitectureAdapter(fullQNetworkName, qnetwork.get()));
                }

                if (configuration.getLearningMethod() == LearningMethod.GAN)
                    CNNTrainCocos.checkGANCocos(configuration);

                if (configuration.hasPreprocessor()) {
                    PreprocessingComponentSymbol preprocessingSymbol = configuration.getPreprocessingComponent().get();
                    List<String> fullNameOfComponent = preprocessingSymbol.getPreprocessingComponentName();
                    String fullPreprocessorName = String.join(".", fullNameOfComponent);

                    int indexOfFirstNameCharacter = fullPreprocessorName.lastIndexOf('.') + 1;
                    fullPreprocessorName = fullPreprocessorName.substring(0, indexOfFirstNameCharacter)
                            + fullPreprocessorName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullPreprocessorName.substring(indexOfFirstNameCharacter + 1);
                    String instanceName = componentInstance.getFullName().replaceAll("\\.", "_");

                    TaggingResolver symtab = EMADLAbstractSymtab.createSymTabAndTaggingResolver(getModelsPath());
                    EMAComponentInstanceSymbol processor_instance = resolveComponentInstanceSymbol(fullPreprocessorName, symtab);
                    processor_instance.setFullName("CNNPreprocessor_" + instanceName);
                    List<FileContent> processorContents = new ArrayList<>();
                    generateComponent(processorContents, new HashSet<EMAComponentInstanceSymbol>(), symtab, processor_instance);
                    fixArmadilloImports(processorContents);

                    for (FileContent fileContent : processorContents) {
                        try {
                            emamGen.generateFile(fileContent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    String targetPath = getGenerationTargetPath();
                    ComponentPortInformation componentPortInformation;
                    componentPortInformation = pythonWrapper.generateAndTryBuilding(processor_instance, targetPath + "/pythonWrapper", targetPath);
                    PreprocessingComponentParameterAdapter componentParameter = new PreprocessingComponentParameterAdapter(componentPortInformation);
                    PreprocessingPortChecker.check(componentParameter);
                    preprocessingSymbol.setPreprocessingComponentParameter(componentParameter);
                }

                cnnTrainGenerator.setInstanceName(componentInstance.getFullName().replaceAll("\\.", "_"));
                List<FileContent> fileContentMap =  cnnTrainGenerator.generateStrings(configuration);
                fileContents.addAll(fileContentMap);
            }
        }
        return fileContents;
    }

    public String readResource(final String fileName, Charset charset) {
        try {
            return Resources.toString(Resources.getResource(fileName), charset);

        } catch (IllegalArgumentException e) {
            System.err.println("Resource file " + fileName + " not found");
            throw new RuntimeException("Resource file " + fileName + " not found");
        } catch (IOException e) {
            System.err.println("IO Error occurred");
            throw new RuntimeException("IO Error occurred");
        }
    }

    private void stopGeneratorIfWarning() {
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
        return this.generateFiles(taggingResolver, componentInstanceSymbol, "", "UNSET");
    }

    @Override
    public CMakeConfig getCmakeConfig() {
        mergeCMakeConfigs();
        return cMakeConfig;
    }

    private void mergeCMakeConfigs() {
        List<String> cmakeCommandList = emamGen.getCMakeConfig().getCMakeListsViewModel().getCmakeCommandList();
        List<String> cmakeCommandListEnd = emamGen.getCMakeConfig().getCMakeListsViewModel().getCmakeCommandListEnd();
        List<String> cmakeLibraryLinkageList = emamGen.getCMakeConfig().getCMakeListsViewModel().getCmakeLibraryLinkageList();
        LinkedHashSet<CMakeFindModule> moduleDependencies = emamGen.getCMakeConfig().getCMakeListsViewModel().getModuleDependencies();
        // merge
        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getCmakeCommandList()
                .stream().filter(s -> !cmakeCommandList.contains(s))
                .forEach(s -> cmakeCommandList.add(s));
        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getCmakeCommandListEnd()
                .stream().filter(s -> !cmakeCommandListEnd.contains(s))
                .forEach(s -> cmakeCommandListEnd.add(s));
        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getCmakeLibraryLinkageList()
                .stream().filter(s -> !cmakeLibraryLinkageList.contains(s))
                .forEach(s -> cmakeLibraryLinkageList.add(s));
        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getModuleDependencies()
                .stream().filter(s -> !moduleDependencies.contains(s))
                .forEach(s -> moduleDependencies.add(s));

        cMakeConfig.getCMakeListsViewModel().setCmakeCommandList(cmakeCommandList);
        cMakeConfig.getCMakeListsViewModel().setCmakeCommandListEnd(cmakeCommandListEnd);
        cMakeConfig.getCMakeListsViewModel().setCmakeLibraryLinkageList(cmakeLibraryLinkageList);
        cMakeConfig.getCMakeListsViewModel().setModuleDependencies(moduleDependencies);
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
