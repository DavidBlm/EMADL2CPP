package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;

public class NetworkScaler {

    private float depth = 0;
    private float width = 0;
    private float resolution = 0;

    private ArchitectureSymbol locArchitectureSymbol = null;

    public ArchitectureSymbol scale(ArchitectureSymbol originalArchitectureSymbol, ScalingFactors scalingFactors, int phi) {
        setLocArchitectureSymbol(originalArchitectureSymbol);
        setDepth(scalingFactors.alpha);
        setWidth(scalingFactors.beta);
        setResolution(scalingFactors.gamma);

        scaleDepth();
        scaleWidth();
        scaleResolution();

        return getLocArchitectureSymbol();
    }


    private void scaleDepth() {
    }

    private void scaleResolution() {
    }

    private void scaleWidth() {

    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float alpha) {
        this.depth = (float)Math.pow(alpha, EfficientNetConfig.phi);
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float beta) {
        this.width = (float)Math.pow(beta, EfficientNetConfig.phi);
    }

    public float getResolution() {
        return resolution;
    }

    public void setResolution(float gamma) {
        this.resolution = (float)Math.pow(gamma, EfficientNetConfig.phi);
    }

    public ArchitectureSymbol getLocArchitectureSymbol() {
        return locArchitectureSymbol;
    }

    public void setLocArchitectureSymbol(ArchitectureSymbol architectureSymbol) {
        this.locArchitectureSymbol = architectureSymbol;
    }
}
