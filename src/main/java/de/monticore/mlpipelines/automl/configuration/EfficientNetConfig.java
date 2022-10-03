package de.monticore.mlpipelines.automl.configuration;

public class EfficientNetConfig extends TrainAlgorithmConfig{
    public static final double FLOPS_CONDITION_VALUE = 2;
    public static final double MIN_ALPHA = 1;
    public static final double MIN_BETA = 1;
    public static final double MIN_GAMMA = 1;
    public static final double MAX_BETA = 1.4;
    public static final double MAX_GAMMA = 1.4;
    public static final double BETA_STEP_SIZE = 0.1;
    public static final double GAMMA_STEP_SIZE = 0.1;
    public static final double maximum_image_width_and_height = 32;
    public static final int phi = 1;
    public double alpha;
    public double beta;
    public double gamma;
}
