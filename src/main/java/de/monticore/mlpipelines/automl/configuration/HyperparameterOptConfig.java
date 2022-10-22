package de.monticore.mlpipelines.automl.configuration;

public class HyperparameterOptConfig {

    private String optimizer;

    public HyperparameterOptConfig() {
    }

    public HyperparameterOptConfig(String optimizer) {
        this.optimizer = optimizer;
    }

    public String getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(String optimizer) {
        this.optimizer = optimizer;
    }
}
