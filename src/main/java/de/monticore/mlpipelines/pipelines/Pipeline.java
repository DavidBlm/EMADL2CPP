package de.monticore.mlpipelines.pipelines;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;

public abstract class Pipeline {

    protected final LearningMethod learningMethod;

    protected ASTConfLangCompilationUnit trainingConfiguration;

    public void setConfigurationModel(final ASTConfLangCompilationUnit trainingConfiguration) {
        this.trainingConfiguration = trainingConfiguration;
    }

    protected Pipeline(final LearningMethod learningMethod) {
        this.learningMethod = learningMethod;
    }

    public abstract void execute();
}
