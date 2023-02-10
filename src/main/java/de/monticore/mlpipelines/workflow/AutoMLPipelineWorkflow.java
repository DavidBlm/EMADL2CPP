package de.monticore.mlpipelines.workflow;

import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.automl.AutoMLPipeline;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.pipelines.PythonPipeline;

public class AutoMLPipelineWorkflow extends AbstractWorkflow {
    public AutoMLPipelineWorkflow(final MontiAnnaContext applicationContext) {
        super(applicationContext);
    }

    public void createPipeline(final LearningMethod learningMethod) {
        final PythonPipeline pythonPipeline = new PythonPipeline(learningMethod);
        pythonPipeline.setMontiAnnaGenerator(this.montiAnnaGenerator);

        final AutoMLPipeline autoMLPipeline = new AutoMLPipeline(learningMethod);
        autoMLPipeline.setTrainPipeline(pythonPipeline);

        this.setPipeline(autoMLPipeline);
    }

//    @Override
//    public void execute() throws IOException {
//        // frontend
//        final String rootModelName = Names.getSimpleName(montiAnnaContext.getRootModelName());
//        final String pathToModelsDirectory = Paths.get(montiAnnaContext.getParentModelPath().toString(),
//                getDirectoryPathSupplementFromComponentName(montiAnnaContext.getRootModelName())).toString();
//        final String pathToRootModel = Paths.get(pathToModelsDirectory, rootModelName + ".emadl").toString();
//        final ASTEMACompilationUnit rootEMADLComponent = new EMADLParser().parseModelIfExists(pathToRootModel);
//        final ModelPath modelPath = new ModelPath(Paths.get(montiAnnaContext.getParentModelPath().toString()));
//        final Scope emadlSymbolTable = SymbolTableCreator.createEMADLSymbolTable(rootEMADLComponent,
//                new GlobalScope(modelPath, new EMADLLanguage()));
//        final EMAComponentInstanceSymbol network = getNetworkTobeTrained(rootEMADLComponent, emadlSymbolTable);
//        final String fullNetworkNameReplacedWithUnderscores = network.getFullName().replace(".", "_");
//        final String pathToTrainingConfiguration = Paths.get(pathToModelsDirectory,
//                fullNetworkNameReplacedWithUnderscores + ".conf").toString();
//        final ASTConfLangCompilationUnit searchSpace = parseTrainingConfiguration(
//                pathToTrainingConfiguration);
//
//
//        SimulatedAnnealing hyperparamsOptAlg = new SimulatedAnnealing();
//        ASTConfLangCompilationUnit trainingConfiguration = hyperparamsOptAlg.getInitialHyperparams(searchSpace);
//
//        //final ConfigurationSymbol trainingConfigurationSymbol = trainingConfiguration.getConfiguration()
//        //        .getConfigurationSymbol();
//        final String pathToPipelineConfiguration = Paths.get(pathToModelsDirectory,
//                fullNetworkNameReplacedWithUnderscores + "_pipeline.conf").toString();
//        final ASTConfLangCompilationUnit pipelineConfiguration = parsePipelineConfiguration(
//                pathToPipelineConfiguration);
//
//        // TODO: trainingConfiguration validation not working (fix necessary)
//        //final ConfigurationValidator configurationValidator = new ConfigurationValidator();
//        //configurationValidator.validateTrainingConfiguration(trainingConfigurationSymbol);
//
//
//        final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics = calculateExecutionSemantics();
//        //backend
//        generateBackendArtefactsIntoExperiment();
//        final LearningMethod learningMethod = LearningMethod.SUPERVISED;
//        createPipeline(learningMethod);
//        pipeline.setConfigurationModel(trainingConfiguration);
//        pipeline.setPipelineConfiguration(pipelineConfiguration);
//        pipeline.setPipelineModelWithExecutionSemantics(pipelineModelWithExecutionSemantics);
//        pipeline.setNeuralNetwork(network);
//        pipeline.execute();
//    }
}
