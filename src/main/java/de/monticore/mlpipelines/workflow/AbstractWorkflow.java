package de.monticore.mlpipelines.workflow;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._symboltable.ConfLangLanguage;
import conflang._symboltable.ConfigurationSymbol;
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
import java.nio.file.Paths;
import java.util.Optional;

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
        // measure the time for the whole function
        final long startTime = System.nanoTime();

        // frontend
        final String rootModelName = Names.getSimpleName(montiAnnaContext.getRootModelName());
        final String pathToModelsDirectory = Paths.get(montiAnnaContext.getParentModelPath().toString(),
                getDirectoryPathSupplementFromComponentName(montiAnnaContext.getRootModelName())).toString();
        final String pathToRootModel = Paths.get(pathToModelsDirectory, rootModelName + ".emadl").toString();
        final ASTEMACompilationUnit rootEMADLComponent = new EMADLParser().parseModelIfExists(pathToRootModel);
        final ModelPath modelPath = new ModelPath(Paths.get(montiAnnaContext.getParentModelPath().toString()));
        final Scope emadlSymbolTable = SymbolTableCreator.createEMADLSymbolTable(rootEMADLComponent,
                new GlobalScope(modelPath, new EMADLLanguage()));
        final EMAComponentInstanceSymbol network = getNetworkTobeTrained(rootEMADLComponent, emadlSymbolTable);

        final String modelConfDir = Paths.get(pathToModelsDirectory, rootModelName).toString();
        final String networkName = this.getNetworkName(network);

        final String pathToTrainingConfiguration = Paths.get(modelConfDir,
                networkName + ".conf").toString();
        final ASTConfLangCompilationUnit trainingConfiguration = parseTrainingConfiguration(
                pathToTrainingConfiguration);
        final Scope trainingConfigurationSymbolTable = SymbolTableCreator.createConfLangSymbolTable(
                trainingConfiguration, new GlobalScope(modelPath, new ConfLangLanguage()));
        final ConfigurationSymbol trainingConfigurationSymbol = trainingConfiguration.getConfiguration()
                .getConfigurationSymbol();
        final String pathToPipelineConfiguration = Paths.get(modelConfDir,
                networkName + "_pipeline.conf").toString();
        final ASTConfLangCompilationUnit pipelineConfiguration = parsePipelineConfiguration(
                pathToPipelineConfiguration);
        final Scope pipelineConfigurationSymbolTable = SymbolTableCreator.createConfLangSymbolTable(
                trainingConfiguration, new GlobalScope(modelPath, new ConfLangLanguage()));

//        final ConfigurationValidator configurationValidator = new ConfigurationValidator();
//        configurationValidator.validateTrainingConfiguration(trainingConfigurationSymbol);

        checkCoCos();
        backendSpecificValidations();
        final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics = calculateExecutionSemantics();

        // Load AutoML pipeline configurations
        ASTConfLangCompilationUnit nasConf = this.getNASConfiguration(modelConfDir);
        ASTConfLangCompilationUnit searchSpace = this.getAutoMLConfiguration(modelConfDir,
                "SearchSpace.conf");
        ASTConfLangCompilationUnit hyperparamsOptConf = this.getAutoMLConfiguration(modelConfDir,
                "HyperparameterOpt.conf");
        ASTConfLangCompilationUnit evaluationCriteria = this.getAutoMLConfiguration(modelConfDir,
                "EvaluationCriteria.conf");

        trainingConfiguration.getConfiguration().addSuperConfiguration(nasConf.getConfiguration());

        //backend
        generateBackendArtefactsIntoExperiment();
        final LearningMethod learningMethod = LearningMethod.SUPERVISED;
        createPipeline(learningMethod);
        pipeline.setConfigurationModel(trainingConfiguration);
        pipeline.setPipelineConfiguration(pipelineConfiguration);
        pipeline.setPipelineModelWithExecutionSemantics(pipelineModelWithExecutionSemantics);
        pipeline.setNeuralNetwork(network);
        pipeline.setNetworkName(networkName);
        pipeline.setHyperparamsOptConf(hyperparamsOptConf);
        pipeline.setEvaluationCriteria(evaluationCriteria);

        this.setPipelineSearchSpace(trainingConfiguration, searchSpace);

        final long midTime = System.nanoTime();

        System.out.println("Monti time: " + (midTime - startTime) / 1000000 + "ms");
        executePipeline();

        final long endTime = System.nanoTime();
        System.out.println("Total time: " + (endTime - startTime) / 1000000 + "ms");
        System.out.println("Monti time: " + (midTime - startTime) / 1000000 + "ms");
        System.out.println("AutoML time: " + (endTime - midTime) / 1000000 + "ms");
    }

    private void setPipelineSearchSpace(ASTConfLangCompilationUnit trainingConfiguration, ASTConfLangCompilationUnit searchSpace) {
        String trainConfigName = trainingConfiguration.getConfiguration().getName();
        searchSpace.getConfiguration().setName(trainConfigName);
        pipeline.setSearchSpace(searchSpace);
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

    protected ASTConfLangCompilationUnit getNASConfiguration(String modelDirPath) throws IOException {
        if (Files.exists(Paths.get(modelDirPath, "AdaNet.conf"))) {
            return this.getAutoMLConfiguration(modelDirPath, "AdaNet.conf");
        } else if (Files.exists(Paths.get(modelDirPath, "EfficientNet.conf"))) {
            return this.getAutoMLConfiguration(modelDirPath, "EfficientNet.conf");
        } else {
            throw new FileNotFoundException("No conf file for Neural Architecture Search found.");
        }
    }
}
