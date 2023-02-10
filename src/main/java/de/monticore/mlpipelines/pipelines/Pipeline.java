package de.monticore.mlpipelines.pipelines;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;

public abstract class Pipeline {

    protected final LearningMethod learningMethod;

    protected ASTConfLangCompilationUnit trainingConfiguration;

    protected ASTConfLangCompilationUnit pipelineConfiguration;

    protected EMAComponentInstanceSymbol neuralNetwork;

    protected EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics;

    protected Pipeline(final LearningMethod learningMethod) {
        this.learningMethod = learningMethod;
    }

    public void setNeuralNetwork(final EMAComponentInstanceSymbol neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
    }

    public void setTrainingConfiguration(final ASTConfLangCompilationUnit trainingConfiguration) {
        this.trainingConfiguration = trainingConfiguration;
    }

    public void setPipelineModelWithExecutionSemantics(final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics) {
        this.pipelineModelWithExecutionSemantics = pipelineModelWithExecutionSemantics;
    }

    public void setConfigurationModel(final ASTConfLangCompilationUnit trainingConfiguration) {
        this.trainingConfiguration = trainingConfiguration;
    }

    public abstract void execute();

    public void setPipelineConfiguration(final ASTConfLangCompilationUnit pipelineConfiguration) {
        this.pipelineConfiguration = pipelineConfiguration;
    }

    public abstract float getTrainedAccuracy();
}
