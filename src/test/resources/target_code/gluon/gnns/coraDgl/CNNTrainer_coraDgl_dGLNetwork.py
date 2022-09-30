# (c) https://github.com/MontiCore/monticore
import logging
import mxnet as mx

import CNNCreator_coraDgl_dGLNetwork
import CNNDataLoader_coraDgl_dGLNetwork
import CNNDataCleaner_coraDgl_dGLNetwork
import CNNSupervisedTrainer_coraDgl_dGLNetwork

if __name__ == "__main__":
    logger = logging.getLogger()
    logging.basicConfig(level=logging.DEBUG)
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)

    coraDgl_dGLNetwork_creator = CNNCreator_coraDgl_dGLNetwork.CNNCreator_coraDgl_dGLNetwork()
    coraDgl_dGLNetwork_creator.validate_parameters()
    coraDgl_dGLNetwork_cleaner = CNNDataCleaner_coraDgl_dGLNetwork.CNNDataCleaner_coraDgl_dGLNetwork()
    coraDgl_dGLNetwork_loader = CNNDataLoader_coraDgl_dGLNetwork.CNNDataLoader_coraDgl_dGLNetwork(
        coraDgl_dGLNetwork_cleaner
    )

    coraDgl_dGLNetwork_trainer = CNNSupervisedTrainer_coraDgl_dGLNetwork.CNNSupervisedTrainer_coraDgl_dGLNetwork(
        coraDgl_dGLNetwork_loader,
        coraDgl_dGLNetwork_creator
    )

    coraDgl_dGLNetwork_trainer.train(
        batch_size=1,
        num_epoch=20,
        load_checkpoint=False,
        context='cpu',
        preprocessing=False,
        normalize=False,
        eval_metric='accuracy_masked',
        eval_metric_params={
        },
        eval_train=False,
        loss='softmax_cross_entropy',
            loss_params={
                'sparse_label': True},
        optimizer='adam',
        optimizer_params={
            'weight_decay': 0.0,
            'learning_rate': 0.05,
            'learning_rate_decay': 1.0,
            'step_size': 1        },
    )    

