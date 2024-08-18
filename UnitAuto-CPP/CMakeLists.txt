cmake_minimum_required(VERSION 3.28)
project(unitauto-cpp)

set(CMAKE_CXX_STANDARD 17)

add_executable(unitauto-cpp main.cpp
        unitauto/nlohmann/json.hpp
        unitauto/method_util.hpp
        unitauto/server.hpp
        unitauto/test/test_util.hpp)
