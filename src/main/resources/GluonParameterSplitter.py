import mxnet as mx
import warnings
import sys

from mxnet import gluon

ctx = mx.gpu() if mx.context.num_gpus() else mx.cpu()
inputName = None
modelPath = None
paramsPath = None
network = None
oldNetwork = None

oldNetworkPath = None

newModelDirectory = None
newModelName = None
parameterLayers = None
reExport = False

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
    elif arg == "-onp":
        oldNetworkPath = sys.argv[index + 1]
    elif arg == "-re":
        reExport = True

    index += 1

if reExport:
    #warnings.simplefilter("ignore")
    print("Old network: ", oldNetworkPath)
    print("Old params: ", paramsPath)
    print("Input name: ", inputName)
    oldNetwork = gluon.nn.SymbolBlock.imports(oldNetworkPath, [inputName], paramsPath, ctx=ctx)
    #oldNetwork.load_parameters(paramsPath)
    oldNetwork.initialize()
    print("re-exporting parameters of old network to:", paramsPath)
    oldNetwork.collect_params()
    oldNetwork.export(oldNetworkPath)
    oldNetwork.save_parameters(paramsPath, deduplicate=True)

"""
with warnings.catch_warnings():
    warnings.simplefilter("ignore")
    oldNetwork = gluon.nn.SymbolBlock.imports(oldNetworkPath, [inputName], paramsPath, ctx=ctx, allow_missing=True)
"""

with warnings.catch_warnings():
    warnings.simplefilter("ignore")
    network = gluon.nn.SymbolBlock.imports(modelPath, [inputName], paramsPath, ctx=ctx,
                                           ignore_extra=True, allow_missing=False)




if network is not None :
    file_save_dir = newModelDirectory + "/" + newModelName
    network.export(file_save_dir)

    param_file_save_dir = newModelDirectory + "/" + newModelName + "-0000.params"
    network.save_parameters(param_file_save_dir)
