package de.monticore.mlpipelines.automl.hyperparameters.parallel;

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
}
