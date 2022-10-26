package de.monticore.mlpipelines.workflow;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;

public abstract class AbstractWorkflow {

    //parsing
    public abstract ASTConfLangCompilationUnit parseTrainingConfiguration();
    public abstract ASTConfLangCompilationUnit parsePipelineConfiguration();
    public abstract ASTEMACompilationUnit parsePipelineReferenceModel();

    //pipeline semantics
    public abstract EMAComponentInstanceSymbol addExecutionSemanticsToEmaComponent(EMAComponentInstanceSymbol pipelineReferenceModel);

    //generation
    public abstract void generateBackendArtefacts();
    public abstract void execute();
    public abstract void executePipelineSpecificWorkflow();
    public abstract void readResults();
}
