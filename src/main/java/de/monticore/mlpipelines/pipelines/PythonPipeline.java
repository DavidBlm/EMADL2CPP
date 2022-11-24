package de.monticore.mlpipelines.pipelines;

import com.google.common.collect.Lists;
import conflang._ast.ASTConfigurationEntry;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.backend.generation.MontiAnnaGenerator;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.montipipes.generators.PipelineGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PythonPipeline extends Pipeline {


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
        final List<ASTConfigurationEntry> pipelineConfigurationEntries = pipelineConfiguration.getConfiguration().getAllConfigurationEntries();
        final String trainingConfFileName = calculatePathToGeneratedTrainingConfiguration().getFileName().toString();
        final String generatedNetworkFileName = calculatePathToGeneratedNetwork().getFileName().toString();

        final String schemaAPIScriptName = createSchemaApiPathFromLearningMethod().getFileName().toString();
        final List<String> scriptDependencies = Lists.newArrayList(schemaAPIScriptName, trainingConfFileName, generatedNetworkFileName );
        new PipelineGenerator().generatePipelineExecutor(pipelineConfigurationEntries, this.pipelineModelWithExecutionSemantics, scriptDependencies);
    }

    private Path calculatePathToGeneratedNetwork() {
        final String NetworkName = this.neuralNetwork.getName().replace("Network","");
        return Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToTrainingConfiguration(), "CNNCreator_"+ NetworkName );
    }

    private Path calculatePathToGeneratedTrainingConfiguration() {
        final String NetworkName = this.neuralNetwork.getName();
        return Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToTrainingConfiguration(), "Training_Configuration_"+ NetworkName );
    }

    @Override
    public void execute() {
        generateTrainingConfiguration();
        generatePipelineExecutionScript();
        runScript();

    }

    protected Process runScript() {
        final String pathToExecutionScript = Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToExecutionScript(), "pipeline_executor.py").toString();
        ProcessBuilder processBuilder = new ProcessBuilder("python3", pathToExecutionScript);
        processBuilder.redirectErrorStream(true);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Pipeline execution has aborted", e);
        }
    }

}
