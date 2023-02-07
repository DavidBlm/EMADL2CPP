package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import com.google.common.collect.Lists;
import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.automl.hyperparameters.sequential.regression.GaussianProcessRegression;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;
import java.util.*;

public class BayesianOptimization extends SequentialAlgorithm {

    private List<List<Double>> evaluatedConfigs;

    private List<Double> metricValues = new ArrayList<>();
    private int maxCandNumber = 1000;

    private int numRandomIter = 10;

    private double tradeOff = 0.01;

    private String metricType;

    private GaussianProcessRegression gpr = new GaussianProcessRegression();

    private List<Double> acquisition(List<List<Double>> samples) {
        List<Double> eiList = new ArrayList<>();

        for (List<Double> sampleList : samples) {
            double[][] xSample = new double[1][sampleList.size()];
            xSample[0] = this.listToArr(sampleList);
            Map<String, Object> predMap = gpr.predict(xSample);
            double mean = ((double[]) predMap.get("mean"))[0];
            double cov = ((double[][]) predMap.get("cov"))[0][0];
            eiList.add(this.calcExpectedImprovement(mean, cov, this.metricType));
        }
        return eiList;
    }

    private double calcExpectedImprovement(double mean, double cov, String metricType) {
        double std = Math.sqrt(cov);
        if (std == 0) {
            return 0;
        } else {
            NormalDistribution norm = new NormalDistribution();
            double imp = mean - this.currBestEvalMetric - this.tradeOff;
            double z = imp / std;
            double ei = imp * norm.cumulativeProbability(z) + std * norm.density(z);
            if (metricType.equals("accuracy")) {
                return ei;
            } else {
                return -ei;
            }
        }
    }

    private double[][] getConfigsArr() {
        int numConfigs = this.evaluatedConfigs.size();
        int numParams = this.evaluatedConfigs.get(0).size();
        double[][] configsArr = new double[numConfigs][numParams];
        for (int i=0; i < numConfigs; i++) {
            configsArr[i] = this.listToArr(this.evaluatedConfigs.get(i));
        }
        return configsArr;
    }

    private double[] getMetricValsArr() {
        return this.listToArr(this.metricValues);
    }

    private List<Double> compilationUnitToList(ASTConfLangCompilationUnit config) {
        List<Double> configList = new ArrayList<>();
        Map<String, Boolean> params = ASTConfLangCompilationUnitHandler.getAllKeys(config);
        SortedSet<String> sortedParams = new TreeSet<>(params.keySet());
        for (String key : sortedParams) {
            Boolean isNested = params.get(key);
            if (isNested) {
                Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(config, key);
                Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");
                SortedSet<String> sortedNestedParams = new TreeSet<>(nestedMap.keySet());
                for (String nestedKey : sortedNestedParams) {
                    double val = Double.parseDouble(nestedMap.get(nestedKey).toString());
                    configList.add(val);
                }
            } else {
                Object valObj = ASTConfLangCompilationUnitHandler.getValueByKey(config, key);
                double val = Double.parseDouble(valObj.toString());
                configList.add(val);
            }
        }
        return configList;
    }

    private ASTConfLangCompilationUnit listToConfig(List<Double> configList, ASTConfLangCompilationUnit searchSpace) {
        ASTConfLangCompilationUnit config = searchSpace.deepClone();
        Map<String, Boolean> params = ASTConfLangCompilationUnitHandler.getAllKeys(config);

        int i = 0;

        SortedSet<String> sortedParams = new TreeSet<>(params.keySet());
        for (String key : sortedParams) {
            Boolean isNested = params.get(key);

            double val;

            if (isNested) {
                Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(config, key);
                Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");
                SortedSet<String> sortedNestedParams = new TreeSet<>(nestedMap.keySet());
                for (String nestedKey : sortedNestedParams) {
                    val = configList.get(i).doubleValue();
                    config = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(config, key, nestedKey, val);
                    i++;
                }
            } else {
                val = configList.get(i).doubleValue();
                config = ASTConfLangCompilationUnitHandler.setValueForKey(config, key, val);
                i++;
            }
        }

        return config;
    }

    private List<List<Double>> buildCandidates(ASTConfLangCompilationUnit searchSpace, List<List<Double>> sampledList) {
        List<List<Double>> candidatesList;
        List<List<Double>> possibleValList = this.createPossibleValMap(searchSpace);
        int nPossibleConfigs = this.calcNumPossibilities(possibleValList);
        if (nPossibleConfigs <= this.maxCandNumber) {
            candidatesList = Lists.cartesianProduct(possibleValList);
            candidatesList.removeAll(sampledList);
        } else {
            candidatesList = new ArrayList<>();
            for (int i=0; i < this.maxCandNumber; i++) {
                List<Double> candidateConfig = this.createRandomCandidate(possibleValList);
                if (!candidatesList.contains(candidateConfig) && !sampledList.contains(candidateConfig)) {
                    candidatesList.add(candidateConfig);
                }
            }
        }
        return candidatesList;
    }

    private List<Double> createRandomCandidate(List<List<Double>> possibleValList) {
        List<Double> configCandidate = new ArrayList<>();
        Random rand = new Random();
        for (List<Double> valList : possibleValList) {
            double randValue = valList.get(rand.nextInt(valList.size()));
            configCandidate.add(randValue);
        }
        return configCandidate;
    }

    private int calcNumPossibilities(List<List<Double>> possibleValList) {
        int n = 1;
        for (List<Double> valList : possibleValList) {
            n *= valList.size();
        }
        return n;
    }

    private List<List<Double>> createPossibleValMap(ASTConfLangCompilationUnit searchSpace) {
        List<List<Double>> valList = new ArrayList<>();

        Map<String, Boolean> params = ASTConfLangCompilationUnitHandler.getAllKeys(searchSpace);

        SortedSet<String> sortedParams = new TreeSet<>(params.keySet());
        for (String key : sortedParams) {
            Boolean isNested = params.get(key);

            if (isNested) {
                valList.addAll(this.getAllPossibleNestedValues(searchSpace, key));
            } else {
                valList.add(this.getAllPossibleValues(searchSpace, key));
            }
        }

        return valList;
    }

    private List<Double> getAllPossibleValues(ASTConfLangCompilationUnit searchSpace, String key) {
        Object valObj = ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, key);
        return this.createValList(valObj);
    }

    private List<List<Double>> getAllPossibleNestedValues(ASTConfLangCompilationUnit searchSpace, String rootKey) {
        List<List<Double>> possibleNestedValList = new ArrayList<>();

        Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, rootKey);
        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");

        SortedSet<String> sortedNestedParams = new TreeSet<>(nestedMap.keySet());

        for (String nestedKey : sortedNestedParams) {
            Object nestedValObj = nestedMap.get(nestedKey);
            possibleNestedValList.add(this.createValList(nestedValObj));
        }

        return possibleNestedValList;
    }

    private List<Double> createValList(Object valObj) {
        if (valObj instanceof Map) {
            Map<String, Object> valMap = (Map<String, Object>) valObj;
            Object lower = valMap.get("lower");
            Object upper = valMap.get("upper");
            Object stepSize;
            if (valMap.containsKey("step_size")) {
                stepSize = valMap.get("step_size");
            } else {
                stepSize = valMap.get("lower");
            }
            return this.createValsFromRange(lower, upper, stepSize);
        } else {
            return Arrays.asList((double) valObj);
        }
    }

    private List<Double> createValsFromRange(Object lower, Object upper, Object stepSize) {
        BigDecimal lowerDecimal = new BigDecimal(lower.toString());
        BigDecimal upperDecimal = new BigDecimal(upper.toString());
        BigDecimal stepSizeDecimal = new BigDecimal(stepSize.toString());

        List<Double> valList = new ArrayList<>();
        while (lowerDecimal.doubleValue() <= upperDecimal.doubleValue()) {
            valList.add(lowerDecimal.doubleValue());
            lowerDecimal = lowerDecimal.add(stepSizeDecimal);
        }

        return valList;
    }

    public int getNumRandomIter() {
        return numRandomIter;
    }

    public void setNumRandomIter(int numRandomIter) {
        this.numRandomIter = numRandomIter;
    }

    @Override
    public ASTConfLangCompilationUnit getInitialHyperparams(ASTConfLangCompilationUnit searchSpace) {
        this.evaluatedConfigs = this.buildCandidates(searchSpace, new ArrayList<>());
        this.evaluatedConfigs = this.evaluatedConfigs.subList(0, this.numRandomIter);
        List<Double> initConfigList = evaluatedConfigs.get(0);
        ASTConfLangCompilationUnit initialConfig = this.listToConfig(initConfigList, searchSpace);
        return initialConfig;
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {
        if (this.currentIteration == 0) {
            this.setCurrBestHyperparams(hyperParams);
            this.setCurrBestEvalMetric(evalValue);
            this.metricType = metricType;
        } else {
            if (this.updateBest(this.currBestEvalMetric, evalValue, metricType)) {
                this.currBestEvalMetric = evalValue;
                this.currBestHyperparams = hyperParams;
            }
        }

        this.metricValues.add(evalValue);
        if (this.currentIteration >= this.numRandomIter) {
            this.evaluatedConfigs.add(this.compilationUnitToList(hyperParams));
        }

        this.executeIteration();
    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        if (this.currentIteration < this.numRandomIter) {
            List<Double> configList = this.evaluatedConfigs.get(this.currentIteration);
            return this.listToConfig(configList, searchSpace);
        } else {
            gpr.fit(this.getConfigsArr(), this.getMetricValsArr());

            List<List<Double>> samples = this.buildCandidates(searchSpace, this.evaluatedConfigs);
            List<Double> eiList = this.acquisition(samples);

            Double maxEi = Collections.max(eiList);
            int maxId = eiList.indexOf(maxEi);

            List<Double> maxEiConfig = samples.get(maxId);
            return this.listToConfig(maxEiConfig, searchSpace);
        }
    }
}
