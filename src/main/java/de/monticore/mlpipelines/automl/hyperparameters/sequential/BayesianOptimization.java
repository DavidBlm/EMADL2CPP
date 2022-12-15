package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;

public class BayesianOptimization extends SequentialAlgorithm {

    // TODO: Specify datatype
    private Object sampledPoints;

    public Object approximateFunct() {
        Object approximatedFunc = null;
        return approximatedFunc;
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {

    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        return null;
    }
}
