package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearch;

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

public class AdaNetAlgorithm extends NeuralArchitectureSearch {
    private final CandidateFinder candidateFinder;
    private boolean stopAlgorithm = false;
    //    private final CandidateEmadlBuilder candidateBuilder;
    private CandidateEvaluationResult bestCandidateResult;

    public AdaNetAlgorithm() {
//        this.candidateBuilder = new CandidateEmadlBuilder(sourceModelPath, generatedModelPath);
        AdaNetComponentFinder componentFinder = new AdaNetComponentFinder();
        this.candidateFinder = new CandidateFinder(componentFinder);
    }

    public AdaNetAlgorithm(CandidateFinder candidateFinder, CandidateEmadlBuilder candidateBuilder) {
        super();
        this.candidateFinder = candidateFinder;
//        this.candidateBuilder = candidateBuilder;
    }


    public CandidateEvaluationResult getBestCandidateResult() {
        return bestCandidateResult;
    }


    @Override
    public ArchitectureSymbol execute(ArchitectureSymbol startNetwork) throws IllegalStateException {
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

        return null;
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
            this.stopAlgorithm = true;
            return;
        }
        bestCandidateResult = bestNewCandidate;
    }

    private CandidateEvaluationResult evaluateCandidate(AdaNetCandidate candidate) {
//        ArchitectureSymbol candidateArchitecture = candidateBuilder.createArchitectureFromCandidate(candidate);
//        Configuration configuration = new Configuration();
//        this.getTrainPipeline().execute(candidateArchitecture, configuration);
//        float score = this.getTrainPipeline().getTrainedAccuracy();
//        return new CandidateEvaluationResult(candidate, score);
        return null;
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
