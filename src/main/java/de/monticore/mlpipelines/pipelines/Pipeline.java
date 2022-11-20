package de.monticore.mlpipelines.pipelines;

import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;

public abstract class Pipeline {

    protected final LearningMethod learningMethod;

    protected Pipeline(final LearningMethod learningMethod) {
        this.learningMethod = learningMethod;
    }

    public abstract void execute();
}
