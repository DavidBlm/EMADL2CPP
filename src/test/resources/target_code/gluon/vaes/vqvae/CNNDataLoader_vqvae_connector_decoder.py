# (c) https://github.com/MontiCore/monticore
import os
import h5py
import mxnet as mx
import logging
import sys
import numpy as np
import importlib
from mxnet import nd

class CNNDataLoader_vqvae_connector_decoder:
    _input_names_ = ['encoding']
    _output_names_ = ['data_label']

    def __init__(self):
        self._data_dir = "/home/asmeta/.m2/repository/de/monticore/lang/monticar/datasets/vae-mnist/1/vae-mnist-1-dataset/training_data/"

    def load_data(self, batch_size, shuffle=False):
        train_h5, test_h5 = self.load_h5_files()

        train_data = {}
        data_mean = {}
        data_std = {}
        train_images = {}

        for input_name in self._input_names_:
            train_data["input"+str(self._input_names_.index(input_name))] = train_h5[input_name]
            train_dataset = train_h5[input_name]
            train_dataset_shape = train_data["input"+str(self._input_names_.index(input_name))].shape
            # slice_size limits the memory consumption, by only loading slices of size <500MB into memory
            slice_size = min(train_dataset_shape[0] - 1, int(500e6 / (train_h5[input_name][0].size * \
                train_h5[input_name][0].itemsize)))
            num_slices = max(1, int(train_h5[input_name].shape[0] / slice_size))
            mean = np.zeros(train_dataset_shape[1: ])
            std = np.zeros(train_dataset_shape[1: ])

            for i in range(int(train_dataset_shape[0] / slice_size)):
                mean += train_dataset[i * slice_size: (i + 1) * slice_size].mean(axis=0) / num_slices
                std += train_dataset[i * slice_size: (i + 1) * slice_size].std(axis=0) / num_slices
            if slice_size > train_dataset_shape[0] - 1:
                mean += train_dataset[num_slices * slice_size: ].mean(axis=0) / (slice_size - num_slices % slice_size)
                std += train_dataset[num_slices * slice_size: ].std(axis=0) / (slice_size - num_slices % slice_size)
            std += 1e-5

            data_mean[input_name + '_'] = nd.array(mean)
            data_std[input_name + '_'] = nd.array(std)

            if 'images' in train_h5:
                train_images = train_h5['images']

        train_label = {}
        index = 0
        for output_name in self._output_names_:
            train_label[index] = train_h5[output_name]
            index += 1

        train_iter = mx.io.NDArrayIter(data=train_data,
                                       label=train_label,
                                       batch_size=batch_size,
                                       shuffle=shuffle)

        test_iter = None

        if test_h5 != None:
            test_data = {}
            test_images = {}
            for input_name in self._input_names_:
                test_data["input"+str(self._input_names_.index(input_name))] = test_h5[input_name]

                if 'images' in test_h5:
                    test_images = test_h5['images']

            test_label = {}
            index = 0
            for output_name in self._output_names_:
                test_label[index] = test_h5[output_name]
                index += 1

            test_iter = mx.io.NDArrayIter(data=test_data,
                                          label=test_label,
                                          batch_size=batch_size)

        return train_iter, test_iter, data_mean, data_std, train_images, test_images

    def load_preprocessed_data(self, batch_size, preproc_lib, shuffle=False):
        train_h5, test_h5 = self.load_h5_files()

        wrapper = importlib.import_module(preproc_lib)
        instance = getattr(wrapper, preproc_lib)()
        instance.init()
        lib_head, _sep, tail = preproc_lib.rpartition('_')
        inp = getattr(wrapper, lib_head + "_input")()

        train_data = {}
        train_label = {}
        data_mean = {}
        data_std = {}
        train_images = {}

        shape_output = self.preprocess_data(instance, inp, 0, train_h5)
        train_len = len(train_h5[self._input_names_[0]])

        for input_name in self._input_names_:
            if type(getattr(shape_output, input_name + "_out")) == np.ndarray:
                cur_shape = (train_len,) + getattr(shape_output, input_name + "_out").shape
            else:
                cur_shape = (train_len, 1)
            train_data["input"+str(self._input_names_.index(input_name))] = mx.nd.zeros(cur_shape)
        for output_name in self._output_names_:
            if type(getattr(shape_output, output_name + "_out")) == nd.array:
                cur_shape = (train_len,) + getattr(shape_output, output_name + "_out").shape
            else:
                cur_shape = (train_len, 1)
            train_label[output_name] = mx.nd.zeros(cur_shape)

        for i in range(train_len):
            output = self.preprocess_data(instance, inp, i, train_h5)
            for input_name in self._input_names_:
                train_data["input"+str(self._input_names_.index(input_name))][i] = getattr(output, input_name + "_out")
            for output_name in self._output_names_:
                train_label[output_name][i] = getattr(shape_output, output_name + "_out")

        for input_name in self._input_names_:
            data_mean[input_name + '_'] = nd.array(train_data["input"+str(self._input_names_.index(input_name))][:].mean(axis=0))
            data_std[input_name + '_'] = nd.array(train_data["input"+str(self._input_names_.index(input_name))][:].asnumpy().std(axis=0) + 1e-5)

        if 'images' in train_h5:
            train_images = train_h5['images']

        train_iter = mx.io.NDArrayIter(data=train_data,
                                       label=train_label,
                                       batch_size=batch_size,
                                       shuffle=shuffle)

        test_data = {}
        test_label = {}

        shape_output = self.preprocess_data(instance, inp, 0, test_h5)
        test_len = len(test_h5[self._input_names_[0]])

        for input_name in self._input_names_:
            if type(getattr(shape_output, input_name + "_out")) == np.ndarray:
                cur_shape = (test_len,) + getattr(shape_output, input_name + "_out").shape
            else:
                cur_shape = (test_len, 1)
            test_data["input"+str(self._input_names_.index(input_name))] = mx.nd.zeros(cur_shape)
        for output_name in self._output_names_:
            if type(getattr(shape_output, output_name + "_out")) == nd.array:
                cur_shape = (test_len,) + getattr(shape_output, output_name + "_out").shape
            else:
                cur_shape = (test_len, 1)
            test_label[output_name] = mx.nd.zeros(cur_shape)

        for i in range(test_len):
            output = self.preprocess_data(instance, inp, i, test_h5)
            for input_name in self._input_names_:
                test_data["input"+str(self._input_names_.index(input_name))][i] = getattr(output, input_name + "_out")
            for output_name in self._output_names_:
                test_label[output_name][i] = getattr(shape_output, output_name + "_out")

        test_images = {}
        if 'images' in test_h5:
            test_images = test_h5['images']

        test_iter = mx.io.NDArrayIter(data=test_data,
                                       label=test_label,
                                       batch_size=batch_size)

        return train_iter, test_iter, data_mean, data_std, train_images, test_images

    def preprocess_data(self, instance_wrapper, input_wrapper, index, data_h5):
        for input_name in self._input_names_:
            data = data_h5[input_name][index]
            attr = getattr(input_wrapper, input_name)
            if (type(data)) == np.ndarray:
                data = np.asfortranarray(data).astype(attr.dtype)
            else:
                data = type(attr)(data)
            setattr(input_wrapper, input_name, data)
        for output_name in self._output_names_:
            data = data_h5[output_name][index]
            attr = getattr(input_wrapper, output_name)
            if (type(data)) == np.ndarray:
                data = np.asfortranarray(data).astype(attr.dtype)
            else:
                data = type(attr)(data)
            setattr(input_wrapper, output_name, data)
        return instance_wrapper.execute(input_wrapper)

    def load_h5_files(self, learning_method=""):
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

            if learning_method != "vae":
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

                if learning_method != "vae":
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

    def load_vae_data(self, batch_size, shuffle=False, input_names=[] ):
        self._input_names_ = input_names
        train_h5, test_h5 = self.load_h5_files(learning_method="vae")

        train_data = {}
        data_mean = {}
        data_std = {}
        train_images = {}
        train_label = {}

        for input_name in self._input_names_:
            if input_name == "label":
                train_label[input_name] = train_h5[input_name]
            else:
                train_data[input_name] = train_h5[input_name]
                train_dataset = train_h5[input_name]
                train_dataset_shape = train_data[input_name].shape
                # slice_size limits the memory consumption, by only loading slices of size <500MB into memory
                slice_size = min(train_dataset_shape[0] - 1, int(500e6 / (train_h5[input_name][0].size * \
                train_h5[input_name][0].itemsize)))
                num_slices = max(1, int(train_h5[input_name].shape[0] / slice_size))
                mean = np.zeros(train_dataset_shape[1:])
                std = np.zeros(train_dataset_shape[1:])

                for i in range(int(train_dataset_shape[0] / slice_size)):
                    mean += train_dataset[i * slice_size: (i + 1) * slice_size].mean(axis=0) / num_slices
                    std += train_dataset[i * slice_size: (i + 1) * slice_size].std(axis=0) / num_slices
                if slice_size > train_dataset_shape[0] - 1:
                    mean += train_dataset[num_slices * slice_size:].mean(axis=0) / (
                    slice_size - num_slices % slice_size)
                    std += train_dataset[num_slices * slice_size:].std(axis=0) / (slice_size - num_slices % slice_size)
                std += 1e-5

                data_mean[input_name + '_'] = nd.array(mean)
                data_std[input_name + '_'] = nd.array(std)

        if 'images' in train_h5:
            train_images = train_h5['images']

        train_iter = mx.io.NDArrayIter(data=train_data,
                                       label=train_label,
                                       batch_size=batch_size,
                                       shuffle=shuffle)

        test_iter = None

        if test_h5 != None:
            test_data = {}
            test_label = {}
            test_images = {}
            for input_name in self._input_names_:
                if input_name == "label":
                    test_label[input_name] = test_h5[input_name]
                else:
                    test_data[input_name] = test_h5[input_name]

            if 'images' in test_h5:
                test_images = test_h5['images']


            test_iter = mx.io.NDArrayIter(data=test_data,
                                          label=test_label,
                                          batch_size=batch_size)

        return train_iter, test_iter, data_mean, data_std, train_images, test_images