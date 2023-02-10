package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.configuration.EfficientNetConfig;
import de.monticore.mlpipelines.automl.emadlprinter.EmadlPrettyPrinter;
import de.monticore.mlpipelines.automl.helper.FileLoader;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearch;

import java.util.Arrays;
import java.util.List;

public class EfficientNet extends NeuralArchitectureSearch {
    private ScalingFactorsGridSearch gridSearch;
    private NetworkScaler networkScaler;

    private ScalingFactors scalingFactors;
    private ArchitectureSymbol scaledArchitecture;
    private EfficientNetConfig config;


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
        findBestScalingFactors();
        scaleNetwork();
        saveNetwork();
        return scaledArchitecture;
    }

    private void printNetwork() {
        EmadlPrettyPrinter builder = new EmadlPrettyPrinter();
        List<String> emadl = Arrays.asList(builder.prettyPrint(getStartNetwork()).split("\n"));
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
        EmadlPrettyPrinter builder = new EmadlPrettyPrinter();
        List<String> emadl = Arrays.asList(builder.prettyPrint(scaledArchitecture).split("\n"));
        String modelDirPath = "src/test/resources/models/efficientnet/";
        String modelName = "EfficientNetB" + config.getPhi();
        String modelFileEnding = ".emadl";
        String pathString = modelDirPath + modelName + modelFileEnding;
        new FileLoader().writeToFile(emadl, pathString);
    }
}
