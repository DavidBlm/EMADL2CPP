package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;

import java.util.ArrayList;
import java.util.List;

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

public class AdaNet extends TrainAlgorithm {
    int lastStepBestComponentsDepth = 1;
    private ArchitectureSymbol scaledArchitecture;
    private CandidateSearch candidateSearch;

    public AdaNet(CandidateSearch candidateSearch) {
        super();
        this.candidateSearch = candidateSearch;
    }


    @Override
    public void train(ArchitectureSymbol startNetwork) {
        setStartNetwork(startNetwork);
        for (int i = 1; i < AdaNetConfig.MAX_ITERATIONS; i++) {
            AdaNetComponent bestComponent = null;
            List<AdaNetComponent> adanetComponents = generatePossibleComponents(startNetwork);
            bestComponent = selectBestComponent(startNetwork, adanetComponents);
            //call train for all components

            addComponentToNetwork(bestComponent);
        }
    }

    private List<AdaNetComponent> generatePossibleComponents(ArchitectureSymbol startNetwork) {
        List<AdaNetComponent> generatedPossibleComponents = new ArrayList<>();
        int minDepth = lastStepBestComponentsDepth;
        int maxDepth = minDepth + 1;
        for (int i = minDepth; i < maxDepth + 1; i++) {
            AdaNetComponent component = new AdaNetComponent(i);
            generatedPossibleComponents.add(component);
        }

        return generatedPossibleComponents;
    }

    private AdaNetComponent selectBestComponent(
            ArchitectureSymbol startNetwork,
            List<AdaNetComponent> adanetComponents) {
        AdaNetComponent bestComponent = adanetComponents.get(0);

        //check the best component
        return bestComponent;
    }

    private void trainAndEvaluateNetwork(ArchitectureSymbol startNetwork) {
        // Call train pipeline
    }

    private void removeComponentFromNetwork(ArchitectureSymbol startNetwork) {

    }

    private void addComponentToNetwork(AdaNetComponent component) {

    }
}
