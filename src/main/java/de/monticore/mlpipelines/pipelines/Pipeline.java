package de.monticore.mlpipelines.pipelines;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTComponent;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;

public abstract class Pipeline {

    protected final LearningMethod learningMethod;

    protected ASTConfLangCompilationUnit trainingConfiguration;

    protected ASTConfLangCompilationUnit pipelineConfiguration;

    protected ASTComponent neuralNetwork;

    public void setTrainingConfiguration(final ASTConfLangCompilationUnit trainingConfiguration) {
        this.trainingConfiguration = trainingConfiguration;
    }

    public void setPipelineModelWithExecutionSemantics(final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics) {
        this.pipelineModelWithExecutionSemantics = pipelineModelWithExecutionSemantics;
    }

    protected EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics;

    public void setConfigurationModel(final ASTConfLangCompilationUnit trainingConfiguration) {
        this.trainingConfiguration = trainingConfiguration;
    }

    protected Pipeline(final LearningMethod learningMethod) {
        this.learningMethod = learningMethod;
    }

    public abstract void execute();

    public void setPipelineConfiguration(final ASTConfLangCompilationUnit pipelineConfiguration) {
        this.pipelineConfiguration = pipelineConfiguration;
    }
}
