package de.monticore.mlpipelines.configuration;

public class ExperimentConfiguration {

    public final  String pathToTrainingConfiguration;
    public final  String pathToPipelineImplementations;
    public final  String pathToExecutionScript;

    public ExperimentConfiguration(final String pathToTrainingConfiguration, final String pathToPipelineImplementations, final String pathToExecutionScript) {
        this.pathToTrainingConfiguration = pathToTrainingConfiguration;
        this.pathToPipelineImplementations = pathToPipelineImplementations;
        this.pathToExecutionScript = pathToExecutionScript;
    }
}
