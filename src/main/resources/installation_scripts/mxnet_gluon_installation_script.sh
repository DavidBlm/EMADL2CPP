sudo apt-get update -y
sudp apt-get install -y build-essential git openjdk-8-jdk maven ninja-build ccache libopenblas-dev libblas-dev /
						liblapack-dev libopencv-dev libarmadillo-dev cmake python2.7 python-dev /
						python-numpy python3-pip python3-pip swig unzip libboost-all-dev

sudo update-alternatives --config java

pip3 install --user --upgrade "cmake>=3.13.2"

wget https://bootstrap.pypa.io/get-pip.py
python get-pip.py
pip install --user h5py matplotlib numpy==1.16.5 mxnet==1.5.1 #Not alll test work with mxnet v1.6.0, the curent standard version installed for the cpu installation of mxnet.
                                                              #You could alternativly also use python 3.6 instead of 2.7, then you could also use the newest numpy version. 
															  #Note that you then have to also set the PYTHON_PATH acordingly, or specifiy the python path for all applications in their build scripts
															  #and test currently only run on the python specified in the PYTHON_PATH.
                                                              #If you want to use mxnet with cuda install f.e. mxnet-cu100 for cuda 10.0  (for this currently v1.5.1 is already the newest version),
                                                              #of course then you have to install cuda and cudnn beforehand.

git clone --recursive https://github.com/apache/incubator-mxnet.git mxnet
cd mxnet && git checkout tags/1.5.0 && git submodule update --recursive --init
cd mxnet && mkdir build && cd build && cmake -DUSE_CPP_PACKAGE=1 -DUSE_CUDA=0 -GNinja .. && ninja -v
cd mxnet && cp -r include/mxnet /usr/include/mxnet && cp -r cpp-package/include/mxnet-cpp /usr/include/ && cp -r 3rdparty/tvm/nnvm/include/nnvm /usr/include/ && cp -r 3rdparty/dmlc-core/include/dmlc /usr/include/

#you have tohave armadillo-9.600.6.zip in your current folder
unzip armadillo.zip -d .
cd armadillo-9.600.6 && cmake . && make && make install

mkdir -p /root/.config/matplotlib
echo "backend : Agg" > /root/.config/matplotlib/matplotlibrc