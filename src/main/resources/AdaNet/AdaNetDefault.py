import CoreAdaNet
import mxnet
from mxnet import gluon, io
from adaNetUtils import train_candidate
from typing import Dict, Tuple, List


class CandidateHull(CoreAdaNet.SuperCandidateHull):
    """
        the hull which holds the stacked building blocks and is potentially added to the model
    """

    def __init__(self, stack: int, **kwargs):
        self.names = []
        self.stack = stack
        self.body = None
        super(CandidateHull, self).__init__(**kwargs)

    def build(self) -> None:
        """
            this function builds the candidate function structure and is called by its superclass
            if there are parameters needed for the construction make sure they are added before the superclasses
            constructor is called!
        """
        body = {self.name_ + f"{i}": CoreAdaNet.SuperBuildingBlock(operation=self.building_block) for i in
                range(self.stack)}
        for name, operation in body.items():
            self.__setattr__(name, operation)
            self.names.append(name)
        self.body = body

    def count_nodes(self) -> int:
        if self.node_count is None:
            oc = 0
            for name in self.names:
                oc += self.__getattribute__(name).count_nodes()
            self.node_count = oc
        return self.node_count

    def hybrid_forward(self, F, x, *args, **kwargs):
        if self.input:
            x = self.input(x)

        for name in self.names:
            x = self.__getattribute__(name)(x)

        if self.output:
            x = self.output(x)
        x = self.finalOut(x)

        return x


class Builder(CoreAdaNet.SuperBuilder):
    """
        the object which generates the new candidates
    """

    def __init__(self, **kwargs):
        super(Builder, self).__init__(**kwargs)
        self.round = 0
        self.pre_stack = 1
        self.step = 0
        self.candidate_per_round = 2

    def get_candidates(self) -> Dict[str, Tuple[CandidateHull, List[float]]]:
        """
        :returns a dict of already trained
        """

        candidates = {}
        for i in range(self.candidate_per_round):
            name = f'candidate{i}round{self.round}'
            candidate = CandidateHull(name=name, in_block=self.input, out_block=self.output, stack=i + 1,
                                      model_shape=self.model_shape, building_block=self.build_operation)
            candidate_loss = self.train(candidate)
            candidates.setdefault(name, (candidate, candidate_loss))

        return candidates

    def update(self, up=1) -> None:
        """
        :param up, increases the stack height by up
        :returns None

        updates the round count and increases the stack height by up
        """
        self.pre_stack += up
        self.round += 1
