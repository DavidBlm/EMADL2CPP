package de.monticore.mlpipelines.automl;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;
import de.monticore.mlpipelines.automl.hyperparameters.HyperparamsOptAlgGenerator;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearch;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearchBuilder;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.symboltable.CommonScope;

public class AutoMLPipeline extends Pipeline {
    private Pipeline trainPipeline;
    private ArchitectureSymbol architecture;
    private NeuralArchitectureSearch neuralArchitectureSearch;
    private AbstractHyperparameterAlgorithm hyperparameterAlgorithm;
    private NeuralArchitectureSearchBuilder neuralArchitectureSearchBuilder;

    public AutoMLPipeline(final LearningMethod learningMethod) {
        super(learningMethod);
        neuralArchitectureSearchBuilder = new NeuralArchitectureSearchBuilder();
    }

    public void setTrainPipeline(Pipeline trainPipeline) {
        this.trainPipeline = trainPipeline;
    }

    @Override
    public void execute() {
        this.trainPipeline.setNeuralNetwork(neuralNetwork);
        this.trainPipeline.setNetworkName(getNetworkName());
        this.trainPipeline.setTrainingConfiguration(trainingConfiguration);
        this.trainPipeline.setPipelineConfiguration(pipelineConfiguration);
        this.trainPipeline.setPipelineModelWithExecutionSemantics(pipelineModelWithExecutionSemantics);

        //Measure the time for hyperparameter optimization
        long startTime = System.currentTimeMillis();
        ArchitectureSymbol originalArchitecture = getArchitectureSymbol();
        executeNeuralArchitectureSearch(originalArchitecture);
        executeHyperparameterOptimization(hyperparamsOptConf);
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        System.out.println("Hyperparameter optimization took " + duration + " milliseconds");

        System.out.println();
        System.out.println();
        System.out.println("Final Training");
        trainPipeline.setNeuralNetwork(neuralNetwork);
        trainPipeline.execute();
    }

    private ArchitectureSymbol getArchitectureSymbol() {
        CommonScope spannedScope = (CommonScope) neuralNetwork.getSpannedScope();
        CommonScope subScope = (CommonScope) spannedScope.getSubScopes().get(0);
        ArchitectureSymbol originalArchitecture = (ArchitectureSymbol) subScope.getSpanningSymbol().get();
        return originalArchitecture;
    }

    @Override
    public float getTrainedAccuracy() {
        return 0;
    }

    private void executeNeuralArchitectureSearch(ArchitectureSymbol originalArchitecture) {
        loadTrainAlgorithm();
        architecture = neuralArchitectureSearch.execute(originalArchitecture);
    }

    private void executeHyperparameterOptimization(ASTConfLangCompilationUnit hyperparamsOptConf) {
        hyperparameterAlgorithm = HyperparamsOptAlgGenerator.generateAlgorithm(hyperparamsOptConf);
        hyperparameterAlgorithm.executeOptimization(this.trainPipeline, this.searchSpace, this.evaluationCriteria);
    }

    public void loadTrainAlgorithm() {
        neuralArchitectureSearchBuilder.setConfig(trainingConfiguration);
        neuralArchitectureSearchBuilder.setTrainingPipeline(trainPipeline);
        this.neuralArchitectureSearch = neuralArchitectureSearchBuilder.build();
    }

    public Pipeline getTrainPipeline() {
        return trainPipeline;
    }

    public NeuralArchitectureSearch getTrainAlgorithm() {
        return neuralArchitectureSearch;
    }

    public ArchitectureSymbol getArchitecture() {
        return architecture;
    }

    public NeuralArchitectureSearchBuilder getTrainAlgorithmBuilder() {
        return neuralArchitectureSearchBuilder;
    }

    public void setTrainAlgorithmBuilder(NeuralArchitectureSearchBuilder neuralArchitectureSearchBuilder) {
        this.neuralArchitectureSearchBuilder = neuralArchitectureSearchBuilder;
    }
}
