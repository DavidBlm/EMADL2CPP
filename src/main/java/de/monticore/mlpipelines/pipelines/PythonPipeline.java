package de.monticore.mlpipelines.pipelines;

import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.backend.generation.MontiAnnaGenerator;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PythonPipeline extends Pipeline {


    //private PipelineGenerator
    private Path pathToExecutionScript;

    private MontiAnnaGenerator montiAnnaGenerator;

    public void setMontiAnnaGenerator(final MontiAnnaGenerator montiAnnaGenerator) {
        this.montiAnnaGenerator = montiAnnaGenerator;
    }

    public PythonPipeline(final LearningMethod learningMethod) {
        super(learningMethod);
    }

    public Path createSchemaApiPathFromLearningMethod() {
        final String capitalisedLearningMethodName = this.learningMethod.name().charAt(0) + this.learningMethod.name().substring(1).toLowerCase();
        return Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToTrainingConfiguration(), capitalisedLearningMethodName + "_Schema_API");
    }

    public void generateTrainingConfiguration() {
        montiAnnaGenerator.generateTrainingConfiguration(this.trainingConfiguration);
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
