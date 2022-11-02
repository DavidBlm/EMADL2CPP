package de.monticore.mlpipelines.automl.hyperparameters;

public abstract class AbstractHyperparameterAlgorithm {

    protected int currentIteration;

    public void executeIteration() {
        this.currentIteration++;
        // TODO: Implement method
    }
}
