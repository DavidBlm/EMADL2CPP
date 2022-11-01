package de.monticore.mlpipelines.python;

import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.Pipeline;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PythonPipeline extends Pipeline {


    //private PipelineGenerator
    private Path pathToExecutionScript;

    public PythonPipeline(final LearningMethod learningMethod) {
        super(learningMethod);
    }

    public Path createSchemaApiPathFromLearningMethod() {
        final String capitalisedLearningMethodName = this.learningMethod.name().charAt(0) + this.learningMethod.name().substring(1).toLowerCase();
        return Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToTrainingConfiguration(), capitalisedLearningMethodName + "_Schema_API");
    }

    public void generateTrainingConfiguration() {

    }

    public void generatePipelineExecutionScript() {
        // pipelinegenrator.generate(path)
    }

    @Override
    public void execute() {
        createSchemaApiPathFromLearningMethod();
        generateTrainingConfiguration();
        generatePipelineExecutionScript();
        runScript();

    }

    private void runScript() {
    }

}
