package de.monticore.mlpipelines.automl.configuration;

public class PreprocessingConfig {

    private double trainSplit;

    private String normMethod;

    private Boolean grayscale;

    private Boolean dataAugmentation;

    public double getTrainSplit() {
        return trainSplit;
    }

    public void setTrainSplit(double trainSplit) {
        this.trainSplit = trainSplit;
    }

    public String getNormMethod() {
        return normMethod;
    }

    public void setNormMethod(String normMethod) {
        this.normMethod = normMethod;
    }

    public Boolean getGrayscale() {
        return grayscale;
    }

    public void setGrayscale(Boolean grayscale) {
        this.grayscale = grayscale;
    }

    public Boolean getDataAugmentation() {
        return dataAugmentation;
    }

    public void setDataAugmentation(Boolean dataAugmentation) {
        this.dataAugmentation = dataAugmentation;
    }
}
