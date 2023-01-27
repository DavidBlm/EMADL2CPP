import mxnet as mx
import warnings
import sys

from mxnet import gluon

ctx = mx.gpu() if mx.context.num_gpus() else mx.cpu()
inputName = None
modelPath = None
paramsPath = None
network = None

newModelDirectory = None
newModelName = None
parameterLayers = None

index = 0
while index < len(sys.argv):
    arg = sys.argv[index]

    if arg == "-in":
        inputName = sys.argv[index + 1]
    elif arg == "-mp":
        modelPath = sys.argv[index + 1]
    elif arg == "-pp":
        paramsPath = sys.argv[index + 1]
    elif arg == "-nmd":
        newModelDirectory = sys.argv[index + 1]
    elif arg == "-nmn":
        newModelName = sys.argv[index + 1]
    elif arg == "-pl":
        paramList = sys.argv[index + 1]
        parameterLayers = paramList.split(",")
    index += 1

with warnings.catch_warnings():
    warnings.simplefilter("ignore")
    network = gluon.nn.SymbolBlock.imports(modelPath, [inputName], paramsPath, ctx=ctx,
                                           ignore_extra=True, allow_missing=True)

if network is not None:
    print("Parameters for " + newModelName + ":")
    print(network.collect_params())
    param_file_save_dir = newModelDirectory + "/" + newModelName + "-0000.params"
    network.save_parameters(param_file_save_dir)
    print("New parameter file saved to: " + param_file_save_dir)
