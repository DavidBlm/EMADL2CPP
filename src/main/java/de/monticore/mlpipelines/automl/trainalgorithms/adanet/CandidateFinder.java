package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import java.util.ArrayList;
import java.util.List;

public class CandidateFinder {
    private final AdaNetComponentFinder componentFinder;
    private int minDepth;

    public CandidateFinder() {
        this.componentFinder = new AdaNetComponentFinder();
        this.minDepth = 1;
    }

    public CandidateFinder(AdaNetComponentFinder componentFinder) {
        this.componentFinder = componentFinder;
    }

    public List<AdaNetCandidate> findCandidates(AdaNetCandidate currentCandidate) {
        this.minDepth = currentCandidate.getComponent().getNumberLayers();
        List<AdaNetComponent> adanetComponents = componentFinder.findComponents(minDepth);
        List<AdaNetCandidate> candidates = createCandidatesFromComponents(adanetComponents, currentCandidate);

        return candidates;
    }

    private List<AdaNetCandidate> createCandidatesFromComponents(
            List<AdaNetComponent> adanetComponents,
            AdaNetCandidate currentCandidate) {
        List<AdaNetCandidate> candidates = new ArrayList<>();
        for (AdaNetComponent component : adanetComponents) {
            AdaNetCandidate candidate = new AdaNetCandidate(component, currentCandidate.getPreviousComponents());
            candidates.add(candidate);
        }
        return candidates;
    }

    private AdaNetCandidate createNewCandidate(AdaNetCandidate currentCandidate, AdaNetComponent component) {
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        previousComponents.addAll(currentCandidate.getPreviousComponents());
        previousComponents.add(currentCandidate.getComponent());
        AdaNetCandidate newCandidate = new AdaNetCandidate(component, previousComponents);
        return newCandidate;
    }


    public AdaNetComponentFinder getComponentFinder() {
        return componentFinder;
    }
}
