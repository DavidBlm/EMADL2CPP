from caffe2.python import workspace, core, model_helper, brew, optimizer
from caffe2.python.predictor import mobile_exporter
from caffe2.proto import caffe2_pb2
import numpy as np
import math
import datetime
import logging
import os
import sys
import lmdb

class CNNCreator_mnist_mnistClassifier_net:

    module = None
    _current_dir_ = os.path.join('./')
    _data_dir_    = os.path.join(_current_dir_, 'data/mnist.LeNetNetwork')
    _model_dir_   = os.path.join(_current_dir_, 'model', 'mnist.LeNetNetwork')

    _init_net_    = os.path.join(_model_dir_, 'init_net.pb')
    _predict_net_ = os.path.join(_model_dir_, 'predict_net.pb')

    def get_total_num_iter(self, num_epoch, batch_size, dataset_size):
        #Force floating point calculation
        batch_size_float = float(batch_size)
        dataset_size_float = float(dataset_size)

        iterations_float = math.ceil(num_epoch*(dataset_size_float/batch_size_float))
        iterations_int = int(iterations_float)

        return iterations_int

    def get_epoch_as_iter(self, num_epoch, batch_size, dataset_size):   #To print metric durint training process
        #Force floating point calculation
        batch_size_float = float(batch_size)
        dataset_size_float = float(dataset_size)

        epoch_float = math.ceil(dataset_size_float/batch_size_float)
        epoch_int = int(epoch_float)

        return epoch_int

    def add_input(self, model, batch_size, db, db_type, device_opts):
        with core.DeviceScope(device_opts):
            if not os.path.isdir(db):
                logging.error("Data loading failure. Directory '" + os.path.abspath(db) + "' does not exist.")
                sys.exit(1)
            elif not (os.path.isfile(os.path.join(db, 'data.mdb')) and os.path.isfile(os.path.join(db, 'lock.mdb'))):
                logging.error("Data loading failure. Directory '" + os.path.abspath(db) + "' does not contain lmdb files.")
                sys.exit(1)

            # load the data
            data_uint8, label = brew.db_input(
                model,
                blobs_out=["data_uint8", "label"],
                batch_size=batch_size,
                db=db,
                db_type=db_type,
            )
            # cast the data to float
            data = model.Cast(data_uint8, "data", to=core.DataType.FLOAT)

            # scale data from [0,255] down to [0,1]
            data = model.Scale(data, data, scale=float(1./256))

            # don't need the gradient for the backward pass
            data = model.StopGradient(data, data)

            dataset_size = int (lmdb.open(db).stat()['entries'])

            return data, label, dataset_size

    def create_model(self, model, data, device_opts, is_test):
    	with core.DeviceScope(device_opts):

    		image_ = data
    		# image_, output shape: {[1,28,28]}
    		conv1_ = brew.conv(model, image_, 'conv1_', dim_in=1, dim_out=20, kernel=5, stride=1, pad=1)
    		# conv1_, output shape: {[20,28,28]}
    		pool1_ = brew.max_pool(model, conv1_, 'pool1_', kernel=2, stride=2, pad=1)
    		# pool1_, output shape: {[20,14,14]}
    		conv2_ = brew.conv(model, pool1_, 'conv2_', dim_in=20, dim_out=50, kernel=5, stride=1, pad=1)
    		# conv2_, output shape: {[50,14,14]}
    		pool2_ = brew.max_pool(model, conv2_, 'pool2_', kernel=2, stride=2, pad=1)
    		# pool2_, output shape: {[50,7,7]}
    		fc2_ = brew.fc(model, pool2_, 'fc2_', dim_in=50 * 7 * 7, dim_out=500)
    		# fc2_, output shape: {[500,1,1]}
    		relu2_ = brew.relu(model, fc2_, fc2_)
    		fc3_ = brew.fc(model, relu2_, 'fc3_', dim_in=500, dim_out=10)
    		# fc3_, output shape: {[10,1,1]}
    		predictions_ = brew.softmax(model, fc3_, 'predictions_')

    		return predictions_

    # this adds the loss and optimizer
    def add_training_operators(self, model, output, label, device_opts, loss, opt_type, base_learning_rate, policy, stepsize, epsilon, beta1, beta2, gamma, momentum) :
    	with core.DeviceScope(device_opts):
    		if loss == 'cross_entropy':
    		    xent = model.LabelCrossEntropy([output, label], 'xent')
    		    loss = model.AveragedLoss(xent, "loss")
    		elif loss == 'euclidean':
    		    dist = model.net.SquaredL2Distance([label, output], 'dist')
    		    loss = dist.AveragedLoss([], ['loss'])

    		model.AddGradientOperators([loss])

    		if opt_type == 'adam':
    		    if policy == 'step':
    		        opt = optimizer.build_adam(model, base_learning_rate=base_learning_rate, policy=policy, stepsize=stepsize, beta1=beta1, beta2=beta2, epsilon=epsilon)
    		    elif policy == 'fixed' or policy == 'inv':
    		        opt = optimizer.build_adam(model, base_learning_rate=base_learning_rate, policy=policy, beta1=beta1, beta2=beta2, epsilon=epsilon)
    		    print("adam optimizer selected")
    		elif opt_type == 'sgd':
    		    if policy == 'step':
    		        opt = optimizer.build_sgd(model, base_learning_rate=base_learning_rate, policy=policy, stepsize=stepsize, gamma=gamma, momentum=momentum)
    		    elif policy == 'fixed' or policy == 'inv':
    		        opt = optimizer.build_sgd(model, base_learning_rate=base_learning_rate, policy=policy, gamma=gamma, momentum=momentum)
    		    print("sgd optimizer selected")
    		elif opt_type == 'rmsprop':
    		    if policy == 'step':
    		        opt = optimizer.build_rms_prop(model, base_learning_rate=base_learning_rate, policy=policy, stepsize=stepsize, decay=gamma, momentum=momentum, epsilon=epsilon)
    		    elif policy == 'fixed' or policy == 'inv':
    		        opt = optimizer.build_rms_prop(model, base_learning_rate=base_learning_rate, policy=policy, decay=gamma, momentum=momentum, epsilon=epsilon)
    		    print("rmsprop optimizer selected")
    		elif opt_type == 'adagrad':
    		    if policy == 'step':
    		        opt = optimizer.build_adagrad(model, base_learning_rate=base_learning_rate, policy=policy, stepsize=stepsize, decay=gamma, epsilon=epsilon)
    		    elif policy == 'fixed' or policy == 'inv':
    		        opt = optimizer.build_adagrad(model, base_learning_rate=base_learning_rate, policy=policy, decay=gamma, epsilon=epsilon)
    		    print("adagrad optimizer selected")

    def add_accuracy(self, model, output, label, device_opts, eval_metric):
        with core.DeviceScope(device_opts):
            if eval_metric == 'accuracy':
                accuracy = brew.accuracy(model, [output, label], "accuracy")
            elif eval_metric == 'top_k_accuracy':
                accuracy = brew.accuracy(model, [output, label], "accuracy", top_k=3)
            return accuracy

    def train(self, num_epoch=1000, batch_size=64, context='gpu', eval_metric='accuracy', loss='cross_entropy', opt_type='adam', base_learning_rate=0.001, weight_decay=0.001, policy='fixed', stepsize=1, epsilon=1E-8, beta1=0.9, beta2=0.999, gamma=0.999, momentum=0.9) :
        if context == 'cpu':
            device_opts = core.DeviceOption(caffe2_pb2.CPU, 0)
            print("CPU mode selected")
        elif context == 'gpu':
            device_opts = core.DeviceOption(caffe2_pb2.CUDA, 0)
            print("GPU mode selected")

    	workspace.ResetWorkspace(self._model_dir_)

    	arg_scope = {"order": "NCHW"}
    	# == Training model ==
    	train_model= model_helper.ModelHelper(name="train_net", arg_scope=arg_scope)
    	data, label, train_dataset_size = self.add_input(train_model, batch_size=batch_size, db=os.path.join(self._data_dir_, 'train_lmdb'), db_type='lmdb', device_opts=device_opts)
    	predictions_ = self.create_model(train_model, data, device_opts=device_opts, is_test=False)
    	self.add_training_operators(train_model, predictions_, label, device_opts, loss, opt_type, base_learning_rate, policy, stepsize, epsilon, beta1, beta2, gamma, momentum)
    	if not loss == 'euclidean':
    		self.add_accuracy(train_model, predictions_, label, device_opts, eval_metric)
    	with core.DeviceScope(device_opts):
    		brew.add_weight_decay(train_model, weight_decay)

    	# Initialize and create the training network
    	workspace.RunNetOnce(train_model.param_init_net)
    	workspace.CreateNet(train_model.net, overwrite=True)

    	# Main Training Loop
    	iterations = self.get_total_num_iter(num_epoch, batch_size, train_dataset_size)
        epoch_as_iter = self.get_epoch_as_iter(num_epoch, batch_size, train_dataset_size)
        print("\n*** Starting Training for " + str(num_epoch) + " epochs = " + str(iterations) + " iterations ***")
        start_date = datetime.datetime.now()
    	for i in range(iterations):
    		workspace.RunNet(train_model.net)
    		if i % 50 == 0 or i % epoch_as_iter == 0:
    			if not loss == 'euclidean':
    				print 'Iter ' + str(i) + ': ' + 'Loss ' + str(workspace.FetchBlob("loss")) + ' - ' + 'Accuracy ' + str(workspace.FetchBlob('accuracy'))
    			else:
    				print 'Iter ' + str(i) + ': ' + 'Loss ' + str(workspace.FetchBlob("loss"))

    			current_time = datetime.datetime.now()
    			elapsed_time = current_time - start_date
    			print 'Progress: ' + str(i) + '/' + str(iterations) + ', ' +'Current time spent: ' + str(elapsed_time)
        current_time = datetime.datetime.now()
        elapsed_time = current_time - start_date
        print 'Progress: ' + str(iterations) + '/' + str(iterations) + ' Training done' + ', ' + 'Total time spent: ' + str(elapsed_time)

    	print("\n*** Running Test model ***")
    	# == Testing model. ==
    	test_model= model_helper.ModelHelper(name="test_net", arg_scope=arg_scope, init_params=False)
    	data, label, test_dataset_size = self.add_input(test_model, batch_size=batch_size, db=os.path.join(self._data_dir_, 'test_lmdb'), db_type='lmdb', device_opts=device_opts)
    	predictions_ = self.create_model(test_model, data, device_opts=device_opts, is_test=True)
    	if not loss == 'euclidean':
    		self.add_accuracy(test_model, predictions_, label, device_opts, eval_metric)
    	workspace.RunNetOnce(test_model.param_init_net)
    	workspace.CreateNet(test_model.net, overwrite=True)

    	# Main Testing Loop
    	test_accuracy = np.zeros(test_dataset_size/batch_size)
        start_date = datetime.datetime.now()
    	for i in range(test_dataset_size/batch_size):
    		# Run a forward pass of the net on the current batch
    		workspace.RunNet(test_model.net)
    		# Collect the batch accuracy from the workspace
    		if not loss == 'euclidean':
    			test_accuracy[i] = workspace.FetchBlob('accuracy')
    			print 'Iter ' + str(i) + ': ' + 'Accuracy ' + str(workspace.FetchBlob("accuracy"))
    		else:
    			test_accuracy[i] = workspace.FetchBlob("loss")
    			print 'Iter ' + str(i) + ': ' + 'Loss ' + str(workspace.FetchBlob("loss"))

    		current_time = datetime.datetime.now()
    		elapsed_time = current_time - start_date
    		print 'Progress: ' + str(i) + '/' + str(test_dataset_size/batch_size) + ', ' +'Current time spent: ' + str(elapsed_time)
        current_time = datetime.datetime.now()
        elapsed_time = current_time - start_date
        print 'Progress: ' + str(test_dataset_size/batch_size) + '/' + str(test_dataset_size/batch_size) + ' Testing done' + ', ' + 'Total time spent: ' + str(elapsed_time)
    	print('Test accuracy mean: {:.9f}'.format(test_accuracy.mean()))

    	# == Deployment model. ==
    	# We simply need the main AddModel part.
    	deploy_model = model_helper.ModelHelper(name="deploy_net", arg_scope=arg_scope, init_params=False)
    	self.create_model(deploy_model, "data", device_opts, is_test=True)

    	print("\n*** Saving deploy model ***")
    	self.save_net(self._init_net_, self._predict_net_, deploy_model)

    def save_net(self, init_net_path, predict_net_path, model):

    	init_net, predict_net = mobile_exporter.Export(
    		workspace,
    		model.net,
    		model.params
    	)

        try:
            os.makedirs(self._model_dir_)
        except OSError:
            if not os.path.isdir(self._model_dir_):
                raise

    	print("Save the model to init_net.pb and predict_net.pb")
    	with open(predict_net_path, 'wb') as f:
    		f.write(model.net._net.SerializeToString())
    	with open(init_net_path, 'wb') as f:
    		f.write(init_net.SerializeToString())

    	print("Save the model to init_net.pbtxt and predict_net.pbtxt as additional information")

    	with open(init_net_path.replace('.pb','.pbtxt'), 'w') as f:
    		f.write(str(init_net))
    	with open(predict_net_path.replace('.pb','.pbtxt'), 'w') as f:
    		f.write(str(predict_net))
    	print("== Saved init_net and predict_net ==")

    def load_net(self, init_net_path, predict_net_path, device_opts):
        if not os.path.isfile(init_net_path):
            logging.error("Network loading failure. File '" + os.path.abspath(init_net_path) + "' does not exist.")
            sys.exit(1)
        elif not os.path.isfile(predict_net_path):
            logging.error("Network loading failure. File '" + os.path.abspath(predict_net_path) + "' does not exist.")
            sys.exit(1)

        init_def = caffe2_pb2.NetDef()
    	with open(init_net_path, 'rb') as f:
    		init_def.ParseFromString(f.read())
    		init_def.device_option.CopyFrom(device_opts)
    		workspace.RunNetOnce(init_def.SerializeToString())

    	net_def = caffe2_pb2.NetDef()
    	with open(predict_net_path, 'rb') as f:
    		net_def.ParseFromString(f.read())
    		net_def.device_option.CopyFrom(device_opts)
    		workspace.CreateNet(net_def.SerializeToString(), overwrite=True)
    	print("*** Loaded init_net and predict_net ***")
