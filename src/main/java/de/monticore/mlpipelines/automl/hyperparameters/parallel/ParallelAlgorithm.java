package de.monticore.mlpipelines.automl.hyperparameters.parallel;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;

import java.util.ArrayList;
import java.util.List;

public abstract class ParallelAlgorithm extends AbstractHyperparameterAlgorithm {

    private List<ASTConfLangCompilationUnit> currentPopulation;

    private int populationSize;

    private List<Double> evalValues;

    public List<ASTConfLangCompilationUnit> initializePopulation(ASTConfLangCompilationUnit searchSpace) {
        List<ASTConfLangCompilationUnit> hyperparamsPopulation = new ArrayList<>();

        while (hyperparamsPopulation.size() < this.populationSize) {
            ASTConfLangCompilationUnit hyperparams = this.getInitialHyperparams(searchSpace);
            if (!this.checkHyperparamsInPopulation(hyperparams, hyperparamsPopulation)) {
                hyperparamsPopulation.add(hyperparams);
            }
        }

        return hyperparamsPopulation;
    }

    protected boolean checkHyperparamsInPopulation(ASTConfLangCompilationUnit hyperparams, List<ASTConfLangCompilationUnit> population) {
        for (ASTConfLangCompilationUnit populationEntry : population) {
            if (populationEntry.deepEquals(hyperparams)) {
                return true;
            }
        }
        return false;
    }

    public List<ASTConfLangCompilationUnit> getCurrentPopulation() {
        return this.currentPopulation;
    }

    public void setCurrentPopulation(List<ASTConfLangCompilationUnit> currentPopulation) {
        this.currentPopulation = currentPopulation;
    }

    public List<Double> getEvalValues() {
        return evalValues;
    }

    public void setEvalValues(List<Double> evalValues) {
        this.evalValues = evalValues;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public abstract List<ASTConfLangCompilationUnit> getNewPopulation(ASTConfLangCompilationUnit searchSpace, String metricType);

    public abstract void executeOptimizationStep(List<ASTConfLangCompilationUnit> hyperParamsPopulation, ASTConfLangCompilationUnit searchSpace, List<Double> evalValues, String metricType);
}
