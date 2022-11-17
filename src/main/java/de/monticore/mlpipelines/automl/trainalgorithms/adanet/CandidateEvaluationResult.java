package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;

public class CandidateEvaluationResult {
    private final float score;
    private final AdaNetCandidate candidate;

    public CandidateEvaluationResult(AdaNetCandidate candidate, float score) {
        this.score = score;
        this.candidate = candidate;
    }

    public float getScore() {
        return this.score;
    }

    public AdaNetCandidate getCandidate() {
        return this.candidate;
    }
}
