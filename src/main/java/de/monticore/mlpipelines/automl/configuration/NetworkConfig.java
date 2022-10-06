package de.monticore.mlpipelines.automl.configuration;

public class NetworkConfig {

    private String networkPath;

    public NetworkConfig() {
    }

    public NetworkConfig(String networkPath) {
        this.networkPath = networkPath;
    }

    public String getNetworkPath() {
        return networkPath;
    }

    public void setNetworkPath(String networkPath) {
        this.networkPath = networkPath;
    }
}
