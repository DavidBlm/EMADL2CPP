package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.helper.FileLoader;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;

import java.util.List;

public class EfficientNet extends TrainAlgorithm {
    private ScalingFactorsGridSearch gridSearch;
    private NetworkScaler networkScaler;

    private ScalingFactors scalingFactors;
    private ArchitectureSymbol scaledArchitecture;


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
    public void execute(ArchitectureSymbol startNetwork) {
        setStartNetwork(startNetwork);
        createMissingObjects();
        findBestScalingFactors();
        scaleNetwork();
        saveNetwork();
    }

    private void createMissingObjects() {
        this.networkScaler = this.networkScaler == null ? new NetworkScaler() : this.networkScaler;
        this.gridSearch =
                this.gridSearch == null
                        ? new ScalingFactorsGridSearch(getStartNetwork(), getTrainConfiguration(), getTrainPipeline(),
                        this.networkScaler)
                        : this.gridSearch;
    }

    private void findBestScalingFactors() {
        this.scalingFactors = this.gridSearch.findScalingFactors();
    }

    private void scaleNetwork() {
        EfficientNetConfig config = getTrainConfiguration();
        this.scaledArchitecture = this.networkScaler.scale(getStartNetwork(), this.scalingFactors, config.getPhi());
    }

    private void saveNetwork() {
        EfficientNetConfig config = getTrainConfiguration();
        EfficientNetEmadlBuilder builder = new EfficientNetEmadlBuilder(this.scaledArchitecture, config);
        List<String> emadl = builder.getEmadl();
        String modelDirPath = "src/test/resources/models/efficientnet/";
        String modelName = "EfficientNetB" + config.getPhi();
        String modelFileEnding = ".emadl";
        String pathString = modelDirPath + modelName + modelFileEnding;
        new FileLoader().writeToFile(emadl, pathString);
    }
}
