# (c) https://github.com/MontiCore/monticore  
import os
import h5py
import mxnet as mx
import logging
import sys
from mxnet import nd

class CNNDataLoader_torcs_agent_torcsAgent_dqn:
    _input_names_ = ['state']
    _output_names_ = ['qvalues_label']

    def __init__(self):
        self._data_dir = "data/"

    def load_data(self, batch_size):
        train_h5, test_h5 = self.load_h5_files()

        train_data = {}
        data_mean = {}
        data_std = {}

        for input_name in self._input_names_:
            train_data[input_name] = train_h5[input_name]
            data_mean[input_name] = nd.array(train_h5[input_name][:].mean(axis=0))
            data_std[input_name] = nd.array(train_h5[input_name][:].std(axis=0) + 1e-5)

        train_label = {}
        for output_name in self._output_names_:
            train_label[output_name] = train_h5[output_name]

        train_iter = mx.io.NDArrayIter(data=train_data,
                                       label=train_label,
                                       batch_size=batch_size)

        test_iter = None

        if test_h5 != None:
            test_data = {}
            for input_name in self._input_names_:
                test_data[input_name] = test_h5[input_name]

            test_label = {}
            for output_name in self._output_names_:
                test_label[output_name] = test_h5[output_name]

            test_iter = mx.io.NDArrayIter(data=test_data,
                                          label=test_label,
                                          batch_size=batch_size)

        return train_iter, test_iter, data_mean, data_std

    def load_h5_files(self):
        train_h5 = None
        test_h5 = None
        train_path = self._data_dir + "train.h5"
        test_path = self._data_dir + "test.h5"

        if os.path.isfile(train_path):
            train_h5 = h5py.File(train_path, 'r')

            for input_name in self._input_names_:
                if not input_name in train_h5:
                    logging.error("The HDF5 file '" + os.path.abspath(train_path) + "' has to contain the dataset "
                                  + "'" + input_name + "'")
                    sys.exit(1)

            for output_name in self._output_names_:
                if not output_name in train_h5:
                    logging.error("The HDF5 file '" + os.path.abspath(train_path) + "' has to contain the dataset "
                                  + "'" + output_name + "'")
                    sys.exit(1)

            if os.path.isfile(test_path):
                test_h5 = h5py.File(test_path, 'r')

                for input_name in self._input_names_:
                    if not input_name in test_h5:
                        logging.error("The HDF5 file '" + os.path.abspath(test_path) + "' has to contain the dataset "
                                      + "'" + input_name + "'")
                        sys.exit(1)

                for output_name in self._output_names_:
                    if not output_name in test_h5:
                        logging.error("The HDF5 file '" + os.path.abspath(test_path) + "' has to contain the dataset "
                                      + "'" + output_name + "'")
                        sys.exit(1)
            else:
                logging.warning("Couldn't load test set. File '" + os.path.abspath(test_path) + "' does not exist.")

            return train_h5, test_h5
        else:
            logging.error("Data loading failure. File '" + os.path.abspath(train_path) + "' does not exist.")
            sys.exit(1)
