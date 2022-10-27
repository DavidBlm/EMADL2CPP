package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.Pipeline;
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
    int lastStepBestComponentsDepth = 1;
    private ArchitectureSymbol scaledArchitecture;
    private final CandidateFinder candidateFinder;
    private final boolean stopAlgorithm = false;
    private CandidateBuilder candidateBuilder;
    private CandidateEvaluationResult bestCandidateResult;
    private Pipeline trainPipeline;

    public AdaNet() {
        this.candidateBuilder = new CandidateBuilder();
        AdaNetComponentFinder componentFinder = new AdaNetComponentFinder();
        this.candidateFinder = new CandidateFinder(componentFinder);
    }

    public AdaNet(CandidateFinder candidateFinder) {
        super();
        this.candidateFinder = candidateFinder;
    }

    public void setPipeline(Pipeline pipeline) {
        this.trainPipeline = pipeline;
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
        AdaNetCandidate candidate = new AdaNetCandidate(component, null);

        this.bestCandidateResult = evaluateCandidate(candidate);
    }

    private CandidateEvaluationResult evaluateCandidate(AdaNetCandidate candidate) {
        ArchitectureSymbol candidateArchitecture = candidateBuilder.createArchitectureFromCandidate(candidate);
        Configuration configuration = new Configuration();
        this.trainPipeline.execute(candidateArchitecture, configuration);
        float score = this.trainPipeline.getTrainedAccuracy();
        return new CandidateEvaluationResult(candidate, score);
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

    private AdaNetCandidate selectBestCandidate(List<AdaNetCandidate> candidates) {
        HashMap<AdaNetCandidate, Float> candidateScores = new HashMap<AdaNetCandidate, Float>();
        for (AdaNetCandidate candidate : candidates) {
            CandidateEvaluationResult evaluationResult = evaluateCandidate(candidate);
            if (evaluationResult.getScore() > bestCandidate.getScore()) {
                bestCandidateResult = evaluationResult;
            }
        }

        return bestCandidate;
    }
}
