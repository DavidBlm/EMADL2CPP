package de.monticore.mlpipelines.workflow;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.io.paths.ModelPath;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.lang.monticar.emadl._symboltable.EMADLLanguage;
import de.monticore.lang.monticar.semantics.Constants;
import de.monticore.lang.monticar.semantics.ExecutionSemantics;
import de.monticore.lang.monticar.semantics.construct.SymtabCreator;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.mlpipelines.backend.generation.MontiAnnaGenerator;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.parsing.ConfigurationLanguageParser;
import de.monticore.parsing.EMADLParser;
import de.monticore.symbolmanagement.SymbolTableCreator;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

public abstract class AbstractWorkflow {
    private final MontiAnnaContext montiAnnaContext = MontiAnnaContext.getInstance();
    private final String parentModelPath = montiAnnaContext.getParentModelPath();
    private final String rootModelName = montiAnnaContext.getRootModelName();

    private MontiAnnaGenerator montiAnnaGenerator;

    private Pipeline pipeline;

    public void setMontiAnnaGenerator(final MontiAnnaGenerator montiAnnaGenerator) {
        this.montiAnnaGenerator = montiAnnaGenerator;
    }

    public void setPipeline(final Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public final void execute() throws IOException {
        // frontend
        parseTrainingConfiguration(parentModelPath + rootModelName + ".conf");
        parsePipelineConfiguration(parentModelPath + rootModelName + "Pipeline.conf");
        createSymbolTable();

        checkCoCos();
        validateConfigurationAgainstSchemas(); // depends on learning method
        backendSpecificValidations();
        //backend
        calculateExecutionSemantics();
        generateBackendArtefactsIntoExperiment(); //predictor,  "network" ,
        final LearningMethod learningMethod = LearningMethod.SUPERVISED;
        createPipeline(learningMethod);
        executePipeline();
        //  pipeline.readresults()
    }

    private void executePipeline() {
        this.pipeline.execute();
    }

    //TODO separating model path from model name ?
    public ASTConfLangCompilationUnit parseTrainingConfiguration(final String pathToTrainingConfiguration)
            throws IOException {
        return new ConfigurationLanguageParser().parseModelOrThrowException(pathToTrainingConfiguration);
    }

    public ASTConfLangCompilationUnit parsePipelineConfiguration(final String pathToPipelineConfiguration)
            throws IOException {
        return new ConfigurationLanguageParser().parseModelOrThrowException(pathToPipelineConfiguration);
    }


    private void createSymbolTable() {

    }

    private void checkCoCos() {

    }

    private void validateConfigurationAgainstSchemas() {
    }

    private void backendSpecificValidations() {
    }

    private void calculateExecutionSemantics() throws IOException {
        //TODO get from schema
        final String pathToPipelineReferenceModel = montiAnnaContext.getPipelineReferenceModelsPath() + " PIPELINE NAME";
        final EMAComponentInstanceSymbol pipelineReferenceModel = parsePipelineReferenceModelToEMAComponent(
                pathToPipelineReferenceModel);
        final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics = addExecutionSemanticsToEmaComponent(
                pipelineReferenceModel);
    }

    //    public abstract void generateBackendArtefacts();

    public void generateBackendArtefactsIntoExperiment() {
       montiAnnaGenerator.generateTargetBackendArtefacts();
    }

    public abstract void createPipeline(final LearningMethod learningMethod);

    public EMAComponentInstanceSymbol parsePipelineReferenceModelToEMAComponent(final String pathToPipeline)
            throws IOException {
        final ASTEMACompilationUnit astemaCompilationUnit = new EMADLParser().parseModelOrThrowException(
                pathToPipeline);
        final ModelPath modelPath = new ModelPath(Paths.get("src/test/resources/models/pipeline"));
        final Scope pipelineSymbolTable = SymbolTableCreator.createEMADLSymbolTable(astemaCompilationUnit,
                new GlobalScope(modelPath, new EMADLLanguage()));
        final String pipelineName = astemaCompilationUnit.getComponent().getName();
        final Optional<EMAComponentInstanceSymbol> emaInstanceComponent = pipelineSymbolTable.resolve(pipelineName,
                EMAComponentInstanceSymbol.KIND);
        return emaInstanceComponent.orElseThrow(IllegalStateException::new);
    }

    public EMAComponentInstanceSymbol addExecutionSemanticsToEmaComponent(EMAComponentInstanceSymbol pipelineReferenceModel) {
        // TODO FMU substitute tagging resolver in the Execution Semantics project
        final TaggingResolver symTab = SymtabCreator.createSymTab("src/test/resources", "src/main/resources",
                Constants.SYNTHESIZED_COMPONENTS_ROOT);
        new ExecutionSemantics(symTab, pipelineReferenceModel).addExecutionSemantics();
        return pipelineReferenceModel;
    }
}
