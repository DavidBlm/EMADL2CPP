package de.monticore.mlpipelines.workflow;

import de.monticore.mlpipelines.python.PythonPipeline;

public abstract class AutonomousPipelineOrchestration extends AbstractWorkflow {


    public void createPipeline() {
        final PythonPipeline pythonPipeline = new PythonPipeline();
        this.setPipeline(pythonPipeline);
    }
}
