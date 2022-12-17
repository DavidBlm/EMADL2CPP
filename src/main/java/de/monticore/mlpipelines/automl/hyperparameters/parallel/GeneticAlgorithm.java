package de.monticore.mlpipelines.automl.hyperparameters.parallel;

import conflang._ast.ASTConfLangCompilationUnit;
import groovy.util.MapEntry;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Stream;

public class GeneticAlgorithm extends ParallelAlgorithm {

    private double mutationConfig;

    private double crossoverConfig;

    private double selectionRate;

    public List<ASTConfLangCompilationUnit> executeSelection(List<ASTConfLangCompilationUnit> hyperParamsPopulation, List<Double> evalValues, String metricType) {
        int selectNum = (int) (hyperParamsPopulation.size() * this.selectionRate);
        selectNum = Math.max(selectNum, 2);

        List<ASTConfLangCompilationUnit> nBestConfigs = this.selectBestConfigs(hyperParamsPopulation, evalValues, selectNum, metricType);

        return nBestConfigs;
    }

    private List<ASTConfLangCompilationUnit> selectBestConfigs(List<ASTConfLangCompilationUnit> population, List<Double> evalValues, int numConfigs, String metricType) {
        List<ASTConfLangCompilationUnit> nBestConfigs = new ArrayList<>();
        List<Double> sortedEvalValues = new ArrayList<>(evalValues);
        Collections.sort(sortedEvalValues);

        if (metricType.equals("Accuracy")) {
            Collections.reverse(sortedEvalValues);
        }

        for (Double value : sortedEvalValues.subList(0, numConfigs)) {
            int index = evalValues.indexOf(value);
            ASTConfLangCompilationUnit config = population.get(index);
            nBestConfigs.add(config);
        }

        return nBestConfigs;
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {

    }

    @Override
    public void executeOptimizationStep(List<ASTConfLangCompilationUnit> hyperParamsPopulation, ASTConfLangCompilationUnit searchSpace, List<Double> evalValues, String metricType) {
        this.setCurrentPopulation(hyperParamsPopulation);
        this.setEvalValues(evalValues);
    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        return null;
    }

    @Override
    public List<ASTConfLangCompilationUnit> getNewPopulation() {
        return null;
    }
}
