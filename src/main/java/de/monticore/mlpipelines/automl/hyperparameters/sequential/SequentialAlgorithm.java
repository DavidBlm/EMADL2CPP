package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.emadlprinter.ASTConfLangCompilationUnitPrinter;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;
import de.monticore.mlpipelines.pipelines.Pipeline;

public abstract class SequentialAlgorithm extends AbstractHyperparameterAlgorithm {

    private ASTConfLangCompilationUnit currentHyperparameters;

    public ASTConfLangCompilationUnit getCurrentHyperparameters() {
        return this.currentHyperparameters;
    }

    public void setCurrentHyperparameters(ASTConfLangCompilationUnit currentHyperparameters) {
        this.currentHyperparameters = currentHyperparameters;
    }

    protected boolean updateBest(double currValue, double newValue, String metricType) {
        if (metricType.equals("accuracy")) {
            return newValue > currValue;
        }
        else {
            return newValue < currValue;
        }
    }

    @Override
    public void executeOptimization(Pipeline pipeline, ASTConfLangCompilationUnit searchSpace, ASTConfLangCompilationUnit evaluationCriteria) {
        String metricType = (String) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "metric");
        double criteria = (double) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "acceptance_rate");
        int maxIterNum = (int) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "max_iteration_number");

        ASTConfLangCompilationUnit trainingConfiguration = this.getInitialHyperparams(searchSpace);

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();

        while(this.getCurrentIteration() < maxIterNum) {
            pipeline.setConfigurationModel(trainingConfiguration);
            pipeline.execute();

            double evalValue = Double.valueOf(((Float) (pipeline.getTrainedAccuracy() / 100)).toString());
            // TODO: Find correct way to log current iteration and its metric result
            System.out.println("Current Iteration: " + this.getCurrentIteration() + "; Eval Value: " + evalValue);
            //TODO: Pretty print trainingConfiguration correctly into conf files
            System.out.println(printer.prettyPrint(trainingConfiguration));
            this.executeOptimizationStep(trainingConfiguration, searchSpace, evalValue, metricType);
            trainingConfiguration = this.getNewHyperparamsCandidate(searchSpace);
            if (evalValue >= criteria) {
                break;
            }
        }

        ASTConfLangCompilationUnit bestTrainingConfig = this.getCurrBestHyperparams();
        double bestEvalValue = this.getCurrBestEvalMetric();

        // TODO: Pretty print optimal trainingConfiguration correctly into conf file and print best eval value
        System.out.println("Best Eval Value: " + bestEvalValue);
        System.out.println(printer.prettyPrint(bestTrainingConfig));
    }
}
