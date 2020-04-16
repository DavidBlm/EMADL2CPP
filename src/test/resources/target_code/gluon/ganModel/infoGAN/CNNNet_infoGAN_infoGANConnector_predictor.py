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
                self.input_normalization_noise_ = ZScoreNormalization(data_mean=data_mean['noise_'],
                                                                               data_std=data_std['noise_'])
            else:
                self.input_normalization_noise_ = NoNormalization()

            if data_mean:
                assert(data_std)
                self.input_normalization_c1_ = ZScoreNormalization(data_mean=data_mean['c1_'],
                                                                               data_std=data_std['c1_'])
            else:
                self.input_normalization_c1_ = NoNormalization()

            self.upconvolution3_padding = (0,0)
            self.upconvolution3_ = gluon.nn.Conv2DTranspose(channels=512,
                kernel_size=(4,4),
                strides=(1,1),
                padding=self.upconvolution3_padding,
                use_bias=False)
            # upconvolution3_, output shape: {[512,4,4]}

            self.batchnorm3_ = gluon.nn.BatchNorm()
            # batchnorm3_, output shape: {[512,4,4]}

            self.relu3_ = gluon.nn.Activation(activation='relu')
            self.upconvolution4_padding = (1,1)
            self.upconvolution4_ = gluon.nn.Conv2DTranspose(channels=256,
                kernel_size=(4,4),
                strides=(2,2),
                padding=self.upconvolution4_padding,
                use_bias=False)
            # upconvolution4_, output shape: {[256,8,8]}

            self.batchnorm4_ = gluon.nn.BatchNorm()
            # batchnorm4_, output shape: {[256,8,8]}

            self.relu4_ = gluon.nn.Activation(activation='relu')
            self.upconvolution5_padding = (1,1)
            self.upconvolution5_ = gluon.nn.Conv2DTranspose(channels=128,
                kernel_size=(4,4),
                strides=(2,2),
                padding=self.upconvolution5_padding,
                use_bias=False)
            # upconvolution5_, output shape: {[128,16,16]}

            self.batchnorm5_ = gluon.nn.BatchNorm()
            # batchnorm5_, output shape: {[128,16,16]}

            self.relu5_ = gluon.nn.Activation(activation='relu')
            self.upconvolution6_padding = (1,1)
            self.upconvolution6_ = gluon.nn.Conv2DTranspose(channels=64,
                kernel_size=(4,4),
                strides=(2,2),
                padding=self.upconvolution6_padding,
                use_bias=False)
            # upconvolution6_, output shape: {[64,32,32]}

            self.batchnorm6_ = gluon.nn.BatchNorm()
            # batchnorm6_, output shape: {[64,32,32]}

            self.relu6_ = gluon.nn.Activation(activation='relu')
            self.upconvolution7_padding = (1,1)
            self.upconvolution7_ = gluon.nn.Conv2DTranspose(channels=1,
                kernel_size=(4,4),
                strides=(2,2),
                padding=self.upconvolution7_padding,
                use_bias=False)
            # upconvolution7_, output shape: {[1,64,64]}

            self.tanh7_ = gluon.nn.Activation(activation='tanh')

            pass

    def hybrid_forward(self, F, noise_, c1_):
        noise_ = self.input_normalization_noise_(noise_)
        c1_ = self.input_normalization_c1_(c1_)
        concatenate3_ = F.concat(noise_, c1_, dim=1)
        reshape3_ = F.reshape(concatenate3_, shape=(0,72,1,1))
        upconvolution3_ = self.upconvolution3_(reshape3_)
        batchnorm3_ = self.batchnorm3_(upconvolution3_)
        relu3_ = self.relu3_(batchnorm3_)
        upconvolution4_ = self.upconvolution4_(relu3_)
        batchnorm4_ = self.batchnorm4_(upconvolution4_)
        relu4_ = self.relu4_(batchnorm4_)
        upconvolution5_ = self.upconvolution5_(relu4_)
        batchnorm5_ = self.batchnorm5_(upconvolution5_)
        relu5_ = self.relu5_(batchnorm5_)
        upconvolution6_ = self.upconvolution6_(relu5_)
        batchnorm6_ = self.batchnorm6_(upconvolution6_)
        relu6_ = self.relu6_(batchnorm6_)
        upconvolution7_ = self.upconvolution7_(relu6_)
        tanh7_ = self.tanh7_(upconvolution7_)
        data_ = F.identity(tanh7_)

        return data_

