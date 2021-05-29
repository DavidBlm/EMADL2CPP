sudo apt-get update -y
# (c) https://github.com/MontiCore/monticore  
sudp apt-get install -y build-essential git openjdk-8-jdk maven ninja-build ccache libopenblas-dev libblas-dev /
						liblapack-dev libopencv-dev libarmadillo-dev cmake python2.7 python-dev /
						python-numpy python-tk python3-pip python3-pip swig unzip libboost-all-dev

sudo update-alternatives --config java

pip3 install --user --upgrade "cmake>=3.13.2"

wget https://bootstrap.pypa.io/get-pip.py
python get-pip.py
pip install --user h5py matplotlib numpy==1.16.5 mxnet==1.5.1.post0 #The newest version installed the curent standard version installed v1.7.0 cant be run with python2 the current standard of the EMDAL2CPP generator,
																	#As the needed numpy version is not suported anymore for python2 (python will no longer be supported).
																	#Further more not all test work with mxnet v1.6.0. And when just using v1.5.1 you can't compile against the libmxnet.so needed for compiling
																	#the cpp prediction part, the same holds for 1.6.0 but not 1.5.1.post0 and versions later than 1.6.0 (1.7.0) as there was some compression done for this
																	#library which was then droped again by the developers of mxnet.
                                                                    #You could alternativly also use python 3.6 instead of 2.7, then you could also use the newest numpy version. 
															  		#Note that you then have to also set the PYTHON_PATH acordingly, or specifiy the python path for all applications in their build scripts
															  		#and test currently only run on the python specified in the PYTHON_PATH.
                                                              		#If you want to use mxnet with cuda install f.e. mxnet-cu100 for cuda 10.0  (if v1.5.1 is the newest version (here it specifing post0 is not
																	#neccescery), otherwise no gurantee for the numpy dependency, see above), #of course then you have to install cuda and cudnn beforehand.

git clone --recursive https://github.com/apache/incubator-mxnet.git mxnet
cd mxnet && git checkout tags/1.5.0 && git submodule update --recursive --init
mkdir build && cd build && cmake -DUSE_CPP_PACKAGE=1 -DUSE_CUDA=0 -GNinja .. && ninja -v
sudo cp -r include/mxnet /usr/include/mxnet && sudo cp -r cpp-package/include/mxnet-cpp /usr/include/ && sudo cp -r 3rdparty/tvm/nnvm/include/nnvm /usr/include/ && sudo cp -r 3rdparty/dmlc-core/include/dmlc /usr/include/

#you have to have armadillo-9.600.6.zip in your current folder
unzip armadillo-9.600.6.zip -d .
cd armadillo-9.600.6 && cmake . && make && sudo make install

sudo mkdir -p /root/.config/matplotlib
echo "backend : Agg" > sudo /root/.config/matplotlib/matplotlibrc
