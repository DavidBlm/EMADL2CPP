package de.monticore.mlpipelines.python;

import de.monticore.mlpipelines.Pipeline;

import java.nio.file.Path;

public class PythonPipeline extends Pipeline {



    //private PipelineGenerator
    private Path pathToExecutionScript;


    public void selectSchemaAPI() {

    }

    public void generateTrainingConfiguration() {

    }

    public void generatePipelineExecutionScript() {
        // pipelinegenrator.generate(path)
    }

    @Override
    public void execute() {
        selectSchemaAPI();
        generateTrainingConfiguration();
        generatePipelineExecutionScript();
        runScript();

    }

    private void runScript() {
    }

}
