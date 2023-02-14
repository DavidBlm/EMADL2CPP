package de.monticore.mlpipelines.automl.hyperparameters.parallel;

import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;

public class ParallelAlgorithm extends AbstractHyperparameterAlgorithm {

    // TODO: Specify datatype
    private Object currentPopulation;

    public void initializePopulation() {
        // TODO: Implement method
    }

    // TODO: Specify datatype
    public Object updatePopulation() {
        Object updatedPopulation = null;
        return updatedPopulation;
    }

    public Object getCurrentPopulation() {
        return this.currentPopulation;
    }
}
