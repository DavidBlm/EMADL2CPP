package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;

import java.util.ArrayList;
import java.util.List;

public class CandidateFinder {
    private final CandidateBuilder candidateBuilder;
    private final AdaNetComponentFinder componentFinder;
    private int minDepth;

    public CandidateFinder() {
        this.candidateBuilder = new CandidateBuilder();
        this.componentFinder = new AdaNetComponentFinder();
        this.minDepth = 1;
    }

    public CandidateFinder(CandidateBuilder candidateBuilder, AdaNetComponentFinder componentFinder) {
        this.candidateBuilder = candidateBuilder;
        this.componentFinder = componentFinder;
    }

    public List<ArchitectureSymbol> findCandidates(ArchitectureSymbol architecture, int minComponentDepth) {
        this.minDepth = minComponentDepth;

        List<AdaNetComponent> adanetComponents = componentFinder.findComponents(architecture, minDepth);
        List<ArchitectureSymbol> candidates = new ArrayList<>();
        for (AdaNetComponent component : adanetComponents) {
            candidates.add(candidateBuilder.createCandidate(architecture, component));
        }
        return candidates;
    }

    public int getMinDepth() {
        return this.minDepth;
    }

    public CandidateBuilder getCandidateBuilder() {
        return this.candidateBuilder;
    }

    public AdaNetComponentFinder getComponentFinder() {
        return this.componentFinder;
    }
}
