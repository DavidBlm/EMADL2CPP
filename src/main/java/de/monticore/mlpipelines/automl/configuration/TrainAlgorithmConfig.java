package de.monticore.mlpipelines.automl.configuration;

public class TrainAlgorithmConfig {
    //private class TrainAlgorithm;
    public int numEpochs;
    public boolean saveTrainedArchitecture;
    public String architectureSavePath;
    public String trainAlgorithmName;
    private String trainPipelineName;

    public TrainAlgorithmConfig() {
        trainAlgorithmName = "EfficientNet";
        trainPipelineName = "Pytorch";
        numEpochs = 10;
        saveTrainedArchitecture = false;
        architectureSavePath = "";
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

    public String getTrainPipelineName() {
        return trainPipelineName;
    }

    public void setTrainPipelineName(String pipelineName) {
        this.trainPipelineName = pipelineName;
    }
}
