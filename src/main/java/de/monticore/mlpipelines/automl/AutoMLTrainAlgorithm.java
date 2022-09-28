package de.monticore.mlpipelines.automl;

import de.monticore.mlpipelines.Pipeline;

public abstract class AutoMLTrainAlgorithm {
    private Class trainConfiguration;
    private Pipeline trainPipeline;
    private Class startNetwork;

    public abstract void train();
}
