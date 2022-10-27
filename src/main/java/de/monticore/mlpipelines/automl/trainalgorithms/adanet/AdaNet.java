package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;
import de.monticore.mlpipelines.automl.configuration.Configuration;
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
    private final CandidateFinder candidateFinder;
    private final boolean stopAlgorithm = false;
    private CandidateBuilder candidateBuilder;
    private CandidateEvaluationResult bestCandidateResult;

    public AdaNet() {
        this.candidateBuilder = new CandidateBuilder();
        AdaNetComponentFinder componentFinder = new AdaNetComponentFinder();
        this.candidateFinder = new CandidateFinder(componentFinder);
    }

    public AdaNet(CandidateFinder candidateFinder) {
        super();
        this.candidateFinder = candidateFinder;
    }


    public CandidateEvaluationResult getBestCandidateResult() {
        return bestCandidateResult;
    }


    @Override
    public void execute(ArchitectureSymbol startNetwork) throws IllegalStateException {
        this.execute();
    }

    public void execute() {
        if (getTrainPipeline() == null) {
            throw new IllegalStateException("Train pipeline not set");
        }

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
        AdaNetCandidate candidate = new AdaNetCandidate(component, null);

        this.bestCandidateResult = evaluateCandidate(candidate);
    }

    private void executeIteration() {
        AdaNetCandidate lastCandidate = bestCandidateResult.getCandidate();
        List<AdaNetCandidate> candidates = candidateFinder.findCandidates(lastCandidate);
        CandidateEvaluationResult bestNewCandidate = selectBestCandidate(candidates);
        if (bestNewCandidate.getScore() <= bestCandidateResult.getScore()) {
            return;
        }
        bestCandidateResult = bestNewCandidate;
    }

    private CandidateEvaluationResult evaluateCandidate(AdaNetCandidate candidate) {
        ArchitectureSymbol candidateArchitecture = candidateBuilder.createArchitectureFromCandidate(candidate);
        Configuration configuration = new Configuration();
        this.getTrainPipeline().execute(candidateArchitecture, configuration);
        float score = this.getTrainPipeline().getTrainedAccuracy();
        return new CandidateEvaluationResult(candidate, score);
    }

    private CandidateEvaluationResult selectBestCandidate(List<AdaNetCandidate> candidates) {
        CandidateEvaluationResult bestCandidate = new CandidateEvaluationResult(null, 0);
        for (AdaNetCandidate candidate : candidates) {
            CandidateEvaluationResult evaluationResult = evaluateCandidate(candidate);
            if (evaluationResult.getScore() > bestCandidate.getScore()) {
                bestCandidateResult = evaluationResult;
            }
        }

        return bestCandidate;
    }
}
