package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.trainalgorithms.TrainAlgorithm;

public class EfficientNet extends TrainAlgorithm {
    private ScalingFactorsGridSearch gridSearch;
    private NetworkScaler networkScaler;

    public EfficientNet() {
        super();
    }

    public EfficientNet(ScalingFactorsGridSearch gridSearch, NetworkScaler networkScaler) {
        super();
        this.gridSearch = gridSearch;
        this.networkScaler = networkScaler;
    }

    public ScalingFactorsGridSearch getGridSearch(){
        return this.gridSearch;
    }

    public NetworkScaler getNetworkScaler(){
        return this.networkScaler;
    }


    @Override
    public void train(ArchitectureSymbol startNetwork) {
        this.createMissingObjects();
        this.setStartNetwork(startNetwork);
    }

    private void createMissingObjects() {
        this.networkScaler = this.networkScaler == null ? new NetworkScaler() : this.networkScaler;
        this.gridSearch = this.gridSearch == null
                ? new ScalingFactorsGridSearch(
                        getStartNetwork(), getTrainConfiguration(), getTrainPipeline(), this.networkScaler)
                : this.gridSearch;
    }

}
