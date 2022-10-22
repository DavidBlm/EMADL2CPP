package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import java.util.List;

public class AdaNetCandidate {
    private AdaNetComponent component;
    private List<AdaNetComponent> previousComponents;

    public AdaNetCandidate() {
    }

    public AdaNetCandidate(AdaNetComponent component, List<AdaNetComponent> previousComponents) {
        this.component = component;
        this.previousComponents = previousComponents;
    }

    public AdaNetComponent getComponent() {
        return this.component;
    }

    public void setComponent(AdaNetComponent component) {
        this.component = component;
    }

    public List<AdaNetComponent> getPreviousComponents() {
        return this.previousComponents;
    }

    public void setPreviousComponents(List<AdaNetComponent> previousComponents) {
        this.previousComponents = previousComponents;
    }
}
