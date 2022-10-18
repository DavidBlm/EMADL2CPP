package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;

public class AdaNet extends TrainAlgorithm {

    @Override
    public void train(ArchitectureSymbol startNetwork) {
        // For at most n iterations:
        //    generate possible components (Exactly the same amount of layers as the biggest component in the network or one more)
        //    for all components:
        //        add component to network in parallel
        //        train network
        //        evaluate network
        //        remove component from network
        //    select best component
        //    add best component to network in parallel
        //    if network with best component is not better than network without best component:
        //        stop adaNet and return network without best component
        for (int i = 0; i < AdaNetConfig.NUM_ITERATIONS; i++) {

        }
    }

    private void generatePossibleComponents(ArchitectureSymbol startNetwork) {

    }

    private void addComponentToNetwork(ArchitectureSymbol startNetwork) {

    }

    private void trainAndEvaluateNetwork(ArchitectureSymbol startNetwork) {
        // Call train pipeline
    }

    private void removeComponentFromNetwork(ArchitectureSymbol startNetwork) {

    }

    private void selectBestComponent(ArchitectureSymbol startNetwork) {

    }
}
