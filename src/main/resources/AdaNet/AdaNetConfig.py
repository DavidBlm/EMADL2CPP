from enum import Enum


class AdaNetConfig(Enum):
    MAX_NUM_ROUNDS = 100
    GAMMA = .01
    ALPHA = .07
    BETA = .0001
    LAMBDA = 0.0001
