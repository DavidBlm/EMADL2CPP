import mxnet as mx
import logging
import os
import shutil

from CNNNet_infoGAN_infoGANConnector_predictor import Net_0

class CNNCreator_infoGAN_infoGANConnector_predictor:
    _model_dir_ = "model/infoGAN.InfoGANGenerator/"
    _model_prefix_ = "model"

    def __init__(self):
        self.weight_initializer = mx.init.Normal()
        self.networks = {}
        self._weights_dir_ = None

    def load(self, context):
        earliestLastEpoch = None

        for i, network in self.networks.items():
            lastEpoch = 0
            param_file = None

            try:
                os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + "_newest-0000.params")
            except OSError:
                pass
            try:
                os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + "_newest-symbol.json")
            except OSError:
                pass

            if os.path.isdir(self._model_dir_):
                for file in os.listdir(self._model_dir_):
                    if ".params" in file and self._model_prefix_ + "_" + str(i) in file:
                        epochStr = file.replace(".params","").replace(self._model_prefix_ + "_" + str(i) + "-","")
                        epoch = int(epochStr)
                        if epoch > lastEpoch:
                            lastEpoch = epoch
                            param_file = file
            if param_file is None:
                earliestLastEpoch = 0
            else:
                logging.info("Loading checkpoint: " + param_file)
                network.load_parameters(self._model_dir_ + param_file)

                if earliestLastEpoch == None or lastEpoch < earliestLastEpoch:
                    earliestLastEpoch = lastEpoch

        return earliestLastEpoch

    def load_pretrained_weights(self, context):
        if os.path.isdir(self._model_dir_):
            shutil.rmtree(self._model_dir_)
        if self._weights_dir_ is not None:
            for i, network in self.networks.items():
                # param_file = self._model_prefix_ + "_" + str(i) + "_newest-0000.params"
                param_file = None
                if os.path.isdir(self._weights_dir_):
                    lastEpoch = 0

                    for file in os.listdir(self._weights_dir_):

                        if ".params" in file and self._model_prefix_ + "_" + str(i) in file:
                            epochStr = file.replace(".params","").replace(self._model_prefix_ + "_" + str(i) + "-","")
                            epoch = int(epochStr)
                            if epoch > lastEpoch:
                                lastEpoch = epoch
                                param_file = file
                    logging.info("Loading pretrained weights: " + self._weights_dir_ + param_file)
                    network.load_parameters(self._weights_dir_ + param_file, allow_missing=True, ignore_extra=True)
                else:
                    logging.info("No pretrained weights available at: " + self._weights_dir_ + param_file)

    def construct(self, context, data_mean=None, data_std=None):
        self.networks[0] = Net_0(data_mean=data_mean, data_std=data_std)
        self.networks[0].collect_params().initialize(self.weight_initializer, ctx=context)
        self.networks[0].hybridize()
        self.networks[0](mx.nd.zeros((1, 62,), ctx=context), mx.nd.zeros((1, 10,), ctx=context))

        if not os.path.exists(self._model_dir_):
            os.makedirs(self._model_dir_)

        for i, network in self.networks.items():
            network.export(self._model_dir_ + self._model_prefix_ + "_" + str(i), epoch=0)

    def getInputs(self):
        inputs = {}
        input_dimensions = (62,)
        input_domains = (float,0.0,1.0,)
        inputs["noise_"] = input_domains + (input_dimensions,)
        input_dimensions = (10,)
        input_domains = (int,0.0,9.0,)
        inputs["c1_"] = input_domains + (input_dimensions,)
        return inputs

    def getOutputs(self):
        outputs = {}
        output_dimensions = (1,64,64,)
        output_domains = (float,-1.0,1.0,)
        outputs["data_"] = output_domains + (output_dimensions,)
        return outputs
