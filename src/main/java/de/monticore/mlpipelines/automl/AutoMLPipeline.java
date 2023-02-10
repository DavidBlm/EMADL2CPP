package de.monticore.mlpipelines.automl;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;
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
        this.trainPipeline.setTrainingConfiguration(trainingConfiguration);
        this.trainPipeline.setPipelineConfiguration(pipelineConfiguration);
        this.trainPipeline.setPipelineModelWithExecutionSemantics(pipelineModelWithExecutionSemantics);

        ArchitectureSymbol originalArchitecture = getArchitectureSymbol();
        executeNeuralArchitectureSearch(originalArchitecture);
//        executeHyperparameterOptimization(configuration);
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

//    private void executeHyperparameterOptimization(Configuration configuration) {
//        loadHyperparameterAlgorithm();
//        this.configuration = configuration;
//        hyperparameterAlgorithm.executeIteration();
//    }

    public void loadTrainAlgorithm() {
        neuralArchitectureSearchBuilder.setConfig(trainingConfiguration);
        neuralArchitectureSearchBuilder.setTrainingPipeline(trainPipeline);
        this.neuralArchitectureSearch = neuralArchitectureSearchBuilder.build();
    }

//    private void loadHyperparameterAlgorithm() {
//        HyperparameterOptConfig hyperparameterOptConfig = configuration.getHyperparameterOptConfig();
//        //TODO: Use HyperparameterAlgorithmBuilder to load the correct algorithm
//    }


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
