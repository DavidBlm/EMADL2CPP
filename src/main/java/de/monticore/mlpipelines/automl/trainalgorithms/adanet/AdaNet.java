package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;

import java.util.HashMap;
import java.util.Iterator;
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
    private AdaNetCandidate currentCandidate;
    private boolean stopAlgorithm = false;

    public AdaNet() {
        CandidateBuilder candidateBuilder = new CandidateBuilder();
        AdaNetComponentFinder componentFinder = new AdaNetComponentFinder();
        this.candidateFinder = new CandidateFinder(componentFinder);
    }

    public AdaNet(CandidateFinder candidateFinder) {
        super();
        this.candidateFinder = candidateFinder;
    }


    @Override
    public void execute(ArchitectureSymbol startNetwork) {
        setStartNetwork(startNetwork);
        createFirstCandidate();
        for (int i = 1; i < AdaNetConfig.MAX_ITERATIONS; i++) {
            executeIteration();
            if (stopAlgorithm) {
                break;
            }
        }
    }

    private void createFirstCandidate() {
        AdaNetComponent component = new AdaNetComponent(1);
        currentCandidate = new AdaNetCandidate(component, null);
    }

    private void executeIteration() {
        List<AdaNetCandidate> candidates = candidateFinder.findCandidates(currentCandidate);
        AdaNetCandidate bestCandidate = selectBestCandidate(candidates);
        if (bestCandidate == null) {
            return;
        }
        currentCandidate = bestCandidate;
    }

    private AdaNetCandidate selectBestCandidate(List<AdaNetCandidate> candidates) {
        HashMap<AdaNetCandidate, Float> candidateScores = new HashMap<AdaNetCandidate, Float>();
        for (AdaNetCandidate candidate : candidates) {
            List<String> candidateEmadlContent = candidate.getEmadl();
            ArchitectureSymbol candidateArchitecture = createArchitectureFromEmadl(candidateEmadlContent);
            float score = trainCandidate(candidateArchitecture);
            candidateScores.put(candidate, score);
        }

        AdaNetCandidate bestCandidate = getAdaNetCandidateWithHighestScore(candidateScores);
        return bestCandidate;
    }

    private ArchitectureSymbol createArchitectureFromEmadl(List<String> candidateEmadlContent) {
        ArchitectureSymbol candidateArchitecture = null;

    }

    private AdaNetCandidate getAdaNetCandidateWithHighestScore(HashMap<AdaNetCandidate, Float> candidateScores) {
        Iterator<AdaNetCandidate> it = candidateScores.keySet().iterator();
        AdaNetCandidate fk = it.next();
        Float max = candidateScores.get(fk);
        while(it.hasNext()) {
            AdaNetCandidate k = it.next();
            Float val = candidateScores.get(k);
            if (val > max){
                max = val;
                fk=k;
            }
        }
        return fk;
    }
}
