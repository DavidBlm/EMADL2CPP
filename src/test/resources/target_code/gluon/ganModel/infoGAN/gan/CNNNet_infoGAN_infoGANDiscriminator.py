import mxnet as mx
import numpy as np
import math
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
    def __init__(self, hidden_size, num_layers, dropout, bidirectional, **kwargs):
        super(CustomRNN, self).__init__(**kwargs)
        with self.name_scope():
            self.rnn = gluon.rnn.RNN(hidden_size=hidden_size, num_layers=num_layers, dropout=dropout,
                                     bidirectional=bidirectional, activation='tanh', layout='NTC')

    def hybrid_forward(self, F, data, state0):
        output, [state0] = self.rnn(data, [F.swapaxes(state0, 0, 1)])
        return output, F.swapaxes(state0, 0, 1)


class CustomLSTM(gluon.HybridBlock):
    def __init__(self, hidden_size, num_layers, dropout, bidirectional, **kwargs):
        super(CustomLSTM, self).__init__(**kwargs)
        with self.name_scope():
            self.lstm = gluon.rnn.LSTM(hidden_size=hidden_size, num_layers=num_layers, dropout=dropout,
                                       bidirectional=bidirectional, layout='NTC')

    def hybrid_forward(self, F, data, state0, state1):
        output, [state0, state1] = self.lstm(data, [F.swapaxes(state0, 0, 1), F.swapaxes(state1, 0, 1)])
        return output, F.swapaxes(state0, 0, 1), F.swapaxes(state1, 0, 1)


class CustomGRU(gluon.HybridBlock):
    def __init__(self, hidden_size, num_layers, dropout, bidirectional, **kwargs):
        super(CustomGRU, self).__init__(**kwargs)
        with self.name_scope():
            self.gru = gluon.rnn.GRU(hidden_size=hidden_size, num_layers=num_layers, dropout=dropout,
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
                self.input_normalization_data_ = ZScoreNormalization(data_mean=data_mean['data_'],
                                                                               data_std=data_std['data_'])
            else:
                self.input_normalization_data_ = NoNormalization()

            self.conv1_padding = Padding(padding=(0,0,0,0,1,1,1,1))
            self.conv1_ = gluon.nn.Conv2D(channels=64,
                kernel_size=(4,4),
                strides=(2,2),
                use_bias=True)
            # conv1_, output shape: {[64,14,14]}

            self.leakyrelu1_ = gluon.nn.LeakyReLU(0.2)
            self.conv2_padding = Padding(padding=(0,0,0,0,1,1,1,1))
            self.conv2_ = gluon.nn.Conv2D(channels=128,
                kernel_size=(4,4),
                strides=(2,2),
                use_bias=True)
            # conv2_, output shape: {[128,7,7]}

            self.batchnorm2_ = gluon.nn.BatchNorm()
            # batchnorm2_, output shape: {[128,7,7]}

            self.leakyrelu2_ = gluon.nn.LeakyReLU(0.2)
            self.conv3_padding = Padding(padding=(0,0,0,0,2,1,2,1))
            self.conv3_ = gluon.nn.Conv2D(channels=256,
                kernel_size=(4,4),
                strides=(2,2),
                use_bias=True)
            # conv3_, output shape: {[256,4,4]}

            self.batchnorm3_ = gluon.nn.BatchNorm()
            # batchnorm3_, output shape: {[256,4,4]}

            self.leakyrelu3_ = gluon.nn.LeakyReLU(0.2)
            self.conv4_padding = Padding(padding=(0,0,0,0,1,1,1,1))
            self.conv4_ = gluon.nn.Conv2D(channels=512,
                kernel_size=(4,4),
                strides=(2,2),
                use_bias=True)
            # conv4_, output shape: {[512,2,2]}

            self.batchnorm4_ = gluon.nn.BatchNorm()
            # batchnorm4_, output shape: {[512,2,2]}

            self.leakyrelu4_ = gluon.nn.LeakyReLU(0.2)
            self.conv5_1_padding = Padding(padding=(0,0,0,0,2,1,2,1))
            self.conv5_1_ = gluon.nn.Conv2D(channels=1,
                kernel_size=(4,4),
                strides=(1,1),
                use_bias=True)
            # conv5_1_, output shape: {[1,2,2]}

            self.sigmoid5_1_ = gluon.nn.Activation(activation='sigmoid')

            pass

    def hybrid_forward(self, F, data_):
        data_ = self.input_normalization_data_(data_)
        conv1_padding = self.conv1_padding(data_)
        conv1_ = self.conv1_(conv1_padding)
        leakyrelu1_ = self.leakyrelu1_(conv1_)
        conv2_padding = self.conv2_padding(leakyrelu1_)
        conv2_ = self.conv2_(conv2_padding)
        batchnorm2_ = self.batchnorm2_(conv2_)
        leakyrelu2_ = self.leakyrelu2_(batchnorm2_)
        conv3_padding = self.conv3_padding(leakyrelu2_)
        conv3_ = self.conv3_(conv3_padding)
        batchnorm3_ = self.batchnorm3_(conv3_)
        leakyrelu3_ = self.leakyrelu3_(batchnorm3_)
        conv4_padding = self.conv4_padding(leakyrelu3_)
        conv4_ = self.conv4_(conv4_padding)
        batchnorm4_ = self.batchnorm4_(conv4_)
        leakyrelu4_ = self.leakyrelu4_(batchnorm4_)
        conv5_1_padding = self.conv5_1_padding(leakyrelu4_)
        conv5_1_ = self.conv5_1_(conv5_1_padding)
        sigmoid5_1_ = self.sigmoid5_1_(conv5_1_)
        dis_ = F.identity(sigmoid5_1_)
        features_ = F.identity(leakyrelu4_)

        return dis_, features_

