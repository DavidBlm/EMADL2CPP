package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;

public class NetworkScaler {

    private float depthFactor = 0;
    private float widthFactor = 0;
    private float resolutionFactor = 0;

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
        //for stem
        //repeat layers depthFactor times

        //for other blocks
        //repeat layers depthFactor times
    }

    private void scaleWidth() {
        //for only stem
        //kernel = (widthFactor * filter, widthFactor * filter);
    }


    private void scaleResolution() {

    }

    public float getDepth() {
        return depthFactor;
    }

    public void setDepth(float alpha) {
        this.depthFactor = (float)Math.pow(alpha, EfficientNetConfig.phi);
    }

    public float getWidth() {
        return widthFactor;
    }

    public void setWidth(float beta) {
        this.widthFactor = (float)Math.pow(beta, EfficientNetConfig.phi);
    }

    public float getResolution() {
        return resolutionFactor;
    }

    public void setResolution(float gamma) {
        this.resolutionFactor = (float)Math.pow(gamma, EfficientNetConfig.phi);
    }

    public ArchitectureSymbol getLocArchitectureSymbol() {
        return locArchitectureSymbol;
    }

    public void setLocArchitectureSymbol(ArchitectureSymbol architectureSymbol) {
        this.locArchitectureSymbol = architectureSymbol;
    }
}
