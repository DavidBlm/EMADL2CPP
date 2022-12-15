package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;

public abstract class SequentialAlgorithm extends AbstractHyperparameterAlgorithm {

    private ASTConfLangCompilationUnit currentHyperparameters;

    public ASTConfLangCompilationUnit getCurrentHyperparameters() {
        return this.currentHyperparameters;
    }

    public void setCurrentHyperparameters(ASTConfLangCompilationUnit currentHyperparameters) {
        this.currentHyperparameters = currentHyperparameters;
    }
}
