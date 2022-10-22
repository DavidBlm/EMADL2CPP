package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;

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
    private CandidateFinder candidateFinder;
    private ArchitectureSymbol currentArchitecture;
    private boolean stopAlgorithm = false;

    public AdaNet() {
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        AdaNetComponentFinder componentFinder = new AdaNetComponentFinder();
        this.candidateFinder = new CandidateFinder(candidateBuilder, componentFinder);
    }

    public AdaNet(CandidateFinder candidateFinder) {
        super();
        this.candidateFinder = candidateFinder;
    }


    @Override
    public void execute(ArchitectureSymbol startNetwork) {
        setStartNetwork(startNetwork);
        for (int i = 1; i < AdaNetConfig.MAX_ITERATIONS; i++) {
            executeIteration();
            if (stopAlgorithm) {
                break;
            }
        }
    }

    private void executeIteration() {
        List<ArchitectureSymbol> candidates = candidateFinder.findCandidates(currentArchitecture,
                lastStepBestComponentsDepth);
        ArchitectureSymbol bestCandidate = selectBestCandidate(candidates);
        if (bestCandidate == null) {
            return;
        }
        currentArchitecture = bestCandidate;
    }

    private ArchitectureSymbol selectBestCandidate(List<ArchitectureSymbol> candidates) {
        // train all candidates
        // evaluate all candidates
        // select best candidate
        // return best candidate
        return candidates.get(0);
    }

    @Override
    public void setStartNetwork(ArchitectureSymbol startNetwork) {
        super.setStartNetwork(startNetwork);
        this.currentArchitecture = startNetwork;
    }
}
