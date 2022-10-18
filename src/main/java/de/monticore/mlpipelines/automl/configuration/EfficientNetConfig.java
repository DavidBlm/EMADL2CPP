package de.monticore.mlpipelines.automl.configuration;

import de.monticore.mlpipelines.automl.trainalgorithms.efficientnet.ScalingFactors;

public class EfficientNetConfig extends TrainAlgorithmConfig{
    private double flopsConditionValue;

    private ScalingFactors minScalingFactors;

    private ScalingFactors maxScalingFactors;

    private ScalingFactors scalingFactorsStepSize;

    private int maximumImageWidthAndHeight;

    private int minimumImageWidthAndHeight;

    private int phi;

    public EfficientNetConfig() {

    }

    public double getFlopsConditionValue() {
        return flopsConditionValue;
    }

    public void setFlopsConditionValue(double flopsConditionValue) {
        this.flopsConditionValue = flopsConditionValue;
    }

    public ScalingFactors getMinScalingFactors() {
        return minScalingFactors;
    }

    public void setMinScalingFactors(ScalingFactors minScalingFactors) {
        this.minScalingFactors = minScalingFactors;
    }

    public ScalingFactors getMaxScalingFactors() {
        return maxScalingFactors;
    }

    public void setMaxScalingFactors(ScalingFactors maxScalingFactors) {
        this.maxScalingFactors = maxScalingFactors;
    }

    public ScalingFactors getScalingFactorsStepSize() {
        return scalingFactorsStepSize;
    }

    public void setScalingFactorsStepSize(ScalingFactors scalingFactorsStepSize) {
        this.scalingFactorsStepSize = scalingFactorsStepSize;
    }

    public int getMaximumImageWidthAndHeight() {
        return maximumImageWidthAndHeight;
    }

    public void setMaximumImageWidthAndHeight(int maximumImageWidthAndHeight) {
        this.maximumImageWidthAndHeight = maximumImageWidthAndHeight;
    }

    public int getMinimumImageWidthAndHeight() {
        return minimumImageWidthAndHeight;
    }

    public void setMinimumImageWidthAndHeight(int minimumImageWidthAndHeight) {
        this.minimumImageWidthAndHeight = minimumImageWidthAndHeight;
    }

    public int getPhi() {
        return phi;
    }

    public void setPhi(int phi) {
        this.phi = phi;
    }
}
