cmake_minimum_required(VERSION 3.10)
project(unitauto-cpp)

set(CMAKE_CXX_STANDARD 17)
SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fprofile-arcs -ftest-coverage")
SET(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -fprofile-arcs -ftest-coverage")
#SET(CMAKE_EXE_LINKER_FLAGS "${CMAKE_EXE_LINKER_FLAGS} -lgcov")

add_executable(unitauto-cpp main.cpp
        unitauto/nlohmann/json.hpp
        unitauto/method_util.hpp
        unitauto/server.hpp
        unitauto/test/test_util.hpp)
