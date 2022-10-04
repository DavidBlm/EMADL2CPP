package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.configuration.TrainAlgorithmConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;

public class EfficientNet extends TrainAlgorithm {
    public EfficientNet() {
        super();
    }

    @Override
    public void train(ArchitectureSymbol startNetwork) {
        this.setStartNetwork(startNetwork);
    }

}
