/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator.emadlgen;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.math._symboltable.MathStatementsSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.cnnarch.generator.CNNArchGenerator;
import de.monticore.lang.monticar.cnnarch.generator.CNNTrainGenerator;
import de.monticore.lang.monticar.cnnarch.generator.annotations.ArchitectureAdapter;
import de.monticore.lang.monticar.cnnarch.generator.preprocessing.PreprocessingComponentParameterAdapter;
import de.monticore.lang.monticar.cnnarch.generator.preprocessing.PreprocessingPortChecker;
import de.monticore.lang.monticar.cnnarch.generator.training.TrainingComponentsContainer;
import de.monticore.lang.monticar.cnnarch.generator.training.TrainingConfiguration;
import de.monticore.lang.monticar.cnnarch.generator.validation.TrainedArchitectureChecker;
import de.monticore.lang.monticar.cnnarch.gluongenerator.CNNTrain2Gluon;
import de.monticore.lang.monticar.emadl._cocos.EMADLCocos;
import de.monticore.lang.monticar.emadl.generator.backend.Backend;
import de.monticore.lang.monticar.generator.EMAMGenerator;
import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.MathCommandRegister;
import de.monticore.lang.monticar.generator.cmake.CMakeConfig;
import de.monticore.lang.monticar.generator.cpp.*;
import de.monticore.lang.monticar.generator.cpp.converter.TypeConverter;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperFactory;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperStandaloneApi;
import de.monticore.lang.monticar.generator.pythonwrapper.symbolservices.data.ComponentPortInformation;
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

import static de.monticore.lang.monticar.cnnarch.generator.validation.Constants.ROOT_SCHEMA_MODEL_PATH;

public class EMADLGenerator implements EMAMGenerator {

    private boolean generateCMake = false;
    private CMakeConfig cMakeConfig = new CMakeConfig("");
    private GeneratorCPP emamGen;
    private CNNArchGenerator cnnArchGenerator;
    private CNNTrainGenerator cnnTrainGenerator;
    private GeneratorPythonWrapperStandaloneApi pythonWrapper;
    private Backend backend;
    private EMADLFileHandler emadlFileHandler;
    private EMADLTagging emadlTaggingHandler;




    private boolean useDgl = false;
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
        emadlFileHandler = new EMADLFileHandler(this);
        emadlTaggingHandler = new EMADLTagging(this);
    }

    public EMADLFileHandler getEmadlFileHandler() {
        return emadlFileHandler;
    }

    public EMADLTagging getEmadlTaggingHandler() {
        return emadlTaggingHandler;
    }

    public boolean getUseDgl() { return useDgl; }

    public void setUseDgl(boolean useDgl){
        this.useDgl = useDgl;
    }

    public Backend getBackend() {return this.backend;}



    public GeneratorCPP getEmamGen() {
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


    public void generate(String modelPath, String qualifiedName, String pythonPath, String forced, boolean doCompile, String useDgl) throws IOException, TemplateException {
        processedArchitecture = new HashMap<>();
        emadlFileHandler.setModelsPath( modelPath );
        emadlFileHandler.setPythonPath(pythonPath);
        setUseDgl(useDgl.equals("y"));
        TaggingResolver symtab = emadlTaggingHandler.getSymTabAndTaggingResolver();
        EMAComponentInstanceSymbol instance = resolveComponentInstanceSymbol(qualifiedName, symtab);
        try {
            // copy the AdaNet files to
            emadlFileHandler.copyPythonFilesFromResource("AdaNet");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        emadlFileHandler.generateFiles(symtab, instance, pythonPath, forced);

        if (doCompile) {
            if (!generateCMake) // do it either way
                emadlFileHandler.generateCMakeFiles(instance);
            compile();
        }
        processedArchitecture = null;
    }



    private EMAComponentInstanceSymbol resolveComponentInstanceSymbol(String qualifiedName, TaggingResolver symtab) {
        String simpleName = Names.getSimpleName(qualifiedName);
        if (!Character.isUpperCase(simpleName.charAt(0))) {
            String packageName = qualifiedName.substring(0, qualifiedName.length() - simpleName.length() - 1);
            qualifiedName = Names.getQualifiedName(packageName, StringUtils.capitalize(simpleName));
        }

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

    public boolean isAlreadyTrained(String trainingHash, EMAComponentInstanceSymbol componentInstance) {
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

    public List<FileContent> generateStrings(TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol, Set<EMAComponentInstanceSymbol> allInstances, String forced) {
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
        fileContents.addAll(generateCNNTrainer(allInstances, instanceName));
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
        EMAComponentSymbol EMAComponentSymbol = componentInstanceSymbol.getComponentType().getReferencedSymbol();

        /* remove the following two lines if the component symbol full name bug with generic variables is fixed */
        EMAComponentSymbol.setFullName(null);
        EMAComponentSymbol.getFullName();
        /* */

        Optional<ArchitectureSymbol> architecture = componentInstanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);

        // set the path to AdaNet python files
        architecture.ifPresent(architectureSymbol -> {architectureSymbol.setAdaNetUtils(emadlFileHandler.getAdaNetUtils());});
        Optional<MathStatementsSymbol> mathStatements = EMAComponentSymbol.getSpannedScope().resolve("MathStatements", MathStatementsSymbol.KIND);

        EMADLCocos.checkAll(componentInstanceSymbol);

        if (architecture.isPresent()) {
            cnnArchGenerator.check(architecture.get());
            String dPath = emadlFileHandler.getDataPath(taggingResolver, EMAComponentSymbol, componentInstanceSymbol);
            String wPath = emadlFileHandler.getWeightsPath(EMAComponentSymbol, componentInstanceSymbol);
            HashMap layerPathParameterTags = emadlTaggingHandler.getLayerPathParameterTags(taggingResolver, EMAComponentSymbol, componentInstanceSymbol);
            layerPathParameterTags.putAll(emadlTaggingHandler.getLayerArtifactParameterTags(taggingResolver, EMAComponentSymbol, componentInstanceSymbol));
            architecture.get().setDataPath(dPath);
            architecture.get().setWeightsPath(wPath);
            architecture.get().processLayerPathParameterTags(layerPathParameterTags);
            architecture.get().setComponentName(EMAComponentSymbol.getFullName());
            architecture.get().setUseDgl(getUseDgl());
            if(!emadlFileHandler.getCustomFilesPath().equals("")) {
                architecture.get().setCustomPyFilesPath(emadlFileHandler.getCustomFilesPath() + "python/" + Backend.getBackendString(this.backend).toLowerCase());
            }
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

    public void generateCNN(List<FileContent> fileContents, TaggingResolver taggingResolver, EMAComponentInstanceSymbol instance, ArchitectureSymbol architecture) {
        List<FileContent> contents = cnnArchGenerator.generateStrings(taggingResolver, architecture);
        String fullName = instance.getFullName().replaceAll("\\.", "_");

        //get the components execute method
        String executeKey = "execute_" + fullName;
        List<String> executeMethods = emadlFileHandler.getContentOf(contents, executeKey);
        if (executeMethods.size() != 1) {
            throw new IllegalStateException("execute method of " + fullName + " not found");
        }
        String executeMethod = executeMethods.get(0);
        contents.remove(executeKey);

        List<String> applyBeamSearchMethods = emadlFileHandler.getContentOf(contents, "BeamSearch_" + fullName);
        String applyBeamSearchMethod = null;
        if (applyBeamSearchMethods.size() == 1) {
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
        fileContents.add(new FileContent(emadlFileHandler.readResource("CNNTranslator.h", Charsets.UTF_8), "CNNTranslator.h"));
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

    public void generateMathComponent(List<FileContent> fileContents, TaggingResolver taggingResolver, EMAComponentInstanceSymbol EMAComponentSymbol, MathStatementsSymbol mathStatementsSymbol) {
        fileContents.add(new FileContent(
                emamGen.generateString(taggingResolver, EMAComponentSymbol, mathStatementsSymbol),
                EMAComponentSymbol));
    }

    public void generateSubComponents(List<FileContent> fileContents, Set<EMAComponentInstanceSymbol> allInstances, TaggingResolver taggingResolver, EMAComponentInstanceSymbol componentInstanceSymbol) {
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

    public List<FileContent> generateCNNTrainer(Set<EMAComponentInstanceSymbol> allInstances, String mainComponentName) {
        boolean copied = emadlFileHandler.copySchemaFilesFromResource(ROOT_SCHEMA_MODEL_PATH);
        List<FileContent> fileContents = new ArrayList<>();
        TaggingResolver symTabAndTaggingResolver = emadlTaggingHandler.getSymTabAndTaggingResolver();
        for (EMAComponentInstanceSymbol componentInstance : allInstances) {
            EMAComponentSymbol component = componentInstance.getComponentType().getReferencedSymbol();
            Optional<ArchitectureSymbol> architecture = component.getSpannedScope().resolve("", ArchitectureSymbol.KIND);

            if (architecture.isPresent()) {
                String mainComponentConfigFilename = mainComponentName.replaceAll("\\.", "/");
                String componentConfigFilename = component.getFullName().replaceAll("\\.", "/");
                String instanceConfigFilename = component.getFullName().replaceAll("\\.", "/") + "_" + component.getName();
                String trainConfigFilename = emadlFileHandler.getConfigFilename(mainComponentConfigFilename, componentConfigFilename, instanceConfigFilename);
                if (Strings.isNullOrEmpty(trainConfigFilename)) {
                    String message = String.format("Missing training configuration. Could not find a file with any of the following names (only one needed): '%s.conf', '%s.conf', '%s.conf'. These files denote respectively the configuration for the single instance, the component or the whole system.",
                            emadlFileHandler.getModelsPath() + instanceConfigFilename, emadlFileHandler.getModelsPath() + componentConfigFilename, emadlFileHandler.getModelsPath() + mainComponentConfigFilename);
                    Log.error(message);
                    throw new RuntimeException(String.format("Missing training configuration for network '%s'", mainComponentName));
                }

                cnnTrainGenerator.setGenerationTargetPath(getGenerationTargetPath());
                if (cnnTrainGenerator instanceof CNNTrain2Gluon) {
                    ((CNNTrain2Gluon) cnnTrainGenerator).setRootProjectModelsDir(emadlFileHandler.getModelsPath());
                }
                List<String> names = Splitter.on("/").splitToList(trainConfigFilename);
                trainConfigFilename = names.get(names.size() - 1);
                Path modelPath = Paths.get(emadlFileHandler.getModelsPath() + Joiner.on("/").join(names.subList(0, names.size() - 1)));
                TrainingConfiguration trainingConfiguration = cnnTrainGenerator.createTrainingConfiguration(modelPath,
                        trainConfigFilename, copied ? Paths.get(getGenerationTargetPath()) : null);

                // Annotate train configuration with architecture
                final String fullConfigName = String.join(".", names);
                ArchitectureSymbol correspondingArchitecture = this.processedArchitecture.get(fullConfigName);
                assert correspondingArchitecture != null : "No architecture found for train " + fullConfigName + " configuration!";

                TrainingComponentsContainer trainingComponentsContainer = new TrainingComponentsContainer();
                trainingComponentsContainer.setTrainedArchitecture(trainingConfiguration,
                        new ArchitectureAdapter(correspondingArchitecture));
                TrainedArchitectureChecker.checkComponents(trainingConfiguration, trainingComponentsContainer);

                // Resolve critic network if critic is present
                Optional<String> criticName = trainingConfiguration.getCritic();
                if (criticName.isPresent()) {
                    String fullCriticName = criticName.get();
                    int indexOfFirstNameCharacter = fullCriticName.lastIndexOf('.') + 1;
                    fullCriticName = fullCriticName.substring(0, indexOfFirstNameCharacter)
                            + fullCriticName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullCriticName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(fullCriticName,
                            symTabAndTaggingResolver);
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> critic = instanceSymbol.getSpannedScope().resolve("",
                            ArchitectureSymbol.KIND);
                    if (!critic.isPresent()) {
                        String message = String.format("Resolving critic component failed. Critic component '%s' does not have a CNN implementation, but is required to have one.", fullCriticName);
                        Log.error(message);
                        throw new RuntimeException(message);
                    }
                    ArchitectureSymbol architectureSymbol = critic.get();
                    architectureSymbol.setComponentName(fullCriticName);
                    trainingComponentsContainer.setCriticNetwork(new ArchitectureAdapter(architectureSymbol));
                }

                // Resolve discriminator network if discriminator is present
                Optional<String> discriminatorName = trainingConfiguration.getDiscriminatorName();
                if (discriminatorName.isPresent()) {
                    String fullDiscriminatorName = discriminatorName.get();
                    int indexOfFirstNameCharacter = fullDiscriminatorName.lastIndexOf('.') + 1;
                    fullDiscriminatorName = fullDiscriminatorName.substring(0, indexOfFirstNameCharacter)
                            + fullDiscriminatorName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullDiscriminatorName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(
                            fullDiscriminatorName, emadlTaggingHandler.getSymTabAndTaggingResolver());
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> discriminator = instanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                    if (!discriminator.isPresent()) {
                        String message = String.format("Resolving discriminator component failed. Discriminator component '%s' does not have a CNN implementation, but is required to have one.", fullDiscriminatorName);
                        Log.error(message);
                        throw new RuntimeException(message);
                    }
                    ArchitectureSymbol architectureSymbol = discriminator.get();
                    architectureSymbol.setComponentName(fullDiscriminatorName);
                    trainingComponentsContainer.setDiscriminatorNetwork(
                            new ArchitectureAdapter(architectureSymbol));
                }

                // Resolve QNetwork if present
                Optional<String> qNetworkName = trainingConfiguration.getQNetwork();
                if (qNetworkName.isPresent()) {
                    String fullQNetworkName =  qNetworkName.get();
                    int indexOfFirstNameCharacter = fullQNetworkName.lastIndexOf('.') + 1;
                    fullQNetworkName = fullQNetworkName.substring(0, indexOfFirstNameCharacter)
                            + fullQNetworkName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullQNetworkName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(
                            fullQNetworkName, emadlTaggingHandler.getSymTabAndTaggingResolver());
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> qNetwork = instanceSymbol.getSpannedScope().resolve("",
                            ArchitectureSymbol.KIND);
                    if (!qNetwork.isPresent()) {
                        String message = String.format("Resolving Q-Network component failed. Q-Network component '%s' does not have a CNN implementation, but is required to have one.", fullQNetworkName);
                        Log.error(message);
                        throw new RuntimeException(message);
                    }
                    ArchitectureSymbol architectureSymbol = qNetwork.get();
                    architectureSymbol.setComponentName(fullQNetworkName);
                    trainingComponentsContainer.setQNetwork(new ArchitectureAdapter(architectureSymbol));
                }

                // Resolve encoder network if present
                Optional<String> encoderName = trainingConfiguration.getEncoderName();
                if (encoderName.isPresent()) {
                    String fullEncoderName = encoderName.get();
                    int indexOfFirstNameCharacter = fullEncoderName.lastIndexOf('.') + 1;
                    fullEncoderName = fullEncoderName.substring(0, indexOfFirstNameCharacter)
                            + fullEncoderName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullEncoderName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(
                            fullEncoderName, emadlTaggingHandler.getSymTabAndTaggingResolver());
                    EMADLCocos.checkAll(instanceSymbol);
                    Optional<ArchitectureSymbol> encoder = instanceSymbol.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
                    if (!encoder.isPresent()) {
                        String message = String.format("Resolving encoder component failed. Encoder component '%s' does not have a CNN implementation, but is required to have one.", fullEncoderName);
                        Log.error(message);
                        throw new RuntimeException(message);
                    }
                    ArchitectureSymbol architectureSymbol = encoder.get();
                    architectureSymbol.setComponentName(fullEncoderName);
                    trainingComponentsContainer.setEncoderNetwork(
                            new ArchitectureAdapter(architectureSymbol));

                    ArchitectureAdapter decoder = trainingComponentsContainer.getTrainedArchitecture().get();
                    ArchitectureSymbol decoderArchitectureSymbol = decoder.getArchitectureSymbol();

                    encoder.get().setAuxiliaryArchitecture(decoderArchitectureSymbol);

                }

                Optional<String> rewardFunctionNameOpt = trainingConfiguration.getRewardFunctionName();
                if (rewardFunctionNameOpt.isPresent()) {
                    String fullRewardFunctionName =  rewardFunctionNameOpt.get();
                    int indexOfFirstNameCharacter = fullRewardFunctionName.lastIndexOf('.') + 1;
                    fullRewardFunctionName = fullRewardFunctionName.substring(0, indexOfFirstNameCharacter)
                            + fullRewardFunctionName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullRewardFunctionName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(
                            fullRewardFunctionName, emadlTaggingHandler.getSymTabAndTaggingResolver());
                    EMADLCocos.checkAll(instanceSymbol);
                    trainingComponentsContainer.setRewardFunction(instanceSymbol);
                }

                Optional<String> policyFunctionNameOpt = trainingConfiguration.getPolicyFunctionName();
                if (policyFunctionNameOpt.isPresent()) {
                    String fullPolicyFunctionName =  policyFunctionNameOpt.get();
                    int indexOfFirstNameCharacter = fullPolicyFunctionName.lastIndexOf('.') + 1;
                    fullPolicyFunctionName = fullPolicyFunctionName.substring(0, indexOfFirstNameCharacter)
                            + fullPolicyFunctionName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullPolicyFunctionName.substring(indexOfFirstNameCharacter + 1);

                    EMAComponentInstanceSymbol instanceSymbol = resolveComponentInstanceSymbol(
                            fullPolicyFunctionName, emadlTaggingHandler.getSymTabAndTaggingResolver());
                    EMADLCocos.checkAll(instanceSymbol);
                    trainingComponentsContainer.addTrainingComponent("policy",
                            instanceSymbol.getComponentType().getReferencedSymbol());
                }

                Optional<String> preprocessor = trainingConfiguration.getPreprocessor();
                if (preprocessor.isPresent()) {
                    String fullPreprocessorName = preprocessor.get();

                    int indexOfFirstNameCharacter = fullPreprocessorName.lastIndexOf('.') + 1;
                    fullPreprocessorName = fullPreprocessorName.substring(0, indexOfFirstNameCharacter)
                            + fullPreprocessorName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                            + fullPreprocessorName.substring(indexOfFirstNameCharacter + 1);
                    String instanceName = componentInstance.getFullName().replaceAll("\\.", "_");

                    TaggingResolver symtab = emadlTaggingHandler.getSymTabAndTaggingResolver();
                    EMAComponentInstanceSymbol processor_instance = resolveComponentInstanceSymbol(fullPreprocessorName, symtab);
                    processor_instance.setFullName("CNNPreprocessor_" + instanceName);
                    List<FileContent> processorContents = new ArrayList<>();
                    generateComponent(processorContents, new HashSet<>(), symtab, processor_instance);
                    emadlFileHandler.fixArmadilloImports(processorContents);

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
                    trainingComponentsContainer.setPreprocessingComponentParameter(componentParameter);
                }

                cnnTrainGenerator.setInstanceName(componentInstance.getFullName().replaceAll("\\.", "_"));
                List<FileContent> fileContentList = cnnTrainGenerator.generateStrings(trainingConfiguration,
                        trainingComponentsContainer, copied ? Paths.get(getGenerationTargetPath()) : null);
                fileContents.addAll(fileContentList);
            }
        }
        return fileContents;
    }

    public void stopGeneratorIfWarning() {
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

        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getCmakeCommandList()
                .stream().forEach(s -> cMakeConfig.addCMakeCommand(s));
        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getCmakeCommandListEnd()
                .stream().forEach(s -> cMakeConfig.addCMakeCommandEnd(s));
        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getCmakeLibraryLinkageList()
                .stream().forEach(s -> cMakeConfig.addCmakeLibraryLinkage(s));
        cnnArchGenerator.getCmakeConfig().getCMakeListsViewModel().getModuleDependencies()
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