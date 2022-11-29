package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import de.monticore.mlpipelines.automl.hyperparameters.HyperparameterAlgorithm;

public class SequentialAlgorithm extends HyperparameterAlgorithm {

    // TODO: Specify datatype
    private Object currentHyperparameters;

    // TODO: Specify datatype
    public Object updateHyperparams() {
        Object updatedHyperparameters = null;
        return updatedHyperparameters;
    }

    public Object getCurrentHyperparameters() {
        return this.currentHyperparameters;
    }
}
