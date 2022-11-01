package de.monticore.mlpipelines.configuration;

import de.monticore.lang.monticar.emadl.generator.Backend;

public final class MontiAnnaContext {
    private String parentModelPath;

    private String rootModelName;
    private static MontiAnnaContext montiAnnaConfiguration;
    private final String pipelineReferenceModelsPath = "";

    private ExperimentConfiguration experimentConfiguration;

    private final Backend targetBackend = Backend.PYTORCH;

    private MontiAnnaContext() {
    }

    public static MontiAnnaContext getInstance() {
        if (montiAnnaConfiguration == null) {
            montiAnnaConfiguration = new MontiAnnaContext();
        }
        return montiAnnaConfiguration;
    }

    public String getParentModelPath() {
        return parentModelPath;
    }

    public String getRootModelName() {
        return rootModelName;
    }

    public Backend getTargetBackend() {
        return targetBackend;
    }

    public ExperimentConfiguration getExperimentConfiguration() {
        return experimentConfiguration;
    }

    public void initContext(final String parentModelPath, final String rootModelName, final ExperimentConfiguration experimentConfiguration) {
        getInstance().parentModelPath = parentModelPath;
        getInstance().rootModelName = rootModelName;
        getInstance().experimentConfiguration = experimentConfiguration;
    }

    public String getPipelineReferenceModelsPath() {
        return pipelineReferenceModelsPath;
    }

}
