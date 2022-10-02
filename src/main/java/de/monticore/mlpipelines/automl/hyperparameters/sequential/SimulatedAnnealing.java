package de.monticore.mlpipelines.automl.hyperparameters.sequential;

public class SimulatedAnnealing extends SequentialAlgorithm {

    private double temperature;

    public boolean decideAcceptance() {
        boolean accept = true;
        return accept;
    }

    public double decreaseTemperature() {
        double decreasedTemperature = 0;
        return decreasedTemperature;
    }
}
