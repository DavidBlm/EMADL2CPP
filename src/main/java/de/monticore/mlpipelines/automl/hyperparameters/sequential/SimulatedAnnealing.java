package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import java.util.*;

public class SimulatedAnnealing extends SequentialAlgorithm {

    private double initialTemperature;

    private double currentTemperature;

    private double currEvalMetric;

    private Map<String, Double> currBestHyperparams;
    private double currBestEvalMetric;

    private Map<String, Double> stepSizeMap = new HashMap<String, Double>() {{
        put("num_epoch", 1.0);
        put("batch_size", 2.0);
        put("learning_rate", 0.0005);
        put("learning_rate_decay", 0.1);
        put("step_size", 100.0);
        put("weight_decay", 0.01);
    }};

    private List<String> intKeyList = new ArrayList<String>() {{
        add("num_epoch");
        add("batch_size");
    }};

    public void executeOptimizationStep(Map<String, Double> hyperParams, Double evalValue, String metricType) {
        if (this.currentIteration == 0) {
            this.currBestHyperparams = hyperParams;
            this.currBestEvalMetric = evalValue;

            this.setCurrentHyperparameters(hyperParams);
            this.currEvalMetric = this.currBestEvalMetric;
        } else {
            if (this.updateBest(this.currBestEvalMetric, evalValue, metricType)) {
                this.currBestEvalMetric = evalValue;
                this.currBestHyperparams = hyperParams;
            }

            if (this.decideAcceptance(evalValue)) {
                this.setCurrentHyperparameters(hyperParams);
                this.currEvalMetric = evalValue;
            }
        }

        super.executeIteration();
        this.decreaseTemperature();
    }

    public Map<String, Double> getNewHyperparamsCandidate() {
        Random r = new Random();
        Map<String, Double> candidateMap = new HashMap<>();
        for (Map.Entry<String, Double> entry : this.getCurrentHyperparameters().entrySet()) {
            String parameterKey = entry.getKey();
            double stepSize = this.stepSizeMap.get(parameterKey);
            double candidateValue = entry.getValue() + (r.nextGaussian() * stepSize);
            if (intKeyList.contains(parameterKey)) {
                candidateValue = (int) candidateValue;
            }
            candidateMap.put(entry.getKey(), candidateValue);
        }
        return candidateMap;
    }

    private boolean updateBest(double currValue, double newValue, String metricType) {
        if (metricType.equals("Accuracy")) {
            return newValue > currValue;
        }
        else {
            return newValue < currValue;
        }
    }

    private boolean decideAcceptance(double evaluationMetric) {
        double diff = evaluationMetric - this.currEvalMetric;
        double criterion = Math.exp((-diff) / this.currentTemperature);

        return (diff > 0) && Math.random() < criterion;
    }

    private void decreaseTemperature() {
        this.currentTemperature = this.initialTemperature / (this.currentIteration + 1);
    }

    public Map<String, Double> getCurrBestHyperparams() {
        return currBestHyperparams;
    }

    public void setCurrBestHyperparams(Map<String, Double> currBestHyperparams) {
        this.currBestHyperparams = currBestHyperparams;
    }

    public double getCurrBestEvalMetric() {
        return currBestEvalMetric;
    }

    public void setCurrBestEvalMetric(double currBestEvalMetric) {
        this.currBestEvalMetric = currBestEvalMetric;
    }

    public void setInitialTemperature(double initialTemperature) {
        this.initialTemperature = initialTemperature;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    public double getCurrEvalMetric() {
        return currEvalMetric;
    }

    public void setCurrEvalMetric(double currEvalMetric) {
        this.currEvalMetric = currEvalMetric;
    }
}
