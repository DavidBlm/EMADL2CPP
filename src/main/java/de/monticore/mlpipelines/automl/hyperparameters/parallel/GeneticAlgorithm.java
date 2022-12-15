package de.monticore.mlpipelines.automl.hyperparameters.parallel;

import conflang._ast.ASTConfLangCompilationUnit;

public class GeneticAlgorithm extends ParallelAlgorithm {

    private double mutationConfig;

    private double crossoverConfig;

    public void executeSelection() {
        // TODO: Implement method
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {

    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        return null;
    }
}
