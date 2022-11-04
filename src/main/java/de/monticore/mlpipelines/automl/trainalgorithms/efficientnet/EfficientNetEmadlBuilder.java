package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;

import java.util.ArrayList;
import java.util.List;

public class EfficientNetEmadlBuilder {
    private final ArchitectureSymbol architecture;
    private final EfficientNetConfig config;

    public EfficientNetEmadlBuilder(ArchitectureSymbol architecture, EfficientNetConfig config) {
        this.architecture = architecture;
        this.config = config;
    }

    public List<String> getEmadl() {
        List<String> lines = new ArrayList<>();
        lines.add(createEmadlHeader());
        lines.add("}");
        lines.add("EfficientNetB0()");
        return lines;
    }

    private String createEmadlHeader() {
        String name = "EfficientNetB" + config.getPhi();
        String parameters = getHeaderParameters();
        return "component " + name + parameters + "{";
    }

    private String getHeaderParameters() {
        return "<classes=" + config.getNumberClasses() + ">";
    }
}
