package de.monticore.mlpipelines.configuration;

public final class MontiAnnaContext {
    private String parentModelPath;

    private String rootModelName;
    private static MontiAnnaContext montiAnnaConfiguration;
    private final String pipelineReferenceModelsPath = "";

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

    public void initContext(final String parentModelPath, final String rootModelName) {
        getInstance().parentModelPath = parentModelPath;
        getInstance().rootModelName = rootModelName;
    }

    public String getPipelineReferenceModelsPath() {
        return pipelineReferenceModelsPath;
    }

}
