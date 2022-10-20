package de.monticore.mlpipelines.automl.configuration;

import de.monticore.mlpipelines.automl.trainalgorithms.efficientnet.ScalingFactors;

public class    EfficientNetConfig extends TrainAlgorithmConfig{
    public static final double FLOPS_CONDITION_VALUE = 2;
    public static final ScalingFactors MIN_SCALING_FACTORS = new ScalingFactors(1, 1, 1);
    public static final ScalingFactors MAX_SCALING_FACTORS = new ScalingFactors(2, 1.4f, 1.4f);
    public static final ScalingFactors SCALING_FACTORS_STEP_SIZE = new ScalingFactors(0.1f, 0.1f, 0.1f);
    public static final double MAXIMUM_IMAGE_SIZE = 32;
    public static final int MINIMUM_IMAGE_SIZE = 8;
    public static final int PHI = 1;
}
