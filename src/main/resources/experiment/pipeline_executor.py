import sys
sys.path.append("./steps")
sys.path.append("./configuration")
sys.path.append("./backend")
print(sys.path)

import  HDF5DataAccess
import  MySupervisedTrainer
import  SupervisedEvaluation
import  CNNCreator_LeNet
import  Schema2PythonAPI
import  TrainingConfiguraiton

class Pipeline_Executor:
    def __init__(self):
        pass
    def initialise_data_access_step(self,datasource, schema_api):
        return HDF5_Component(datasource, schema_api)
    def initialise_training_step(self,trainData, network, schema_api):
        return MySupervisedTrainer(trainData, network, schema_api)
    def initialise_evaluation_step(self,testData, trainedModel, schema_api):
        return MyEvaluation(testData, trainedModel, schema_api)

    def execute(self):
        exit(0)
        schema_api= Schema2PythonAPI()
        schema_api.set_training_configuration(TrainingConfiguraiton())
        data_access_step = self.initialise_data_access_step( datasource='path.to.source', schema_api = schema_api)
        trainData, testData=data_access_step.execute()

        training_step = self.initialise_training_step( trainData=trainData,network=CNNCreator_LeNet(), schema_api = schema_api)
        trainedModel, accuracy, loss=training_step.execute()

        evaluation_step = self.initialise_evaluation_step( testData=testData, trainedModel=trainedModel, schema_api = schema_api)
        accuracy=evaluation_step.execute()

    print("Execution finished")


Pipeline_Executor.execute()
