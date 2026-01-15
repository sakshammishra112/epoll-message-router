cmake_minimum_required(VERSION 3.16)

project(epoll_chat_server
        VERSION 1.0
        LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

include_directories(
    ${PROJECT_SOURCE_DIR}/include
)

set(SOURCES
    src/main.cpp
    src/server.cpp
    src/protocol.cpp
    src/net_utils.cpp
)

add_executable(server ${SOURCES})

target_compile_options(server PRIVATE
    -Wall
    -Wextra
    -Wpedantic
)

target_link_libraries(server
    PRIVATE
)
