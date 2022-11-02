package de.monticore.mlpipelines.automl.hyperparameters.sequential;

public class SimulatedAnnealing extends SequentialAlgorithm {

    private double initialTemperature;

    private double currentTemperature;

    private double previousEvaluationMetric;

    @Override
    public void executeIteration() {
        this.decreaseTemperature();
        super.executeIteration();
    }

    @Override
    public double[] updateHyperparams() {
        double stepSize = 0.1;

        return  this.getCurrentHyperparameters();
    }

    public boolean decideAcceptance(double evaluationMetric) {
        double diff = evaluationMetric - this.previousEvaluationMetric;
        double criterion = Math.exp((-diff) / this.currentTemperature);

        if ((diff > 0) && Math.random() < criterion) {
            return true;
        } else {
            return false;
        }
    }

    public void decreaseTemperature() {
        this.currentTemperature = this.initialTemperature / (this.currentIteration + 1);
    }
}
