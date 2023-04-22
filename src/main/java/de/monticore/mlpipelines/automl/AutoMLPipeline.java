package de.monticore.mlpipelines.automl;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;
import de.monticore.mlpipelines.automl.hyperparameters.HyperparamsOptAlgGenerator;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearch;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearchBuilder;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.symboltable.CommonScope;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> instanceConfigs = this.getNetworkInstancesConfigs();

        for (Map<String, Object> configMap : instanceConfigs) {
            EMAComponentInstanceSymbol neuralNetwork = (EMAComponentInstanceSymbol) configMap.get("network");
            String networkName = (String) configMap.get("networkName");
            ASTConfLangCompilationUnit trainingConfiguration = (ASTConfLangCompilationUnit) configMap.get("trainingConfiguration");
            ASTConfLangCompilationUnit pipelineConfiguration = (ASTConfLangCompilationUnit) configMap.get("pipelineConfiguration");
            ASTConfLangCompilationUnit searchSpace = (ASTConfLangCompilationUnit) configMap.get("SearchSpace");
            ASTConfLangCompilationUnit hyperparamsOptConf = (ASTConfLangCompilationUnit) configMap.get("HyperparameterOpt");
            ASTConfLangCompilationUnit evaluationCriteria = (ASTConfLangCompilationUnit) configMap.get("EvaluationCriteria");

            executeInstance(
                neuralNetwork, networkName, trainingConfiguration, pipelineConfiguration,
                searchSpace, hyperparamsOptConf, evaluationCriteria
            );
        }
    }

    private void executeInstance(
            EMAComponentInstanceSymbol neuralNetwork,
            String networkName,
            ASTConfLangCompilationUnit trainingConfiguration,
            ASTConfLangCompilationUnit pipelineConfiguration,
            ASTConfLangCompilationUnit searchSpace,
            ASTConfLangCompilationUnit hyperparamsOptConf,
            ASTConfLangCompilationUnit evaluationCriteria) {
        this.trainPipeline.setNeuralNetwork(neuralNetwork);
        this.trainPipeline.setNetworkName(networkName);
        this.trainPipeline.setTrainingConfiguration(trainingConfiguration);
        this.trainPipeline.setPipelineConfiguration(pipelineConfiguration);
        this.trainPipeline.setPipelineModelWithExecutionSemantics(pipelineModelWithExecutionSemantics);
        this.setConfigurationModel(trainingConfiguration);
        this.setSearchSpace(trainingConfiguration, searchSpace);
        this.setHyperparamsOptConf(hyperparamsOptConf);
        this.setEvaluationCriteria(evaluationCriteria);

        this.trainPipeline.setModelOutputDirectory(String.format("/model/%s/", networkName));

        Log.info(String.format("Executing optimization for instance: %s", networkName), AutoMLPipeline.class.getName());
        ArchitectureSymbol originalArchitecture = getArchitectureSymbol();
        executeNeuralArchitectureSearch(originalArchitecture);
        executeHyperparameterOptimization(hyperparamsOptConf);
        trainPipeline.setNeuralNetwork(neuralNetwork);
        trainPipeline.execute();
    }

    private ArchitectureSymbol getArchitectureSymbol() {
        CommonScope spannedScope = (CommonScope) this.trainPipeline.getNeuralNetwork().getSpannedScope();
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
