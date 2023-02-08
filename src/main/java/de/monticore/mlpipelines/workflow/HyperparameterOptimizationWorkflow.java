package de.monticore.mlpipelines.workflow;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.io.paths.ModelPath;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.lang.monticar.emadl._symboltable.EMADLLanguage;
import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;
import de.monticore.mlpipelines.automl.hyperparameters.HyperparamsOptAlgGenerator;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.pipelines.PythonPipeline;
import de.monticore.parsing.EMADLParser;
import de.monticore.symbolmanagement.SymbolTableCreator;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.Names;

import java.io.IOException;
import java.nio.file.Paths;

public class HyperparameterOptimizationWorkflow extends AbstractWorkflow {

    public HyperparameterOptimizationWorkflow(final MontiAnnaContext applicationContext) {
        super(applicationContext);
    }
    @Override
    public void createPipeline(LearningMethod learningMethod) {
        final PythonPipeline pythonPipeline = new PythonPipeline(learningMethod);
        pythonPipeline.setMontiAnnaGenerator(this.montiAnnaGenerator);
        this.setPipeline(pythonPipeline);
    }

    @Override
    public void execute() throws IOException {
        // frontend
        final String rootModelName = Names.getSimpleName(montiAnnaContext.getRootModelName());
        final String pathToModelsDirectory = Paths.get(montiAnnaContext.getParentModelPath().toString(),
                getDirectoryPathSupplementFromComponentName(montiAnnaContext.getRootModelName())).toString();
        final String pathToRootModel = Paths.get(pathToModelsDirectory, rootModelName + ".emadl").toString();
        final ASTEMACompilationUnit rootEMADLComponent = new EMADLParser().parseModelIfExists(pathToRootModel);
        final ModelPath modelPath = new ModelPath(Paths.get(montiAnnaContext.getParentModelPath().toString()));
        final Scope emadlSymbolTable = SymbolTableCreator.createEMADLSymbolTable(rootEMADLComponent,
                new GlobalScope(modelPath, new EMADLLanguage()));
        final EMAComponentInstanceSymbol network = getNetworkTobeTrained(rootEMADLComponent, emadlSymbolTable);
        final String fullNetworkNameReplacedWithUnderscores = network.getFullName().replace(".", "_");
        final String pathToTrainingConfiguration = Paths.get(pathToModelsDirectory,
                fullNetworkNameReplacedWithUnderscores + ".conf").toString();
        final ASTConfLangCompilationUnit searchSpace = parseTrainingConfiguration(
                pathToTrainingConfiguration);

        final String pathToPipelineConfiguration = Paths.get(pathToModelsDirectory,
                fullNetworkNameReplacedWithUnderscores + "_pipeline.conf").toString();
        final ASTConfLangCompilationUnit pipelineConfiguration = parsePipelineConfiguration(
                pathToPipelineConfiguration);

        // Load AutoML pipeline configurations
        ASTConfLangCompilationUnit nasConf = this.getNASConfiguration(pathToModelsDirectory);
        ASTConfLangCompilationUnit hyperparamsOptConf = this.getAutoMLConfiguration(pathToModelsDirectory,
                "HyperparameterOpt.conf");
        ASTConfLangCompilationUnit evaluationCriteria = this.getAutoMLConfiguration(pathToModelsDirectory,
                "EvaluationCriteria.conf");

        // TODO: searchSpace validation not working (fix necessary)
        //final ConfigurationValidator configurationValidator = new ConfigurationValidator();
        //configurationValidator.validateTrainingConfiguration(trainingConfigurationSymbol);


        final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics = calculateExecutionSemantics();
        //backend
        generateBackendArtefactsIntoExperiment();
        final LearningMethod learningMethod = LearningMethod.SUPERVISED;
        createPipeline(learningMethod);
        pipeline.setPipelineConfiguration(pipelineConfiguration);
        pipeline.setPipelineModelWithExecutionSemantics(pipelineModelWithExecutionSemantics);
        pipeline.setNeuralNetwork(network);

        // TODO: Generate generic hyperparameter optimization algorithm class using Generator and conf file
        //SimulatedAnnealing hyperparamsOptAlg = new SimulatedAnnealing();
        //hyperparamsOptAlg.executeOptimization(pipeline, searchSpace, evaluationCriteria);

        AbstractHyperparameterAlgorithm hyperparamsOptAlg = HyperparamsOptAlgGenerator.generateAlgorithm(hyperparamsOptConf);
        hyperparamsOptAlg.executeOptimization(pipeline, searchSpace, evaluationCriteria);
    }
}
