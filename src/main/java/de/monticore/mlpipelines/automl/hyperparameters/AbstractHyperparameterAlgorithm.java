package de.monticore.mlpipelines.automl.hyperparameters;

public abstract class AbstractHyperparameterAlgorithm {

    protected int currentIteration = 0;

    public void executeIteration() {
        this.currentIteration++;
    }

    public int getCurrentIteration() {
        return currentIteration;
    }

    public void setCurrentIteration(int currentIteration) {
        this.currentIteration = currentIteration;
    }
}
