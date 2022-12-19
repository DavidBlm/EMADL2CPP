package de.monticore.mlpipelines.automl.hyperparameters.parallel;

import conflang._ast.ASTConfLangCompilationUnit;

import java.util.List;

public class ParticleSwarmOptimization extends ParallelAlgorithm {

    private List pbestList;

    // TODO: Specify datatype
    private Object gbest;

    private List velocityList;

    private List positionsList;

    private double learningFactors;

    public List updatePbestList() {
        List updatedPbestList = null;
        return updatedPbestList;
    }

    public Object updateGbest() {
        Object updatedGbest = null;
        return updatedGbest;
    }

    public List updateVelocities() {
        List updatedVelocities = null;
        return updatedVelocities;
    }

    public List updatePositions() {
        List updatedPositions = null;
        return updatedPositions;
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {

    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        return null;
    }

    @Override
    public List<ASTConfLangCompilationUnit> getNewPopulation(ASTConfLangCompilationUnit searchSpace, String metricType) {
        return null;
    }

    @Override
    public void executeOptimizationStep(List<ASTConfLangCompilationUnit> hyperParamsPopulation, ASTConfLangCompilationUnit searchSpace, List<Double> evalValues, String metricType) {

    }
}
