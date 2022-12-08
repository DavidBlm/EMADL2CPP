import mxnet as mx
import warnings
import sys
import os

from mxnet import gluon, nd
from mxnet.gluon import nn

ctx = mx.gpu() if mx.context.num_gpus() else mx.cpu()
originalInputName = None
originalModelPath = None
originalParamsPath = None
network = None

newModelDirectory = None
newModelName = None
newInputName = None
parameterLayers = None

index = 0
print(sys.argv)
while index < len(sys.argv):
    arg = sys.argv[index]

    if arg == "-oin":
        originalInputName = sys.argv[index + 1]
    elif arg == "-omp":
        originalModelPath = sys.argv[index + 1]
    elif arg == "-opp":
        originalParamsPath = sys.argv[index + 1]
    elif arg == "-nmd":
        newModelDirectory = sys.argv[index + 1]
    elif arg == "-nmn":
        newModelName = sys.argv[index + 1]
    elif arg == "-nin":
        newInputName = sys.argv[index + 1]
    elif arg == "-pl":
        paramList = sys.argv[index + 1]
        parameterLayers = paramList.split(",")
    else:
        # print("Unknown argument")
        pass
    index += 1

print(originalModelPath)
print(originalParamsPath)
print(originalInputName)

with warnings.catch_warnings():
    warnings.simplefilter("ignore")
    network = gluon.nn.SymbolBlock.imports(originalModelPath, [originalInputName], originalParamsPath, ctx=ctx,
                                           ignore_extra=True)

if network is not None:
    network.hybridize()
    network.save(newModelDirectory + "/" + newModelName)
    network.export(newModelDirectory + "/" + newModelName + "_ex")
