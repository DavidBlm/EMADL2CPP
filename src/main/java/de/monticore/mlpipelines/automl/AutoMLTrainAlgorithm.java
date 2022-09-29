package de.monticore.mlpipelines.automl;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;

public abstract class AutoMLTrainAlgorithm {
    private Class trainConfiguration;
    private Pipeline trainPipeline;
    private ArchitectureSymbol startNetwork;

    public abstract void train();
}
