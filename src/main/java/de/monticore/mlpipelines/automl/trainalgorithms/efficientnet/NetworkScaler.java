package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;

public class NetworkScaler {

    private float depth;
    private float width;
    private float resolution;

    public ArchitectureSymbol scale(ArchitectureSymbol originalArchitectureSymbol, ScalingFactors scalingFactors) {
        ArchitectureSymbol locArchitecture = originalArchitectureSymbol;
        setDepth(scalingFactors.alpha);
        setWidth(scalingFactors.beta);
        setResolution(scalingFactors.gamma);


        locArchitecture = scaleDepth(locArchitecture);
        locArchitecture = scaleWidth(locArchitecture);
        locArchitecture = scaleResolution(locArchitecture);

        return locArchitecture;
    }


    private ArchitectureSymbol scaleDepth(ArchitectureSymbol architectureSymbol) {

        return architectureSymbol;
    }

    private ArchitectureSymbol scaleWidth(ArchitectureSymbol architectureSymbol) {

        return architectureSymbol;
    }

    private ArchitectureSymbol scaleResolution(ArchitectureSymbol architectureSymbol) {
        return architectureSymbol;
    }

    public float getDepth() {
        return depth;
    }

    public void setDepth(float alpha) {
        this.depth = (float) Math.pow(alpha, EfficientNetConfig.phi);
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float beta) {
        this.width = (float) Math.pow(beta, EfficientNetConfig.phi);
    }

    public float getResolution() {
        return resolution;
    }

    public void setResolution(float gamma) {
        this.resolution = (float) Math.pow(gamma, EfficientNetConfig.phi);
    }
}
