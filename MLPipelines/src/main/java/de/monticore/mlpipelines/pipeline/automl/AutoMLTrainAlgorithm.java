package java.de.monticore.mlpipelines.pipeline.automl;

import de.monticore.mlpipelines.pipeline.Pipeline;

public abstract class AutoMLTrainAlgorithm {
    private Class trainConfiguration;
    private Pipeline trainPipeline;
    private Class startNetwork;

    public abstract void train();
}
