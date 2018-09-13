package de.monticore.lang.monticar.emadl.generator;


import de.monticore.lang.monticar.cnnarch.CNNArchGenerator;
import de.monticore.lang.monticar.cnnarch.mxnetgenerator.CNNArch2MxNet;
import de.monticore.lang.monticar.cnnarch.caffe2generator.CNNArch2Caffe2;
import de.monticore.lang.monticar.cnnarch.mxnetgenerator.CNNTrain2MxNet;
import de.monticore.lang.monticar.cnnarch.caffe2generator.CNNTrain2Caffe2;
import de.monticore.lang.monticar.cnntrain.CNNTrainGenerator;

import java.util.Optional;

public enum Backend {
    MXNET{
        @Override
        public CNNArchGenerator getCNNArchGenerator() {
            return new CNNArch2MxNet();
        }
        @Override
        public CNNTrainGenerator getCNNTrainGenerator() {
            return new CNNTrain2MxNet();
        }
    },
    CAFFE2{
        @Override
        public CNNArchGenerator getCNNArchGenerator() {
            return new CNNArch2Caffe2();
        }
        @Override
        public CNNTrainGenerator getCNNTrainGenerator() {
            return new CNNTrain2Caffe2();
        }
    };

    public abstract CNNArchGenerator getCNNArchGenerator();
    public abstract CNNTrainGenerator getCNNTrainGenerator();

    public static Optional<Backend> getBackendFromString(String backend){
        switch (backend){
            case "MXNET":
                return Optional.of(MXNET);

            case "CAFFE2":
                return Optional.of(CAFFE2);

            default:
                return Optional.empty();
        }
    }
}
