/* (c) https://github.com/MontiCore/monticore */
package calculator;
conforms to artifacttag.DatasetArtifactTagSchema, artifacttag.LayerArtifactParameterTagSchema, 
            dltag.LayerPathParameterTagSchema;

tags NeuronalNetwork {
 tag Network with DatasetArtifact = {artifact = de.monticore.lang.monticar.datasets:mnist:1, type = HDF5};
 tag Network with LayerPathParameter = {path = src/test/resources/models/ModularCNN/modularNetworkSimplePretrained/resources/pretrainedNet1, id = mnist_pre};
}

