package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;

import java.util.Map;

public abstract class SequentialAlgorithm extends AbstractHyperparameterAlgorithm {

    private Map<String, Double> currentHyperparameters;

    public Map<String, Double> getCurrentHyperparameters() {
        return this.currentHyperparameters;
    }

    public void setCurrentHyperparameters(Map<String, Double> currentHyperparameters) {
        this.currentHyperparameters = currentHyperparameters;
    }
}
