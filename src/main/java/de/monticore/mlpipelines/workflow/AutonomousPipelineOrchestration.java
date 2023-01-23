package de.monticore.mlpipelines.workflow;

import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.pipelines.PythonPipeline;

public class AutonomousPipelineOrchestration extends AbstractWorkflow {
    public AutonomousPipelineOrchestration(final MontiAnnaContext applicationContext) {
        super(applicationContext);
    }

    public void createPipeline(final LearningMethod learningMethod) {
        final PythonPipeline pythonPipeline = new PythonPipeline(learningMethod);
        pythonPipeline.setMontiAnnaGenerator(this.montiAnnaGenerator);
        this.setPipeline(pythonPipeline);
    }
}
