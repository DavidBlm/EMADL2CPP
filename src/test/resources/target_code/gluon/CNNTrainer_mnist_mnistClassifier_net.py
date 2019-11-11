import logging
import mxnet as mx
import CNNCreator_mnist_mnistClassifier_net
import CNNDataLoader_mnist_mnistClassifier_net
import CNNSupervisedTrainer_mnist_mnistClassifier_net

if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    logger = logging.getLogger()
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)

    mnist_mnistClassifier_net_creator = CNNCreator_mnist_mnistClassifier_net.CNNCreator_mnist_mnistClassifier_net()
    mnist_mnistClassifier_net_loader = CNNDataLoader_mnist_mnistClassifier_net.CNNDataLoader_mnist_mnistClassifier_net()
    mnist_mnistClassifier_net_trainer = CNNSupervisedTrainer_mnist_mnistClassifier_net.CNNSupervisedTrainer_mnist_mnistClassifier_net(
        mnist_mnistClassifier_net_loader,
        mnist_mnistClassifier_net_creator
    )

    mnist_mnistClassifier_net_trainer.train(
        batch_size=64,
        num_epoch=11,
        context='gpu',
        eval_metric='accuracy',
        eval_metric_params={
        },
        optimizer='adam',
        optimizer_params={
            'epsilon': 1.0E-8,
            'weight_decay': 0.001,
            'beta1': 0.9,
            'beta2': 0.999,
            'learning_rate_policy': 'fixed',
            'learning_rate': 0.001}
    )
