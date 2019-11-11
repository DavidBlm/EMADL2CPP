import mxnet as mx
import numpy as np
from mxnet import gluon


class ZScoreNormalization(gluon.HybridBlock):
    def __init__(self, data_mean, data_std, **kwargs):
        super(ZScoreNormalization, self).__init__(**kwargs)
        with self.name_scope():
            self.data_mean = self.params.get('data_mean', shape=data_mean.shape,
                init=mx.init.Constant(data_mean.asnumpy().tolist()), differentiable=False)
            self.data_std = self.params.get('data_std', shape=data_mean.shape,
                init=mx.init.Constant(data_std.asnumpy().tolist()), differentiable=False)

    def hybrid_forward(self, F, x, data_mean, data_std):
        x = F.broadcast_sub(x, data_mean)
        x = F.broadcast_div(x, data_std)
        return x


class Padding(gluon.HybridBlock):
    def __init__(self, padding, **kwargs):
        super(Padding, self).__init__(**kwargs)
        with self.name_scope():
            self.pad_width = padding

    def hybrid_forward(self, F, x):
        x = F.pad(data=x,
            mode='constant',
            pad_width=self.pad_width,
            constant_value=0)
        return x


class NoNormalization(gluon.HybridBlock):
    def __init__(self, **kwargs):
        super(NoNormalization, self).__init__(**kwargs)

    def hybrid_forward(self, F, x):
        return x


class Reshape(gluon.HybridBlock):
    def __init__(self, shape, **kwargs):
        super(Reshape, self).__init__(**kwargs)
        with self.name_scope():
            self.shape = shape

    def hybrid_forward(self, F, x):
        return F.reshape(data=x, shape=self.shape)


class CustomRNN(gluon.HybridBlock):
    def __init__(self, hidden_size, num_layers, bidirectional, **kwargs):
        super(CustomRNN, self).__init__(**kwargs)
        with self.name_scope():
            self.rnn = gluon.rnn.RNN(hidden_size=hidden_size, num_layers=num_layers,
                                     bidirectional=bidirectional, activation='tanh', layout='NTC')

    def hybrid_forward(self, F, data, state0):
        output, [state0] = self.rnn(data, [F.swapaxes(state0, 0, 1)])
        return output, F.swapaxes(state0, 0, 1)


class CustomLSTM(gluon.HybridBlock):
    def __init__(self, hidden_size, num_layers, bidirectional, **kwargs):
        super(CustomLSTM, self).__init__(**kwargs)
        with self.name_scope():
            self.lstm = gluon.rnn.LSTM(hidden_size=hidden_size, num_layers=num_layers,
                                       bidirectional=bidirectional, layout='NTC')

    def hybrid_forward(self, F, data, state0, state1):
        output, [state0, state1] = self.lstm(data, [F.swapaxes(state0, 0, 1), F.swapaxes(state1, 0, 1)])
        return output, F.swapaxes(state0, 0, 1), F.swapaxes(state1, 0, 1)


class CustomGRU(gluon.HybridBlock):
    def __init__(self, hidden_size, num_layers, bidirectional, **kwargs):
        super(CustomGRU, self).__init__(**kwargs)
        with self.name_scope():
            self.gru = gluon.rnn.GRU(hidden_size=hidden_size, num_layers=num_layers,
                                     bidirectional=bidirectional, layout='NTC')

    def hybrid_forward(self, F, data, state0):
        output, [state0] = self.gru(data, [F.swapaxes(state0, 0, 1)])
        return output, F.swapaxes(state0, 0, 1)


class Net_0(gluon.HybridBlock):
    def __init__(self, data_mean=None, data_std=None, **kwargs):
        super(Net_0, self).__init__(**kwargs)
        with self.name_scope():
            if data_mean:
                assert(data_std)
                self.input_normalization_image_ = ZScoreNormalization(data_mean=data_mean['image_'],
                                                                               data_std=data_std['image_'])
            else:
                self.input_normalization_image_ = NoNormalization()

            self.conv1_padding = Padding(padding=(0,0,0,0,2,2,2,2))
            self.conv1_ = gluon.nn.Conv2D(channels=20,
                kernel_size=(5,5),
                strides=(1,1),
                use_bias=True)
            # conv1_, output shape: {[20,28,28]}

            self.pool1_ = gluon.nn.MaxPool2D(
                pool_size=(2,2),
                strides=(2,2))
            # pool1_, output shape: {[20,14,14]}

            self.conv2_padding = Padding(padding=(0,0,0,0,2,2,2,2))
            self.conv2_ = gluon.nn.Conv2D(channels=50,
                kernel_size=(5,5),
                strides=(1,1),
                use_bias=True)
            # conv2_, output shape: {[50,14,14]}

            self.pool2_ = gluon.nn.MaxPool2D(
                pool_size=(2,2),
                strides=(2,2))
            # pool2_, output shape: {[50,7,7]}

            self.fc2_ = gluon.nn.Dense(units=500, use_bias=True, flatten=True)
            # fc2_, output shape: {[500,1,1]}

            self.relu2_ = gluon.nn.Activation(activation='relu')
            self.fc3_ = gluon.nn.Dense(units=10, use_bias=True, flatten=True)
            # fc3_, output shape: {[10,1,1]}


            pass

    def hybrid_forward(self, F, image_):
        image_ = self.input_normalization_image_(image_)
        conv1_padding = self.conv1_padding(image_)
        conv1_ = self.conv1_(conv1_padding)
        pool1_ = self.pool1_(conv1_)
        conv2_padding = self.conv2_padding(pool1_)
        conv2_ = self.conv2_(conv2_padding)
        pool2_ = self.pool2_(conv2_)
        fc2_ = self.fc2_(pool2_)
        relu2_ = self.relu2_(fc2_)
        fc3_ = self.fc3_(relu2_)
        softmax3_ = F.softmax(fc3_, axis=-1)
        predictions_ = softmax3_

        return predictions_

