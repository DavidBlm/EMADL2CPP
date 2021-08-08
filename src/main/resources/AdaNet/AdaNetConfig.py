from enum import Enum


class AdaNetConfig(Enum):
    MAX_NUM_ROUNDS = 3
    GAMMA = .01
    ALPHA = .07
    BETA = .001
    LAMBDA = 0.01
