package de.monticore.mlpipelines.automl.configuration;

public class EvaluationConfig {

    private String metric;

    private double acceptanceRate;

    public EvaluationConfig() {
    }

    public EvaluationConfig(String metric, double acceptanceRate) {
        this.metric = metric;
        this.acceptanceRate = acceptanceRate;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }
}
