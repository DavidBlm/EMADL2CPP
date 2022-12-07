import mxnet as mx
import warnings
import sys

from mxnet import gluon, nd
from mxnet.gluon import nn


ctx = mx.gpu() if mx.context.num_gpus() else mx.cpu()
originalInputName = None
originalModelPath = None
orignalParamsPath = None
network = None

newModelDirectory = None
newModelName = None
newInputName = None
parameterLayers = None


"""
for index,arg in sys.argv:
   if arg == "-oin":
    originalInputName = sys.argv[index+1]
   elif arg == "-omp":
    originalModelPath = sys.argv[index+1]
   elif arg == "-opp":
    orignalParamsPath = sys.argv[index+1]
   elif arg == "-nmd":
    newModelDirectory = sys.argv[index+1]
   elif arg == "-nmn":
    newModelName = sys.argv[index+1]
   elif arg == "-nin":
    newInputName = sys.argv[index+1]
   elif arg == "-pl":
    paramList = sys.argv[index+1]
    parameterLayers = paramList.split(",")
   else:
    print("Unknown argument")
"""

originalInputName = "data"
originalModelPath = "model_0_newest-symbol.json"
#originalModelPath = "model_Net1_decomposed.json"
#originalModelPath = "model_Net3_decomposed.json"
orignalParamsPath = "model_0_newest-0000.params"
network = None

parameterLayers = ["conv0_weight","conv0_bias","conv1_weight","conv1_bias"]


with warnings.catch_warnings():
    warnings.simplefilter("ignore")
    network = gluon.nn.SymbolBlock.imports(originalModelPath, [originalInputName], orignalParamsPath, ctx=ctx, ignore_extra=True)

if network is not None:
    print(network)
    params_dict = network.collect_params()
    print(params_dict)
    #print(params_dict.keys())

    #newParamDict = ParameterDict()

    for key in params_dict.keys():
        param = params_dict[key]
        print(param.list_data())
        #params_dict[key].zero_grad()
        #param.set_data(0)
        #print(params_dict[key].list_data())

        pass
        #if key not in parameterLayers:
        #    param = params_dict[key]

            #pass

    #print(params_dict.items())
    #print(params_dict)

    #originalNetwork.save_parameters(newModelDirectory + "/" + newModelName + ".params")
    network.save_parameters("test.params")

    newNetwork = gluon.nn.SymbolBlock.imports(originalModelPath, [originalInputName], "test.params", ctx=ctx)
    #originalNetwork.
