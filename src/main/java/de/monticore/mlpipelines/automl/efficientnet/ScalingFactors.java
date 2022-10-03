package de.monticore.mlpipelines.automl.efficientnet;

public class ScalingFactors {
    private float alpha;
    private float beta;
    private float gamma;

    public ScalingFactors(float alpha, float beta, float gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getBeta() {
        return beta;
    }

    public void setBeta(float beta) {
        this.beta = beta;
    }

    public float getGamma() {
        return gamma;
    }

    public void setGamma(float gamma) {
        this.gamma = gamma;
    }
}
