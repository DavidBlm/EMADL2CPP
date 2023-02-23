package de.monticore.mlpipelines.pipelines;

import com.google.common.collect.Lists;
import conflang._ast.ASTConfigurationEntry;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.backend.generation.MontiAnnaGenerator;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.montipipes.config.ExecutionScriptConfiguration;
import de.monticore.montipipes.generators.PipelineGenerator;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class PythonPipeline extends Pipeline {

    private String modelOutputDirectory = "/model/mnist.LeNetNetwork/";
    private final String modelOutputDirectoryWithDot = "." + modelOutputDirectory;
    private MontiAnnaGenerator montiAnnaGenerator;

    public PythonPipeline(final LearningMethod learningMethod) {
        super(learningMethod);
    }

    public void setMontiAnnaGenerator(final MontiAnnaGenerator montiAnnaGenerator) {
        this.montiAnnaGenerator = montiAnnaGenerator;
    }

    public void setModelOutputDirectory(final String modelOutputDirectory) {
        this.modelOutputDirectory = modelOutputDirectory;
    }

    @Override
    public void execute() {
        generateTrainingConfiguration();
        generatePipelineExecutionScript();
        Process process = runScript();
        final InputStream pythonConsoleStream = process.getInputStream();
        try {
            String result = IOUtils.toString(pythonConsoleStream, StandardCharsets.UTF_8);
            process.waitFor();
            System.out.println(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getTrainedAccuracy() {
        JSONObject json = getTrainingResultsAsJsonObject();
        float accuracy = json.getFloat("accuracy");

        return accuracy;
    }

    private JSONObject getTrainingResultsAsJsonObject() {
        String generatedModelPath = MontiAnnaContext.getInstance()
                .getExperimentConfiguration()
                .getPathToExecutionScript();
        String pathToOutputFile = generatedModelPath + modelOutputDirectory;
        String fileName = "results.json";
        String pathToFile = System.getProperty("user.dir") + "\\" + pathToOutputFile + fileName;

        JSONObject json = null;
        try {
            FileReader reader = new FileReader(pathToFile);
            json = new JSONObject(IOUtils.toString(reader));
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    private static String getTrainingOutputFileName(String pathToOutputFile) {
        File folder = new File(pathToOutputFile);
        File[] listOfFiles = folder.listFiles();
        String fileName = "";
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().startsWith("model_") && file.getName().endsWith(".json")) {
                fileName = file.getName();
                break;
            }
        }
        return fileName;
    }

    public void generateTrainingConfiguration() {
        final String configName = "Training_Configuration_" + neuralNetwork.getFullName().replace(".", "_");
        montiAnnaGenerator.generateTrainingConfiguration(this.trainingConfiguration, configName);
    }

    public void generatePipelineExecutionScript() {
        final List<ASTConfigurationEntry> pipelineConfigurationEntries = pipelineConfiguration.getConfiguration()
                .getAllConfigurationEntries();
        final String trainingConfFileName = calculatePathToGeneratedTrainingConfiguration().getFileName().toString();
        final String generatedNetworkFileName = calculatePathToGeneratedNetwork().getFileName().toString();

        final String schemaAPIScriptName = createSchemaApiPathFromLearningMethod().getFileName().toString();
        final List<String> scriptDependencies = Lists.newArrayList(schemaAPIScriptName, trainingConfFileName,
                generatedNetworkFileName);
        final PipelineGenerator pipelineGenerator = new PipelineGenerator();
        pipelineGenerator.setTrainingConfigurationName(trainingConfFileName);
        pipelineGenerator.setSchemaAPIName(schemaAPIScriptName);
        final String networkFullName = this.neuralNetwork.getFullName().replace(".", "_");
        pipelineGenerator.addScriptConfiguration(ExecutionScriptConfiguration.MODEL_DIRECTORY,
                modelOutputDirectoryWithDot);
        pipelineGenerator.generatePipelineExecutor(pipelineConfigurationEntries,
                this.pipelineModelWithExecutionSemantics, scriptDependencies);
    }

    protected Process runScript() {
        final String pathToExecutionScriptDirectory = MontiAnnaContext.getInstance()
                .getExperimentConfiguration()
                .getPathToExecutionScript();
        final String pathToExecutionScript = Paths.get("Pipeline_Executor.py").toString();
        ProcessBuilder processBuilder = new ProcessBuilder("python3", pathToExecutionScript);
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(new File(pathToExecutionScriptDirectory));
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Pipeline execution has aborted", e);
        }
    }

    private Path calculatePathToGeneratedTrainingConfiguration() {
        final String NetworkName = this.neuralNetwork.getFullName().replace(".", "_");
        return Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToTrainingConfiguration(),
                "Training_Configuration_" + NetworkName);
    }

    private Path calculatePathToGeneratedNetwork() {
        final String NetworkName = this.neuralNetwork.getFullName().replace(".", "_");
        return Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToTrainingConfiguration(),
                "CNNNet_" + NetworkName);
    }

    public Path createSchemaApiPathFromLearningMethod() {
        final String capitalisedLearningMethodName = this.learningMethod.name().charAt(0) + this.learningMethod.name()
                .substring(1)
                .toLowerCase();
        return Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToTrainingConfiguration(),
                capitalisedLearningMethodName + "_Schema_API");
    }
}
