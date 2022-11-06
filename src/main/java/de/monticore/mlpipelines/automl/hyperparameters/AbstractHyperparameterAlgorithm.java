package de.monticore.mlpipelines.automl.hyperparameters;

public abstract class AbstractHyperparameterAlgorithm {

    protected int currentIteration = 0;

    public void executeIteration() {
        this.currentIteration++;
    }
}
