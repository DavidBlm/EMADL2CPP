package de.monticore.mlpipelines.automl;

public class TrainAlgorithmConfig {
    //private class TrainAlgorithm;
    private int numEpochs;
    private boolean saveTrainedArchitecture;
    private String architectureSavePath;

    public TrainAlgorithmConfig(int numEpochs, boolean saveTrainedArchitecture, String architectureSavePath) {
        this.numEpochs = numEpochs;
        this.saveTrainedArchitecture = saveTrainedArchitecture;
        this.architectureSavePath = architectureSavePath;
    }

    public int getNumEpochs() {
        return numEpochs;
    }

    public boolean isSaveTrainedArchitecture() {
        return saveTrainedArchitecture;
    }

    public String getArchitectureSavePath() {
        return architectureSavePath;
    }
}
