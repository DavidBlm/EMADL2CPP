package de.monticore.mlpipelines.workflow;

import java.nio.file.Path;

public abstract class AutonomousPipeline extends AbstractWorkflow {

    public abstract void selectSchemaAPI();

    public abstract void generateTrainingConfiguration();

    public abstract Path generatePipelineExecutionScript();
}
