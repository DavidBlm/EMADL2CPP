package de.monticore.mlpipelines.workflow;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.io.paths.ModelPath;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch.generator.CNNArchGenerator;
import de.monticore.lang.monticar.emadl._symboltable.EMADLLanguage;
import de.monticore.lang.monticar.semantics.Constants;
import de.monticore.lang.monticar.semantics.ExecutionSemantics;
import de.monticore.lang.monticar.semantics.construct.SymtabCreator;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
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
    private final MontiAnnaContext montiAnnaConfiguration = MontiAnnaContext.getInstance();

    //parsing steps
    public ASTConfLangCompilationUnit parseTrainingConfiguration(final String pathToTrainingConfiguration) throws IOException {
        return new ConfigurationLanguageParser().parseModelOrThrowException(pathToTrainingConfiguration);
    }

    public ASTConfLangCompilationUnit parsePipelineConfiguration(final String pathToPipelineConfiguration) throws IOException {
        return new ConfigurationLanguageParser().parseModelOrThrowException(pathToPipelineConfiguration);
    }

    //TODO separating model path from model name ?
    public EMAComponentInstanceSymbol parsePipelineReferenceModelToEMAComponent(final String pathToPipeline) throws IOException {
        final ASTEMACompilationUnit astemaCompilationUnit = new EMADLParser().parseModelOrThrowException(pathToPipeline);
        final ModelPath modelPath = new ModelPath(Paths.get("src/test/resources/models/pipeline"));
        final Scope pipelineSymbolTable = SymbolTableCreator.createEMADLSymbolTable(astemaCompilationUnit, new GlobalScope(modelPath, new EMADLLanguage()));
        final String pipelineName = astemaCompilationUnit.getComponent().getName();
        final Optional<EMAComponentInstanceSymbol> emaInstanceComponent = pipelineSymbolTable.resolve(pipelineName, EMAComponentInstanceSymbol.KIND);
        return emaInstanceComponent.orElseThrow(IllegalStateException::new);
    }

    public EMAComponentInstanceSymbol addExecutionSemanticsToEmaComponent(EMAComponentInstanceSymbol pipelineReferenceModel) {
        // TODO FMU substitute tagging resolver in the Execution Semantics project
        final TaggingResolver symTab = SymtabCreator.createSymTab("src/test/resources", "src/main/resources",
                Constants.SYNTHESIZED_COMPONENTS_ROOT);
        new ExecutionSemantics(symTab, pipelineReferenceModel).addExecutionSemantics();
        return pipelineReferenceModel;
    }

    public void generateBackendArtefacts(final String generationTargetPath) {
        final CNNArchGenerator cnnArchGenerator = montiAnnaConfiguration.getTargetBackend().getCNNArchGenerator();
        cnnArchGenerator.setGenerationTargetPath(generationTargetPath);
        cnnArchGenerator.generate(Paths.get(montiAnnaConfiguration.getParentModelPath()), montiAnnaConfiguration.getRootModelName());
    }

    //    public abstract void generateBackendArtefacts();
    public abstract void executePipelineSpecificWorkflow();

    public abstract void readResults();

    public void execute() throws IOException {
        // frontend
        final String parentModelPath = montiAnnaConfiguration.getParentModelPath();
        final String rootModelName = montiAnnaConfiguration.getRootModelName();
        parseTrainingConfiguration(parentModelPath + rootModelName + ".conf");
        parsePipelineConfiguration(parentModelPath + rootModelName + "Pipeline.conf");
        //symbol table creation

        //calculate execution semantics
        //TODO get from schema
        final String pathToPipelineReferenceModel = montiAnnaConfiguration.getPipelineReferenceModelsPath() + " PIPELINE NAME";
        final EMAComponentInstanceSymbol pipelineReferenceModel = parsePipelineReferenceModelToEMAComponent(pathToPipelineReferenceModel);
        final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics = addExecutionSemanticsToEmaComponent(pipelineReferenceModel);


        //check cocos

        // validate configurations against schemas : depends on learning method

        // backend-specific validations


        //backend
        //generate backend artefacts: predictor,  "network" ,
        //extract and write artefacts in appropriate locations
        // generate python training configuration: default if not available
        //select schema api: default supervised, depends on learning method

        executePipelineSpecificWorkflow();


    }
}
