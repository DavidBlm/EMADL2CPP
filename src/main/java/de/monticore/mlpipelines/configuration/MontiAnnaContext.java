package de.monticore.mlpipelines.configuration;

import de.monticore.lang.monticar.emadl.generator.Backend;

public final class MontiAnnaContext {
    private String parentModelPath;

    private String rootModelName;
    private static MontiAnnaContext montiAnnaConfiguration;
    private final String pipelineReferenceModelsPath = "";

    private final Backend targetBackend = Backend.PYTORCH;

    private MontiAnnaContext() {}

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

    public void initContext(final String parentModelPath, final String rootModelName) {
        getInstance().parentModelPath = parentModelPath;
        getInstance().rootModelName = rootModelName;
    }

    public String getPipelineReferenceModelsPath() {
        return pipelineReferenceModelsPath;
    }

}
