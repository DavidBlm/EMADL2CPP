import os
import h5py
import mxnet as mx
import logging
import sys
import numpy as np
import cv2
from mxnet import nd

class CNNDataLoader_mnist_mnistClassifier_net:
    _input_names_ = ['image']
    _output_names_ = ['predictions_label']

    def __init__(self):
        self._data_dir = "data/mnist.LeNetNetwork/"

    def load_data(self, train_batch_size, test_batch_size):
        train_h5, test_h5 = self.load_h5_files()

        train_data = {}
        data_mean = {}
        data_std = {}
        train_images = {}

        for input_name in self._input_names_:
            train_data[input_name] = train_h5[input_name]
            data_mean[input_name + '_'] = nd.array(train_h5[input_name][:].mean(axis=0))
            data_std[input_name + '_'] = nd.array(train_h5[input_name][:].std(axis=0) + 1e-5)

            if 'images' in train_h5:
                train_images = train_h5['images']

        train_label = {}
        index = 0
        for output_name in self._output_names_:
            train_label[index] = train_h5[output_name]
            index += 1

        train_iter = mx.io.NDArrayIter(data=train_data,
                                       label=train_label,
                                       batch_size=train_batch_size)

        train_test_iter = mx.io.NDArrayIter(data=train_data,
                                            label=train_label,
                                            batch_size=test_batch_size)

        test_iter = None

        if test_h5 != None:
            test_data = {}
            test_images = {}
            for input_name in self._input_names_:
                test_data[input_name] = test_h5[input_name]

                if 'images' in test_h5:
                    test_images = test_h5['images']

            test_label = {}
            index = 0
            for output_name in self._output_names_:
                test_label[index] = test_h5[output_name]
                index += 1

            test_iter = mx.io.NDArrayIter(data=test_data,
                                          label=test_label,
                                          batch_size=test_batch_size)

        return train_iter, train_test_iter, test_iter, data_mean, data_std, train_images, test_images

    def load_data_img(self, batch_size, img_size):
        train_h5, test_h5 = self.load_h5_files()
        width = img_size[0]
        height = img_size[1]

        comb_data = {}
        data_mean = {}
        data_std = {}

        for input_name in self._input_names_:
            train_data = train_h5[input_name][:]
            test_data = test_h5[input_name][:]

            train_shape = train_data.shape
            test_shape = test_data.shape

            comb_data[input_name] = mx.nd.zeros((train_shape[0]+test_shape[0], train_shape[1], width, height))
            for i, img in enumerate(train_data):
                img = img.transpose(1,2,0)
                comb_data[input_name][i] = cv2.resize(img, (width, height)).reshape((train_shape[1],width,height))
            for i, img in enumerate(test_data):
                img = img.transpose(1, 2, 0)
                comb_data[input_name][i+train_shape[0]] = cv2.resize(img, (width, height)).reshape((train_shape[1], width, height))

            data_mean[input_name + '_'] = nd.array(comb_data[input_name][:].mean(axis=0))
            data_std[input_name + '_'] = nd.array(comb_data[input_name][:].asnumpy().std(axis=0) + 1e-5)

        comb_label = {}
        for output_name in self._output_names_:
            train_labels = train_h5[output_name][:]
            test_labels = test_h5[output_name][:]
            comb_label[output_name] = np.append(train_labels, test_labels, axis=0)


        train_iter = mx.io.NDArrayIter(data=comb_data,
                                       label=comb_label,
                                       batch_size=batch_size)

        test_iter = None

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
