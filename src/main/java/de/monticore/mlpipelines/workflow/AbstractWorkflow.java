package de.monticore.mlpipelines.workflow;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.io.paths.ModelPath;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.lang.monticar.emadl._symboltable.EMADLLanguage;
import de.monticore.lang.monticar.semantics.Constants;
import de.monticore.lang.monticar.semantics.ExecutionSemantics;
import de.monticore.lang.monticar.semantics.construct.SymtabCreator;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.mlpipelines.backend.generation.MontiAnnaGenerator;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.parsing.ConfigurationLanguageParser;
import de.monticore.parsing.EMADLParser;
import de.monticore.symbolmanagement.SymbolTableCreator;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.StringTransformations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class AbstractWorkflow {

    protected final MontiAnnaContext montiAnnaContext;
    protected MontiAnnaGenerator montiAnnaGenerator;

    protected Pipeline pipeline;

    public AbstractWorkflow(final MontiAnnaContext montiAnnaContext) {
        this.montiAnnaContext = montiAnnaContext;
        this.montiAnnaGenerator = new MontiAnnaGenerator(montiAnnaContext);
    }

    public AbstractWorkflow() {
        this.montiAnnaContext = MontiAnnaContext.getInstance();
        this.montiAnnaGenerator = new MontiAnnaGenerator(this.montiAnnaContext);
    }

    public void setMontiAnnaGenerator(final MontiAnnaGenerator montiAnnaGenerator) {
        this.montiAnnaGenerator = montiAnnaGenerator;
    }

    public void setPipeline(final Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public void execute() throws IOException {
        // frontend
        final String rootModelName = Names.getSimpleName(montiAnnaContext.getRootModelName());
        final String pathToModelsDirectory = Paths.get(montiAnnaContext.getParentModelPath().toString(),
                getDirectoryPathSupplementFromComponentName(montiAnnaContext.getRootModelName())).toString();
        final String pathToRootModel = Paths.get(pathToModelsDirectory, rootModelName + ".emadl").toString();
        final ASTEMACompilationUnit rootEMADLComponent = new EMADLParser().parseModelIfExists(pathToRootModel);
        final ModelPath modelPath = new ModelPath(Paths.get(montiAnnaContext.getParentModelPath().toString()));
        final Scope emadlSymbolTable = SymbolTableCreator.createEMADLSymbolTable(rootEMADLComponent,
                new GlobalScope(modelPath, new EMADLLanguage()));

//        final ConfigurationValidator configurationValidator = new ConfigurationValidator();
//        configurationValidator.validateTrainingConfiguration(trainingConfigurationSymbol);

        checkCoCos();
        backendSpecificValidations();
        final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics = calculateExecutionSemantics();

        //backend
        generateBackendArtefactsIntoExperiment();
        final LearningMethod learningMethod = LearningMethod.SUPERVISED;
        createPipeline(learningMethod);
        pipeline.setNetworkInstancesConfigs(
                this.getNetworkInstanceConfigs(pathToModelsDirectory, rootModelName, rootEMADLComponent, emadlSymbolTable)
        );
        pipeline.setPipelineModelWithExecutionSemantics(pipelineModelWithExecutionSemantics);

        executePipeline();
    }

    protected String getDirectoryPathSupplementFromComponentName(final String rootModelName) {
        final int lastIndexOfDot = rootModelName.lastIndexOf(".");
        if (lastIndexOfDot == -1)
            return rootModelName;
        return rootModelName.substring(0, lastIndexOfDot).replace(".", "/");
    }

    protected static EMAComponentInstanceSymbol getNetworkTobeTrained(
            final ASTEMACompilationUnit rootEMADLComponent,
            Scope emadlSymbolTable) {
        final String componentName = rootEMADLComponent.getComponent().getName();
        final String instanceName = componentName.substring(0, 1).toLowerCase() + componentName.substring(1);
        final EMAComponentInstanceSymbol componentInstance = (EMAComponentInstanceSymbol) emadlSymbolTable.resolve(
                instanceName, EMAComponentInstanceSymbol.KIND).get();
        for (final EMAComponentInstanceSymbol subcomponent : componentInstance.getSubComponents()) {
            final EMAComponentSymbol referencedSymbol = subcomponent.getComponentType().getReferencedSymbol();
            final Optional<ArchitectureSymbol> network = referencedSymbol.getSpannedScope()
                    .resolve("", ArchitectureSymbol.KIND);
            if (network.isPresent())
                return subcomponent;
        }
        throw new IllegalStateException();
    }

    protected List<EMAComponentInstanceSymbol> getAllNetworkInstances(
            final ASTEMACompilationUnit rootEMADLComponent,
            Scope emadlSymbolTable) {
        List<EMAComponentInstanceSymbol> networkInstancesList = new ArrayList<>();
        final String componentName = rootEMADLComponent.getComponent().getName();
        final String instanceName = componentName.substring(0, 1).toLowerCase() + componentName.substring(1);
        final EMAComponentInstanceSymbol componentInstance = (EMAComponentInstanceSymbol) emadlSymbolTable.resolve(
                instanceName, EMAComponentInstanceSymbol.KIND).get();
        for (final EMAComponentInstanceSymbol subcomponent : componentInstance.getSubComponents()) {
            final EMAComponentSymbol referencedSymbol = subcomponent.getComponentType().getReferencedSymbol();
            final Optional<ArchitectureSymbol> network = referencedSymbol.getSpannedScope()
                    .resolve("", ArchitectureSymbol.KIND);
            if (network.isPresent()) {
                networkInstancesList.add(subcomponent);
            }
        }
        return networkInstancesList;
    }

    protected List<Map<String, Object>> getNetworkInstanceConfigs(
            String pathToModelsDirectory, String rootModelName,
            final ASTEMACompilationUnit rootEMADLComponent,
            Scope emadlSymbolTable) throws IOException {
        List<Map<String, Object>> networkInstancesConfigs = new ArrayList<>();

        List<EMAComponentInstanceSymbol> networkInstances = getAllNetworkInstances(rootEMADLComponent, emadlSymbolTable);

        for (EMAComponentInstanceSymbol instanceSymbol : networkInstances) {
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("network", instanceSymbol);
            String instanceNetworkName = this.getNetworkName(instanceSymbol);
            configMap.put("networkName", instanceNetworkName);
            String instanceName = instanceSymbol.getName();
            String componentTypeName = instanceSymbol.getComponentType().getName();
            // Set configurations for NAS:
            ASTConfLangCompilationUnit nasConf = this.getNASConfiguration(
                    pathToModelsDirectory, rootModelName, instanceName, componentTypeName
            );
            ASTConfLangCompilationUnit instanceTrainingConfiguration = this.getAutoMLConfiguration(
                    pathToModelsDirectory, rootModelName, instanceName, componentTypeName, instanceNetworkName + ".conf"
            );
            instanceTrainingConfiguration.getConfiguration().addSuperConfiguration(nasConf.getConfiguration());
            configMap.put("trainingConfiguration", instanceTrainingConfiguration);

            // Set configurations for Hyperparameters Optimization algorithm:
            ASTConfLangCompilationUnit searchSpace = this.getAutoMLConfiguration(
                    pathToModelsDirectory, rootModelName, instanceName, componentTypeName, "SearchSpace.conf"
            );
            configMap.put("SearchSpace", searchSpace);
            ASTConfLangCompilationUnit hyperparamsOptConf = this.getAutoMLConfiguration(
                    pathToModelsDirectory, rootModelName, instanceName, componentTypeName, "HyperparameterOpt.conf"
            );
            configMap.put("HyperparameterOpt", hyperparamsOptConf);
            ASTConfLangCompilationUnit evaluationCriteria = this.getAutoMLConfiguration(
                    pathToModelsDirectory, rootModelName, instanceName, componentTypeName, "EvaluationCriteria.conf"
            );
            configMap.put("EvaluationCriteria", evaluationCriteria);

            // Set pipeline configuration
            String pipelineConfigName = instanceNetworkName + "_pipeline.conf";
            ASTConfLangCompilationUnit pipelineConfiguration = this.getAutoMLConfiguration(
                    pathToModelsDirectory, rootModelName, instanceName, componentTypeName, pipelineConfigName
            );
            configMap.put("pipelineConfiguration", pipelineConfiguration);

            networkInstancesConfigs.add(configMap);
        }
        return networkInstancesConfigs;
    }

    public ASTConfLangCompilationUnit parseTrainingConfiguration(final String pathToTrainingConfiguration)
            throws IOException {
        return new ConfigurationLanguageParser().parseModelIfExists(pathToTrainingConfiguration);
    }

    public ASTConfLangCompilationUnit parsePipelineConfiguration(final String pathToPipelineConfiguration)
            throws IOException {
        return new ConfigurationLanguageParser().parseModelIfExists(pathToPipelineConfiguration);
    }

    //TODO implement me
    protected void checkCoCos() {

    }

    //TODO implement me
    protected void backendSpecificValidations() {
    }

    protected EMAComponentInstanceSymbol calculateExecutionSemantics() throws IOException {
        //TODO get from schema
        final String pathToPipelineReferenceModel = Paths.get(
                montiAnnaContext.getPipelineReferenceModelsPath().toString(), "LinearPipeline.ema").toString();
        EMAComponentInstanceSymbol pipelineReferenceModel = parsePipelineReferenceModelToEMAComponent(
                pathToPipelineReferenceModel);
        return addExecutionSemanticsToEmaComponent(
                pipelineReferenceModel);
    }

    public void generateBackendArtefactsIntoExperiment() {
        montiAnnaGenerator.generateTargetBackendArtefacts();
    }

    public abstract void createPipeline(final LearningMethod learningMethod);

    protected void executePipeline() {
        this.pipeline.execute();
    }

    public EMAComponentInstanceSymbol parsePipelineReferenceModelToEMAComponent(final String pathToPipeline)
            throws IOException {
        final ASTEMACompilationUnit astemaCompilationUnit = new EMADLParser().parseModelIfExists(
                pathToPipeline);
        final ModelPath modelPath = new ModelPath(Paths.get("src/test/resources/models/pipeline"));
        final Scope pipelineSymbolTable = SymbolTableCreator.createEMADLSymbolTable(astemaCompilationUnit,
                new GlobalScope(modelPath, new EMADLLanguage()));
        final String pipelineName = astemaCompilationUnit.getComponent().getName();
        final Optional<EMAComponentInstanceSymbol> emaInstanceComponent = pipelineSymbolTable.resolve(
                StringTransformations.uncapitalize(pipelineName), EMAComponentInstanceSymbol.KIND);
        return emaInstanceComponent.orElseThrow(IllegalStateException::new);
    }

    public EMAComponentInstanceSymbol addExecutionSemanticsToEmaComponent(EMAComponentInstanceSymbol pipelineReferenceModel) {
        final TaggingResolver symTab = SymtabCreator.createSymTab("src/test/resources", "src/main/resources",
                Constants.SYNTHESIZED_COMPONENTS_ROOT);
        new ExecutionSemantics(symTab, pipelineReferenceModel).addExecutionSemantics();
        return pipelineReferenceModel;
    }

    private String getNetworkName(EMAComponentInstanceSymbol network) {
        String fullName = network.getFullName();
        int packageIndex = fullName.indexOf('.');
        String networkName = fullName.substring(packageIndex + 1).replace(".", "_");
        return networkName;
    }

    protected ASTConfLangCompilationUnit getAutoMLConfiguration(String modelDirPath, String configurationName) throws IOException {
        String pathToConfiguration = Paths.get(modelDirPath, configurationName).toString();
        return this.parseTrainingConfiguration(pathToConfiguration);
    }

    protected ASTConfLangCompilationUnit getAutoMLConfiguration(
            String pathToModelsDirectory, String rootModelName,
            String instanceName, String componentTypeName, String configurationName) throws IOException {
        String pathToConfiguration;
        String modelDirPath = Paths.get(pathToModelsDirectory, rootModelName).toString();
        Path instanceSpecificPath = Paths.get(modelDirPath + "_" + instanceName, configurationName);
        Path componentTypeSpecificPath = Paths.get(pathToModelsDirectory, componentTypeName, configurationName);
        Path parentPath = Paths.get(modelDirPath, configurationName);

        if (Files.exists(instanceSpecificPath)) {
            // Priority 1: instance specific configuration
            pathToConfiguration = instanceSpecificPath.toString();
        } else if (Files.exists(componentTypeSpecificPath)) {
            // Priority 2: component type specific configuration
            pathToConfiguration = componentTypeSpecificPath.toString();
        } else if (Files.exists(parentPath)) {
            // Priority 3: parent/global configuration
            pathToConfiguration = parentPath.toString();
        } else {
            throw new FileNotFoundException(String.format("No conf file %s found.", configurationName));
        }

        return this.parseTrainingConfiguration(pathToConfiguration);
    }

    protected ASTConfLangCompilationUnit getNASConfiguration(String modelDirPath) throws IOException {
        if (Files.exists(Paths.get(modelDirPath, "AdaNet.conf"))) {
            return this.getAutoMLConfiguration(modelDirPath, "AdaNet.conf");
        } else if (Files.exists(Paths.get(modelDirPath, "EfficientNet.conf"))) {
            return this.getAutoMLConfiguration(modelDirPath, "EfficientNet.conf");
        } else {
            return null;
        }
    }

    protected ASTConfLangCompilationUnit getNASConfiguration(
            String pathToModelsDirectory, String rootModelName,
            String instanceName, String componentTypeName) throws IOException {
        try {
            return this.getAutoMLConfiguration(
                    pathToModelsDirectory, rootModelName, instanceName, componentTypeName, "AdaNet.conf"
            );
        } catch (FileNotFoundException fileNotFoundException) {
            return this.getAutoMLConfiguration(
                    pathToModelsDirectory, rootModelName, instanceName, componentTypeName, "EfficientNet.conf"
            );
        }
    }
}
