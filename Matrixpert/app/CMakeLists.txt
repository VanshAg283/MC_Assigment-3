cmake_minimum_required(VERSION 3.10.2)

project("matrixpert")

add_library(
    matrixnative
    SHARED
    src/main/cpp/matrix_native.cpp
)

target_include_directories(
    matrixnative
    PRIVATE
    ${CMAKE_SOURCE_DIR}/src/main/cpp/eigen
)

find_library(
    log-lib
    log
)

target_link_libraries(
    matrixnative
    ${log-lib}
)
