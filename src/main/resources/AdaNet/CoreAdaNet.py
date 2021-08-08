"""
    this module provides the abstract classes as well as concrete classes for the AdaNet algorithm components
"""
from abc import ABC, abstractmethod
from typing import Tuple, Dict
import mxnet.gluon
import numpy as np
import mxnet.gluon.nn as nn


class BuildingBlock(ABC, mxnet.gluon.HybridBlock):
    """
        the essential building block which gets stacked
    """

    def __init__(self, operation: mxnet.gluon.HybridBlock, **kwargs):
        super(BuildingBlock, self).__init__(**kwargs)
        self.node_count = None
        self.operation = operation()  # it is expected that the block doesn't take any parameters
        self.oc = None

    def count_nodes(self):
        if self.node_count is None:
            oc = 0
            params = self.collect_params()
            for el in params:
                if 'bias' in el:
                    # each node in a layer has a bias parameter
                    # => number of nodes = number of bias
                    oc += params[el].shape[0]
            self.node_count = oc
        return self.node_count

    def hybrid_forward(self, F, x, *args, **kwargs):
        return self.operation(x)


class CandidateHull(ABC, mxnet.gluon.HybridBlock):
    """
        this is a wrapper to resemble a ensemble candidate
    """

    def __init__(self, building_block: mxnet.gluon.HybridBlock, name: str, model_shape: Tuple[int],
                 rade_aprox: callable = np.sqrt,
                 in_block: mxnet.gluon.HybridBlock = None,
                 out_block: mxnet.gluon.HybridBlock = None, **kwargs):
        super(CandidateHull, self).__init__(**kwargs)
        with self.name_scope():
            self.building_block = building_block
            self.name_ = name
            self.rade_aprox = rade_aprox
            self.node_count = None
            self.complexity = None
            self.rade = None
            self.model_shape = model_shape
            if in_block is not None:
                self.input = in_block()
            else:
                self.input = None
            if out_block is not None:
                self.output = out_block()
            else:
                self.output = None
            self.build()
            self.finalOut = nn.Dense(units=int(np.prod(self.model_shape)), activation=None, flatten=False)

    def get_complexity(self) -> float:
        """
            getter for complexity score

            returns the complexity calculated by the training loss function
            (alpha * r + beta)* ||w||_L1
            r = approximation of the rademacher complexity
        """
        return self.complexity

    def update_complexity(self, complexity) -> None:
        """
            setter for  complexity score
        """
        self.complexity = complexity

    def approximate_rade(self) -> float:
        """
            calculates the rademacher approximation based on the self.rade_aprox
        """
        return self.rade_aprox(self.count_nodes())

    @abstractmethod
    def build(self) -> None:
        """
            this function builds the candidate based on the passed parameter provided by initialization
        """
        pass

    @abstractmethod
    def count_nodes(self) -> int:
        """
            counts the nodes in this candidate
            should call count_nodes() of its building_block properties
        """
        pass


class Builder(ABC):
    def __init__(self, batch_size: int,
                 model_shape: Tuple[int],
                 build_operation: mxnet.gluon.HybridBlock,
                 train_iterator: mxnet.io.NDArrayIter,
                 in_block: mxnet.gluon.HybridBlock = None,
                 out_block: mxnet.gluon.HybridBlock = None,
                 **kwargs):
        self.batch_size = batch_size
        self.model_shape = model_shape
        self.build_operation = build_operation
        self.input = in_block
        self.output = out_block
        self.train_iterator = train_iterator

    @abstractmethod
    def get_candidates(self) -> Dict:
        """
            this fucntion returns a dictionary containing the trained candidates
        """
        pass

    @abstractmethod
    def update(self, up=1):
        """
            :param up function to update the builder for its next round
        """
        pass
