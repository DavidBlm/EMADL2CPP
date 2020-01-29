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
import de.monticore.lang.monticar.cnnarch.gluongenerator.CNNTrain2Gluon;
import de.monticore.lang.monticar.cnnarch.gluongenerator.annotations.ArchitectureAdapter;
import de.monticore.lang.monticar.cnntrain._cocos.CNNTrainCocos;
import de.monticore.lang.monticar.cnntrain._symboltable.ConfigurationSymbol;
import de.monticore.lang.monticar.emadl._cocos.DataPathCocos;
import de.monticore.lang.monticar.emadl._cocos.EMADLCocos;
import de.monticore.lang.monticar.emadl.tagging.dltag.DataPathSymbol;
import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.cpp.ArmadilloHelper;
import de.monticore.lang.monticar.generator.cpp.GeneratorEMAMOpt2CPP;
import de.monticore.lang.monticar.generator.cpp.SimulatorIntegrationHelper;
import de.monticore.lang.monticar.generator.cpp.TypesGeneratorCPP;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapper;
import de.monticore.lang.monticar.generator.cpp.converter.TypeConverter;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperFactory;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperStandaloneApi;
import de.monticore.lang.tagging._symboltable.TagSymbol;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.Splitters;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.antlr.v4.codegen.target.Python2Target;

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

public class EMADLGenerator {

    private GeneratorEMAMOpt2CPP emamGen;
    private CNNArchGenerator cnnArchGenerator;
    private CNNTrainGenerator cnnTrainGenerator;
    private GeneratorPythonWrapperStandaloneApi pythonWrapper;
    private Backend backend;

    private String modelsPath;

    private Map<String, ArchitectureSymbol> processedArchitecture;

    public EMADLGenerator(Backend backend) {
        this.backend = backend;
        emamGen = new GeneratorEMAMOpt2CPP();
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

    public GeneratorEMAMOpt2CPP getEmamGen() {
        return emamGen;
    }

    public void generate(String modelPath, String qualifiedName, String pythonPath, String forced, boolean doCompile) throws IOException, TemplateException {
        processedArchitecture = new HashMap<>();
        setModelsPath( modelPath );
        TaggingResolver symtab = EMADLAbstractSymtab.createSymTabAndTaggingResolver(getModelsPath());
        EMAComponentInstanceSymbol instance = resolveComponentInstanceSymbol(qualifiedName, symtab);


        generateFiles(symtab, instance, symtab, pythonPath, forced);

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
            Log.error("Component with name '" + componentName + "' does not exist.");
            System.exit(1);
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
                Log.error("During compilation, an error occured. See above for more details.");
                System.exit(1);
            }
        }catch(Exception e){
            Log.error("During compilation, the following error occured: '" + e.toString() + "'");
            System.exit(1);
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

    public void generateFiles(TaggingResolver taggingResolver, EMAComponentInstanceSymbol EMAComponentSymbol, Scope symtab, String pythonPath, String forced) throws IOException {
        Set<EMAComponentInstanceSymbol> allInstances = new HashSet<>();
        List<FileContent> fileContents = generateStrings(taggingResolver, EMAComponentSymbol, symtab, allInstances, forced);

        for (FileContent fileContent : fileContents) {
            emamGen.generateFile(fileContent);
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
                        Log.error("Training aborted: exit code " + Integer.toString(exitCode));
                        System.exit(1);
                    }

                    if(exitCode != 0) {
                        Log.error("Training failed: exit code " + Integer.toString(exitCode));
                        System.exit(1);
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
            emamGen.generateFile(fileContent);
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

    public List<FileContent> generateStrings(TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol, Scope symtab, Set<EMAComponentInstanceSymbol> allInstances, String forced){
        List<FileContent> fileContents = new ArrayList<>();
        processedArchitecture = new HashMap<>();

        generateComponent(fileContents, allInstances, taggingResolver, componentInstanceSymbol, symtab);

        String instanceName = componentInstanceSymbol.getComponentType().getFullName().replaceAll("\\.", "_");
        fileContents.addAll(generateCNNTrainer(allInstances, instanceName));

        fileContents.add(ArmadilloHelper.getArmadilloHelperFileContent());
        TypesGeneratorCPP tg = new TypesGeneratorCPP();
        fileContents.addAll(tg.generateTypes(TypeConverter.getTypeSymbols()));

        if (cnnArchGenerator.isCMakeRequired()) {
            cnnArchGenerator.setGenerationTargetPath(getGenerationTargetPath());
            Map<String, String> cmakeContentsMap = cnnArchGenerator.generateCMakeContent(componentInstanceSymbol.getFullName());
            for (String fileName : cmakeContentsMap.keySet()){
                fileContents.add(new FileContent(cmakeContentsMap.get(fileName), fileName));
            }
        }

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
            Log.warn("Tagging info for symbol was found, ignoring data_paths.txt: " + dataPath);
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

    protected void generateComponent(List<FileContent> fileContents,
                                     Set<EMAComponentInstanceSymbol> allInstances,
                                     TaggingResolver taggingResolver,
                                     EMAComponentInstanceSymbol componentInstanceSymbol,
                                     Scope symtab){
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
            architecture.get().setDataPath(dPath);
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
            generateSubComponents(fileContents, allInstances, taggingResolver, componentInstanceSymbol, symtab);
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
        Map<String,String> contentMap = cnnArchGenerator.generateStrings(architecture);
        String fullName = instance.getFullName().replaceAll("\\.", "_");

        //get the components execute method
        String executeKey = "execute_" + fullName;
        String executeMethod = contentMap.get(executeKey);
        if (executeMethod == null){
            throw new IllegalStateException("execute method of " + fullName + " not found");
        }
        contentMap.remove(executeKey);

        String applyBeamSearchMethod = contentMap.get("BeamSearch_" + fullName);

        String component = emamGen.generateString(taggingResolver, instance, (MathStatementsSymbol) null);
        FileContent componentFileContent = new FileContent(
                transformComponent(component, "CNNPredictor_" + fullName, applyBeamSearchMethod, executeMethod, architecture),
                instance);

        for (String fileName : contentMap.keySet()){
            fileContents.add(new FileContent(contentMap.get(fileName), fileName));
        }
        fileContents.add(componentFileContent);
        fileContents.add(new FileContent(readResource("CNNTranslator.h", Charsets.UTF_8), "CNNTranslator.h"));
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

    public void generateSubComponents(List<FileContent> fileContents, Set<EMAComponentInstanceSymbol> allInstances, TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol, Scope symtab){
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
                generateComponent(fileContents, allInstances, taggingResolver, instanceSymbol, symtab);
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
                        Log.error("During the resolving of critic component: Critic component "
                                + fullCriticName + " does not have a CNN implementation but is required to have one");
                        System.exit(-1);
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
                        Log.error("During the resolving of critic component: Critic component "
                                + fullDiscriminatorName + " does not have a CNN implementation but is required to have one");
                        System.exit(-1);
                    }
                    discriminator.get().setComponentName(fullDiscriminatorName);
                    configuration.setDiscriminatorNetwork(new ArchitectureAdapter(fullDiscriminatorName, discriminator.get()));
                    //CNNTrainCocos.checkCriticCocos(configuration);
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
                        Log.error("During the resolving of qnetwork component: qnetwork component "
                                + fullQNetworkName + " does not have a CNN implementation but is required to have one");
                        System.exit(-1);
                    }
                    qnetwork.get().setComponentName(fullQNetworkName);
                    configuration.setQNetwork(new ArchitectureAdapter(fullQNetworkName, qnetwork.get()));
                    //CNNTrainCocos.checkCriticCocos(configuration);
                }

                if (configuration.hasPreprocessor()) {
                    String fullPreprocessorName = configuration.getPreprocessingName().get();
                    int indexOfFirstNameCharacter = fullPreprocessorName.lastIndexOf('.') + 1;
                    fullPreprocessorName = fullPreprocessorName.substring(0, indexOfFirstNameCharacter)
                            + fullPreprocessorName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullPreprocessorName.substring(indexOfFirstNameCharacter + 1);
                    String instanceName = componentInstance.getFullName().replaceAll("\\.", "_");

                    TaggingResolver symtab = EMADLAbstractSymtab.createSymTabAndTaggingResolver(getModelsPath());
                    EMAComponentInstanceSymbol processor_instance = resolveComponentInstanceSymbol(fullPreprocessorName, symtab);
                    processor_instance.setFullName("CNNPreprocessor_" + instanceName);
                    List<FileContent> processorContents = new ArrayList<>();
                    generateComponent(processorContents, new HashSet<EMAComponentInstanceSymbol>(), symtab, processor_instance, symtab);
                    fixArmadilloImports(processorContents);

                    for (FileContent fileContent : processorContents) {
                        try {
                            emamGen.generateFile(fileContent);
                        } catch (IOException e) {
                            //todo: fancy error message
                            e.printStackTrace();
                        }
                    }

                    String targetPath = getGenerationTargetPath();
                    pythonWrapper.generateAndTryBuilding(processor_instance, targetPath + "/pythonWrapper", targetPath);
                }

                cnnTrainGenerator.setInstanceName(componentInstance.getFullName().replaceAll("\\.", "_"));
                Map<String, String> fileContentMap =  cnnTrainGenerator.generateStrings(configuration);
                for (String fileName : fileContentMap.keySet()){
                    fileContents.add(new FileContent(fileContentMap.get(fileName), fileName));
                }
            }
        }
        return fileContents;
    }

    public String readResource(final String fileName, Charset charset) {
        try {
            return Resources.toString(Resources.getResource(fileName), charset);

        } catch (IllegalArgumentException e) {
            System.err.println("Resource file " + fileName + " not found");
            System.exit(1);
            return null;
        } catch (IOException e) {
            System.err.println("IO Error occurred");
            System.exit(1);
            return null;
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

}
