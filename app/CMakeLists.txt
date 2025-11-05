cmake_minimum_required(VERSION 3.22.1)

project("streettycoon")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Add our native library
add_library(streettycoon SHARED
        src/main/cpp/game_simulation.cpp
        src/main/cpp/game_state.cpp
        src/main/cpp/stall.cpp
        src/main/cpp/helper.cpp
        src/main/cpp/json_serializer.cpp
        src/main/cpp/jni_bridge.cpp
)

# Include directories
target_include_directories(streettycoon PRIVATE
        src/main/cpp
)

# Link libraries
find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(streettycoon
        ${log-lib}
        ${android-lib}
)
