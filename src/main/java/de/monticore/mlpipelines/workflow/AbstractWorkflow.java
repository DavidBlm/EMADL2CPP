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
import de.monticore.mlpipelines.validation.ConfigurationValidator;
import de.monticore.parsing.ConfigurationLanguageParser;
import de.monticore.parsing.EMADLParser;
import de.monticore.symbolmanagement.SymbolTableCreator;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.StringTransformations;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class AbstractWorkflow {
    private final MontiAnnaContext montiAnnaContext;
    protected MontiAnnaGenerator montiAnnaGenerator;

    private Pipeline pipeline;

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

    public final void execute() throws IOException {
        // frontend
        final String rootModelName = Names.getSimpleName(montiAnnaContext.getRootModelName());
        final String pathToModelsDirectory = Paths.get(montiAnnaContext.getParentModelPath().toString(),
                getDirectoryPathSupplementFromComponentName(montiAnnaContext.getRootModelName())).toString();
        final String pathToRootModel = Paths.get(pathToModelsDirectory, rootModelName + ".emadl").toString();
        final ASTEMACompilationUnit rootEMADLComponent = new EMADLParser().parseModelOrThrowException(pathToRootModel);
        final ModelPath modelPath = new ModelPath(Paths.get(montiAnnaContext.getParentModelPath().toString()));
        final Scope emadlSymbolTable = SymbolTableCreator.createEMADLSymbolTable(rootEMADLComponent,
                new GlobalScope(modelPath, new EMADLLanguage()));
        final EMAComponentInstanceSymbol network = getNetworkTobeTrained(rootEMADLComponent, emadlSymbolTable);
        final String fullNetworkNameReplacedWithUnderscores = network.getFullName().replace(".", "_");
        final String pathToTrainingConfiguration = Paths.get(pathToModelsDirectory,
                fullNetworkNameReplacedWithUnderscores + ".conf").toString();
        final ASTConfLangCompilationUnit trainingConfiguration = parseTrainingConfiguration(
                pathToTrainingConfiguration);
        final Scope trainingConfigurationSymbolTable = SymbolTableCreator.createConfLangSymbolTable(
                trainingConfiguration, new GlobalScope(modelPath, new ConfLangLanguage()));
        final ConfigurationSymbol trainingConfigurationSymbol = trainingConfiguration.getConfiguration()
                .getConfigurationSymbol();
        final String pathToPipelineConfiguration = Paths.get(pathToModelsDirectory,
                fullNetworkNameReplacedWithUnderscores + "_pipeline.conf").toString();
        final ASTConfLangCompilationUnit pipelineConfiguration = parsePipelineConfiguration(
                pathToPipelineConfiguration);
        final Scope pipelineConfigurationSymbolTable = SymbolTableCreator.createConfLangSymbolTable(
                trainingConfiguration, new GlobalScope(modelPath, new ConfLangLanguage()));

        final ConfigurationValidator configurationValidator = new ConfigurationValidator();
        configurationValidator.validateTrainingConfiguration(trainingConfigurationSymbol);

        checkCoCos();
        backendSpecificValidations();
        final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics = calculateExecutionSemantics();
        //backend
        generateBackendArtefactsIntoExperiment();
        final LearningMethod learningMethod = LearningMethod.SUPERVISED;
        createPipeline(learningMethod);
        pipeline.setConfigurationModel(trainingConfiguration);
        pipeline.setPipelineConfiguration(pipelineConfiguration);
        pipeline.setPipelineModelWithExecutionSemantics(pipelineModelWithExecutionSemantics);
        pipeline.setNeuralNetwork(network);
        executePipeline();
    }

    private String getDirectoryPathSupplementFromComponentName(final String rootModelName) {
        final int lastIndexOfDot = rootModelName.lastIndexOf(".");
        if (lastIndexOfDot == -1)
            return rootModelName;
        return rootModelName.substring(0, lastIndexOfDot).replace(".", "/");
    }

    private static EMAComponentInstanceSymbol getNetworkTobeTrained(
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
        return new ConfigurationLanguageParser().parseModelOrThrowException(pathToTrainingConfiguration);
    }

    public ASTConfLangCompilationUnit parsePipelineConfiguration(final String pathToPipelineConfiguration)
            throws IOException {
        return new ConfigurationLanguageParser().parseModelOrThrowException(pathToPipelineConfiguration);
    }

    //TODO implement me
    private void checkCoCos() {

    }

    //TODO implement me
    private void backendSpecificValidations() {
    }

    private EMAComponentInstanceSymbol calculateExecutionSemantics() throws IOException {
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

    private void executePipeline() {
        this.pipeline.execute();
    }

    public EMAComponentInstanceSymbol parsePipelineReferenceModelToEMAComponent(final String pathToPipeline)
            throws IOException {
        final ASTEMACompilationUnit astemaCompilationUnit = new EMADLParser().parseModelOrThrowException(
                pathToPipeline);
        final ModelPath modelPath = new ModelPath(Paths.get("src/test/resources/models/pipeline"));
        final Scope pipelineSymbolTable = SymbolTableCreator.createEMADLSymbolTable(astemaCompilationUnit,
                new GlobalScope(modelPath, new EMADLLanguage()));
        final String pipelineName = astemaCompilationUnit.getComponent().getName();
        final Optional<EMAComponentInstanceSymbol> emaInstanceComponent = pipelineSymbolTable.resolve(
                StringTransformations.uncapitalize(pipelineName),
                EMAComponentInstanceSymbol.KIND);
        return emaInstanceComponent.orElseThrow(IllegalStateException::new);
    }

    public EMAComponentInstanceSymbol addExecutionSemanticsToEmaComponent(EMAComponentInstanceSymbol pipelineReferenceModel) {
        final TaggingResolver symTab = SymtabCreator.createSymTab("src/test/resources", "src/main/resources",
                Constants.SYNTHESIZED_COMPONENTS_ROOT);
        new ExecutionSemantics(symTab, pipelineReferenceModel).addExecutionSemantics();
        return pipelineReferenceModel;
    }
}
