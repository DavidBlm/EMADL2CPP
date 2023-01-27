/* (c) https://github.com/MontiCore/monticore */
package calculator;
conforms to artifacttag.DatasetArtifactTagSchema, artifacttag.LayerArtifactParameterTagSchema, 
            dltag.LayerPathParameterTagSchema;

tags NeuronalNetwork {
 tag Network with DatasetArtifact = {artifact = de.monticore.lang.monticar.datasets:mnist:1, type = HDF5};
 tag Network with LayerArtifactParameter = {artifact = de.monticore.lang.monticar.pretrained:simple-pretrained:1, id = mnist_pre};
}

