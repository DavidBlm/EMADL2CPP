package de.monticore.mlpipelines.pipelines;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;

import java.util.List;
import java.util.Map;

public abstract class Pipeline {

    protected final LearningMethod learningMethod;

    protected ASTConfLangCompilationUnit trainingConfiguration;

    protected ASTConfLangCompilationUnit pipelineConfiguration;

    protected EMAComponentInstanceSymbol neuralNetwork;

    private String networkName;

    protected ASTConfLangCompilationUnit searchSpace;

    protected ASTConfLangCompilationUnit hyperparamsOptConf;

    protected ASTConfLangCompilationUnit evaluationCriteria;

    protected List<Map<String, Object>> networkInstancesConfigs;

    protected EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics;

    protected String modelOutputDirectory = "/model/mnist.LeNetNetwork/";

    protected String modelOutputDirectoryWithDot = "." + modelOutputDirectory;

    protected Pipeline(final LearningMethod learningMethod) {
        this.learningMethod = learningMethod;
    }

    public void setNeuralNetwork(final EMAComponentInstanceSymbol neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
    }

    public EMAComponentInstanceSymbol getNeuralNetwork() {
        return neuralNetwork;
    }

    public void setTrainingConfiguration(final ASTConfLangCompilationUnit trainingConfiguration) {
        this.trainingConfiguration = trainingConfiguration;
    }

    public void setSearchSpace(ASTConfLangCompilationUnit trainingConfiguration, ASTConfLangCompilationUnit searchSpace) {
        String trainConfigName = trainingConfiguration.getConfiguration().getName();
        searchSpace.getConfiguration().setName(trainConfigName);
        this.searchSpace = searchSpace;
    }

    public void setHyperparamsOptConf(ASTConfLangCompilationUnit hyperparamsOptConf) {
        this.hyperparamsOptConf = hyperparamsOptConf;
    }

    public void setEvaluationCriteria(ASTConfLangCompilationUnit evaluationCriteria) {
        this.evaluationCriteria = evaluationCriteria;
    }

    public String getNetworkName() {
        return networkName;
    }

    public List<Map<String, Object>> getNetworkInstancesConfigs() {
        return networkInstancesConfigs;
    }

    public void setNetworkInstancesConfigs(List<Map<String, Object>> networkInstancesConfigs) {
        this.networkInstancesConfigs = networkInstancesConfigs;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public void setPipelineModelWithExecutionSemantics(final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics) {
        this.pipelineModelWithExecutionSemantics = pipelineModelWithExecutionSemantics;
    }

    public void setConfigurationModel(final ASTConfLangCompilationUnit trainingConfiguration) {
        this.trainingConfiguration = trainingConfiguration;
    }

    public abstract void execute();

    public void setPipelineConfiguration(final ASTConfLangCompilationUnit pipelineConfiguration) {
        this.pipelineConfiguration = pipelineConfiguration;
    }

    public void setModelOutputDirectory(final String modelOutputDirectory) {
        this.modelOutputDirectory = modelOutputDirectory;
        this.modelOutputDirectoryWithDot = "." + modelOutputDirectory;
    }

    public abstract float getTrainedAccuracy();
}
