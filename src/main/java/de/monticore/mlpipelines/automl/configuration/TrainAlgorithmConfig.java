package de.monticore.mlpipelines.automl.configuration;

public class TrainAlgorithmConfig {
    //private class TrainAlgorithm;
    public int numEpochs;
    public boolean saveTrainedArchitecture;
    public String architectureSavePath;
    public String trainAlgorithmName;

    public TrainAlgorithmConfig() {

    }

    public int getNumEpochs() {
        return numEpochs;
    }

    public void setNumEpochs(int numEpochs) {
        this.numEpochs = numEpochs;
    }

    public boolean isSaveTrainedArchitecture() {
        return saveTrainedArchitecture;
    }

    public void setSaveTrainedArchitecture(boolean saveTrainedArchitecture) {
        this.saveTrainedArchitecture = saveTrainedArchitecture;
    }

    public String getArchitectureSavePath() {
        return architectureSavePath;
    }

    public void setArchitectureSavePath(String architectureSavePath) {
        this.architectureSavePath = architectureSavePath;
    }

    public String getTrainAlgorithmName() {
        return trainAlgorithmName;
    }

    public void setTrainAlgorithmName(String trainAlgorithmName) {
        this.trainAlgorithmName = trainAlgorithmName;
    }
}
