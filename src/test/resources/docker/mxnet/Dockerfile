FROM maven:3-jdk-8

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        git \
        wget python gcc \
        build-essential cmake \
        liblapack-dev libblas-dev libboost-dev libarmadillo-dev && \
    rm -rf /var/lib/apt/lists/* 
RUN git clone https://github.com/apache/incubator-mxnet.git mxnet-source && \
    cd mxnet-source && git checkout tags/1.4.0 && cd .. && \
    cp -r mxnet-source/include/mxnet /usr/include/mxnet && \
    rm -r mxnet-source
RUN wget https://bootstrap.pypa.io/get-pip.py
RUN python get-pip.py
RUN pip install mxnet h5py
