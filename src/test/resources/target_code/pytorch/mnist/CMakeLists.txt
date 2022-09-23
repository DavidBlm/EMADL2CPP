cmake_minimum_required(VERSION 3.5)
set(CMAKE_CXX_STANDARD 14)

project(mnist_mnistClassifier LANGUAGES CXX)

#set cmake module path
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR}/cmake)

# add dependencies
find_package(Armadillo REQUIRED)
set(INCLUDE_DIRS ${INCLUDE_DIRS} ${Armadillo_INCLUDE_DIRS})
set(LIBS ${LIBS} ${Armadillo_LIBRARIES})


# additional library linkage
set(LIBS ${LIBS} TensorflowCC::Shared)

# additional commands
find_package(Torch REQUIRED)

# create static library
include_directories(${INCLUDE_DIRS})
add_library(mnist_mnistClassifier mnist_mnistClassifier.cpp)
target_include_directories(mnist_mnistClassifier PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${INCLUDE_DIRS})
target_link_libraries(mnist_mnistClassifier PUBLIC ${LIBS})
set_target_properties(mnist_mnistClassifier PROPERTIES LINKER_LANGUAGE CXX)

# export cmake project
export(TARGETS mnist_mnistClassifier FILE mnist_mnistClassifier.cmake)

# additional commands end
