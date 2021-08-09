from enum import Enum


class AdaNetConfig(Enum):
    MAX_NUM_ROUNDS = 2
    GAMMA = .01
    ALPHA = .07
    BETA = .001
    LAMBDA = 0.01
    DEFAULT_BLOCK = 'FullyConnected'
    DEFAULT_BLOCK_ARGS = {'units': 10,
                          'activation': 'relu',
                          'flatten': True}
