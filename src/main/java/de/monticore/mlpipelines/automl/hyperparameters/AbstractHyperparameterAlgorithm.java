package de.monticore.mlpipelines.automl.hyperparameters;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;

import java.util.List;
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

    protected Object getRangeProperty(ASTConfLangCompilationUnit searchSpace, String key, String rangeType) {
        Object rangePropObj = null;

        Object valObj = ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, key);
        if (valObj instanceof Map) {
            Map<String, Object> rangeMap = (Map<String, Object>) valObj;
            rangePropObj = rangeMap.get(rangeType);
        }

        return rangePropObj;
    }

    protected Object getRangePropForNested(ASTConfLangCompilationUnit searchSpace, String rootKey, String nestedKey, String rangeType) {
        Object rangePropObj = null;

        Map<String, Object> valMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, rootKey);
        Map<String, Object> nestedMap = (Map<String, Object>) valMap.get("nestedMap");
        Object valObj = nestedMap.get(nestedKey);
        if (valObj instanceof Map) {
            Map<String, Object> rangeMap = (Map<String, Object>) valObj;
            rangePropObj = rangeMap.get(rangeType);
        }

        return rangePropObj;
    }

    protected boolean isInteger(Object numberObj) {
        return !numberObj.toString().contains(".");
    }

    protected int createRandInt(int lower, int upper) {
        Random r = new Random();
        return r.nextInt((upper - lower) + 1) + lower;
    }

    protected double createRandDouble(double lower, double upper) {
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
        double newValue = (double) this.keepValInRange(newValCandidate, lower, upper);
        return newValue;
    }

    protected Object addValObj(Object val1, Object val2, Object lower, Object upper) {
        Object sum;
        if (this.isInteger(val1) && this.isInteger(val2)) {
            int val1Int = Integer.parseInt(val1.toString());
            int val2Int = Integer.parseInt(val2.toString());
            sum = val1Int + val2Int;
        } else {
            double val1Double = Double.parseDouble(val1.toString());
            double val2Double = Double.parseDouble(val2.toString());
            sum = val1Double + val2Double;
        }

        if ((lower != null) && (upper != null)) {
            sum = this.keepValInRange(sum, lower, upper);
        }

        return sum;
    }

    protected Object subValObj(Object val1, Object val2, Object lower, Object upper) {
        Object diff;
        if (this.isInteger(val1) && this.isInteger(val2)) {
            int val1Int = Integer.parseInt(val1.toString());
            int val2Int = Integer.parseInt(val2.toString());
            diff = val1Int - val2Int;
        } else {
            double val1Double = Double.parseDouble(val1.toString());
            double val2Double = Double.parseDouble(val2.toString());
            diff = val1Double - val2Double;
        }

        if ((lower != null) && (upper != null)) {
            diff = this.keepValInRange(diff, lower, upper);
        }

        return diff;
    }

    private Object keepValInRange(Object val, Object lower, Object upper) {
        Object valInRange;
        if (this.isInteger(val) && this.isInteger(lower) && this.isInteger(upper)) {
            int valInt = Integer.parseInt(val.toString());
            int lowerInt = Integer.parseInt(lower.toString());
            int upperInt = Integer.parseInt(upper.toString());
            valInRange = Math.max(valInt, lowerInt);
            valInRange = Math.min((Integer) valInRange, upperInt);
        } else {
            double valDouble = Double.parseDouble(val.toString());
            double lowerDouble = Double.parseDouble(lower.toString());
            double upperDouble = Double.parseDouble(upper.toString());
            valInRange = Math.max(valDouble, lowerDouble);
            valInRange = Math.min((double) valInRange, upperDouble);
        }
        return valInRange;
    }

    protected double[] listToArr(List<Double> doubleList) {
        double[] doubleArr = new double[doubleList.size()];
        for (int i=0; i < doubleArr.length; i++) {
            doubleArr[i] = doubleList.get(i);
        }
        return doubleArr;
    }

    public abstract void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType);

    public abstract ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace);
}
