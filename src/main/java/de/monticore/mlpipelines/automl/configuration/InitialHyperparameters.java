package de.monticore.mlpipelines.automl.configuration;

import java.util.Hashtable;

public class InitialHyperparameters {

    private int numEpochs;

    private int batchSize;

    private boolean normalize;

    private String context;

    private boolean loadCheckpoint;

    private Hashtable<String, Object> optimizer;

    public InitialHyperparameters() {
    }

    public InitialHyperparameters(
            int numEpochs,
            int batchSize,
            boolean normalize,
            String context,
            boolean loadCheckpoint,
            Hashtable<String, Object> optimizer) {
        this.numEpochs = numEpochs;
        this.batchSize = batchSize;
        this.normalize = normalize;
        this.context = context;
        this.loadCheckpoint = loadCheckpoint;
        this.optimizer = optimizer;
    }

    public int getNumEpochs() {
        return numEpochs;
    }

    public void setNumEpochs(int numEpochs) {
        this.numEpochs = numEpochs;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public boolean isNormalize() {
        return normalize;
    }

    public void setNormalize(boolean normalize) {
        this.normalize = normalize;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public boolean isLoadCheckpoint() {
        return loadCheckpoint;
    }

    public void setLoadCheckpoint(boolean loadCheckpoint) {
        this.loadCheckpoint = loadCheckpoint;
    }

    public Hashtable<String, Object> getOptimizer() {
        return optimizer;
    }

    public void setOptimizer(Hashtable<String, Object> optimizer) {
        this.optimizer = optimizer;
    }
}
