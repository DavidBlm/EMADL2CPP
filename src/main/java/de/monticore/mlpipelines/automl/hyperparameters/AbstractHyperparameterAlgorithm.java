package de.monticore.mlpipelines.automl.hyperparameters;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;

import java.util.Map;
import java.util.Random;

public abstract class AbstractHyperparameterAlgorithm {

    protected int currentIteration = 0;

    protected ASTConfLangCompilationUnit currBestHyperparams;

    protected double currBestEvalMetric;

    public void executeIteration() {
        this.currentIteration++;
    }

    public int getCurrentIteration() {
        return currentIteration;
    }

    public void setCurrentIteration(int currentIteration) {
        this.currentIteration = currentIteration;
    }

    public ASTConfLangCompilationUnit getCurrBestHyperparams() {
        return currBestHyperparams;
    }

    public void setCurrBestHyperparams(ASTConfLangCompilationUnit currBestHyperparams) {
        this.currBestHyperparams = currBestHyperparams;
    }

    public double getCurrBestEvalMetric() {
        return currBestEvalMetric;
    }

    public void setCurrBestEvalMetric(double currBestEvalMetric) {
        this.currBestEvalMetric = currBestEvalMetric;
    }

    public ASTConfLangCompilationUnit getInitialHyperparams(ASTConfLangCompilationUnit searchSpace) {
        ASTConfLangCompilationUnit initialHyperparams = searchSpace.deepClone();
        Map<String, Boolean> params = ASTConfLangCompilationUnitHandler.getAllKeys(searchSpace);

        for (Map.Entry<String, Boolean> entry : params.entrySet()) {
            String key = entry.getKey();
            Boolean isNested = entry.getValue();
            if (isNested) {
                initialHyperparams = this.getInitHyperparamForNestedKey(initialHyperparams, key);
            } else {
                initialHyperparams = this.getInitHyperparamForKey(initialHyperparams, key);
            }
        }

        return initialHyperparams;
    }

    private ASTConfLangCompilationUnit getInitHyperparamForKey(ASTConfLangCompilationUnit initialHyperparams, String key) {
        Object value = ASTConfLangCompilationUnitHandler.getValueByKey(initialHyperparams, key);
        if (value instanceof Map) {
            Object newValue = this.createValueFromRange((Map<String, Object>) value);
            initialHyperparams = ASTConfLangCompilationUnitHandler.setValueForKey(initialHyperparams, key, newValue);
        }
        return initialHyperparams;
    }

    private ASTConfLangCompilationUnit getInitHyperparamForNestedKey(ASTConfLangCompilationUnit initialHyperparams, String rootKey) {
        Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(initialHyperparams, rootKey);
        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");
        for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
            String nestedKey = nestedEntry.getKey();
            Object nestedValue = nestedEntry.getValue();
            if (nestedValue instanceof Map) {
                Object newNestedValue = this.createValueFromRange((Map<String, Object>) nestedValue);
                initialHyperparams = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(initialHyperparams, rootKey, nestedKey, newNestedValue);
            }
        }
        return initialHyperparams;
    }

    protected Object createValueFromRange(Map<String, Object> rangeMap) {
        Object newValue;

        Object lower = rangeMap.get("lower");
        Object upper = rangeMap.get("upper");

        if (this.isInteger(lower) && this.isInteger(upper)) {
            int lowerInt = Integer.parseInt(lower.toString());
            int upperInt = Integer.parseInt(upper.toString());
            newValue = this.createRandInt(lowerInt, upperInt);
        } else {
            double lowerDouble = Double.parseDouble(lower.toString());
            double upperDouble = Double.parseDouble(upper.toString());
            newValue = this.createRandDouble(lowerDouble, upperDouble);
        }

        return newValue;
    }

    protected Object createValueFromStep(Map<String, Object> rangeMap, Object currentVal) {
        Object newValue;

        Object lower = rangeMap.get("lower");
        Object upper = rangeMap.get("upper");
        Object stepSize = rangeMap.get("step_size");

        if (this.isInteger(lower) && this.isInteger(upper) && this.isInteger(stepSize) && this.isInteger(currentVal)) {
            int lowerInt = Integer.parseInt(lower.toString());
            int upperInt = Integer.parseInt(upper.toString());
            int stepSizeInt = Integer.parseInt(stepSize.toString());
            int currentInt = Integer.parseInt(currentVal.toString());
            newValue = this.createIntFromStep(lowerInt, upperInt, stepSizeInt, currentInt);
        } else {
            double lowerDouble = Double.parseDouble(lower.toString());
            double upperDouble = Double.parseDouble(upper.toString());
            double stepSizeDouble = Double.parseDouble(stepSize.toString());
            double currentDouble = Double.parseDouble(currentVal.toString());
            newValue = this.createDoubleFromStep(lowerDouble, upperDouble, stepSizeDouble, currentDouble);
        }

        return newValue;
    }

    private boolean isInteger(Object numberObj) {
        double doubleVal = Double.parseDouble(numberObj.toString());
        int intVal = (int) doubleVal;
        return  (doubleVal == intVal);
    }

    private int createRandInt(int lower, int upper) {
        Random r = new Random();
        return r.nextInt((upper - lower) + 1) + lower;
    }
    private double createRandDouble(double lower, double upper) {
        Random r = new Random();
        return lower + (upper - lower) * r.nextDouble();
    }

    private int createIntFromStep(int lower, int upper, int stepSize, int currentVal) {
        int stepInt = this.createRandInt(-stepSize, stepSize);
        int newValCandidate = currentVal + stepInt;
        int newValue = Math.max(newValCandidate, lower);
        newValue = Math.min(newValue, upper);
        return newValue;
    }

    private double createDoubleFromStep(double lower, double upper, double stepSize, double currentVal) {
        double stepDouble = this.createRandDouble(-stepSize, stepSize);
        double newValCandidate = currentVal + stepDouble;
        double newValue = Math.max(newValCandidate, lower);
        newValue = Math.min(newValue, upper);
        return newValue;
    }

    public abstract void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType);

    public abstract ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace);
}
