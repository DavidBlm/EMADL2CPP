package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;

public class SequentialAlgorithm extends AbstractHyperparameterAlgorithm {

    // TODO: Specify datatype
    private double[] currentHyperparameters;

    // TODO: Specify datatype
    public double[] updateHyperparams() {
        double[] updatedHyperparameters = null;
        return updatedHyperparameters;
    }

    public double[] getCurrentHyperparameters() {
        return this.currentHyperparameters;
    }
}
