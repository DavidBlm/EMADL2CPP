package de.monticore.mlpipelines.configuration;

public class ExperimentConfiguration {

    private final  String pathToTrainingConfiguration;

    private final  String pathToPipelineImplementations;

    private final  String pathToExecutionScript;
    public ExperimentConfiguration(final String pathToTrainingConfiguration, final String pathToPipelineImplementations, final String pathToExecutionScript) {
        this.pathToTrainingConfiguration = pathToTrainingConfiguration;
        this.pathToPipelineImplementations = pathToPipelineImplementations;
        this.pathToExecutionScript = pathToExecutionScript;
    }

    public String getPathToTrainingConfiguration() {
        return pathToTrainingConfiguration;
    }

    public String getPathToPipelineImplementations() {
        return pathToPipelineImplementations;
    }

    public String getPathToExecutionScript() {
        return pathToExecutionScript;
    }
}
