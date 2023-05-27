package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.SerialCompositeElementSymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.helper.ArchitectureHelper;
import de.monticore.mlpipelines.automl.helper.FileLoader;
import de.monticore.mlpipelines.automl.helper.MathNumberExpressionWrapper;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EfficientNet extends NeuralArchitectureSearch {
    private ScalingFactorsGridSearch gridSearch;
    private NetworkScaler networkScaler;

    private ScalingFactors scalingFactors;
    private ArchitectureSymbol scaledArchitecture;
    private EfficientNetConfig config;

    List<OriginalLayerParams> parametersReference = new ArrayList<>();

    public EfficientNet() {
        super();
    }

    public EfficientNet(ScalingFactorsGridSearch gridSearch, NetworkScaler networkScaler) {
        super();
        this.gridSearch = gridSearch;
        this.networkScaler = networkScaler;
    }

    public ScalingFactorsGridSearch getGridSearch() {
        return this.gridSearch;
    }

    public NetworkScaler getNetworkScaler() {
        return this.networkScaler;
    }

    public ScalingFactors getScalingFactors() {
        return scalingFactors;
    }

    public ArchitectureSymbol getScaledArchitecture() {
        return scaledArchitecture;
    }

    public void setNetworkScaler(NetworkScaler networkScaler) {
        this.networkScaler = networkScaler;
    }

    @Override
    public ArchitectureSymbol execute(ArchitectureSymbol startNetwork) {
        this.config = new EfficientNetConfig(getTrainConfiguration());
        setStartNetwork(startNetwork);
        createMissingObjects();
        printNetwork();
        createOriginalParameterReference(startNetwork);
        findBestScalingFactors();
        scaleNetwork();
        saveNetwork();
        return scaledArchitecture;
    }

    public void createOriginalParameterReference(ArchitectureSymbol startNetwork){
        NetworkInstructionSymbol networkInstruction = startNetwork.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol networkInstructionBody = networkInstruction.getBody();
        List<ArchitectureElementSymbol> architectureElements = networkInstructionBody.getElements();
        int i=0;
        List<String> allowedLayers = Arrays.asList("residualBlock", "reductionBlock", "stem");
        for (ArchitectureElementSymbol architectureElement : architectureElements) {
            int layerRepetition = -1;
            int layerWidth = -1;
            int channelsIndex=1;
            if (allowedLayers.contains(architectureElement.getName())){
                ArrayList symbolExpressions = ArchitectureHelper.getExpressions(architectureElement);

                if (architectureElement.getName().equals("residualBlock")){
                    channelsIndex = 5;
                    int depthIndex = 3;
                    MathNumberExpressionSymbol mathNumberExpression = (MathNumberExpressionSymbol) symbolExpressions.get(
                            depthIndex);
                    MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(mathNumberExpression);
                    layerRepetition = expression.getIntValue();
                }

                MathNumberExpressionSymbol mathNumberExpression = (MathNumberExpressionSymbol) symbolExpressions.get(channelsIndex);
                MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(mathNumberExpression);
                layerWidth = expression.getIntValue();

            }
            parametersReference.add(new OriginalLayerParams(i, layerRepetition, layerWidth));
            i += 1;
        }
        this.gridSearch.parametersReference = parametersReference;
    }

    private void printNetwork() {
        EfficientNetEmadlBuilder builder = new EfficientNetEmadlBuilder(this.getStartNetwork(), config);
        List<String> emadl = builder.getEmadl();
        System.out.println("Original Network: " + emadl);
    }

    private void createMissingObjects() {
        this.networkScaler = this.networkScaler == null ? new NetworkScaler() : this.networkScaler;
        this.gridSearch =
                this.gridSearch == null
                        ? new ScalingFactorsGridSearch(getStartNetwork(), config, getTrainPipeline(),
                        this.networkScaler)
                        : this.gridSearch;
    }

    private void findBestScalingFactors() {
        this.scalingFactors = this.gridSearch.findBestScalingFactors();
    }

    private void scaleNetwork() {
        this.scaledArchitecture = this.networkScaler.scale(getStartNetwork(), this.scalingFactors, config.getPhi());
    }

    private void saveNetwork() {
        EfficientNetEmadlBuilder builder = new EfficientNetEmadlBuilder(this.scaledArchitecture, config);
        List<String> emadl = builder.getEmadl();
        String modelDirPath = "src/test/resources/models/efficientnet/";
        String modelName = "EfficientNetB" + config.getPhi();
        String modelFileEnding = ".emadl";
        String pathString = modelDirPath + modelName + modelFileEnding;
        new FileLoader().writeToFile(emadl, pathString);
    }
}
