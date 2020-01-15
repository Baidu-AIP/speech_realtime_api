cmake_minimum_required(VERSION 3.1)

project(realtime_asr)
set(CMAKE_BUILD_TYPE Debug)
set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_STANDARD 11)

option(WITH_SSL_OPTION "Using wss:\\ instead of ws:\\" OFF)

set(GCC_EXPECTED_VERSION 4.8.2)
if(CMAKE_CXX_COMPILER_VERSION VERSION_LESS GCC_EXPECTED_VERSION)
    message(FATAL_ERROR "GCC: TrinityCore requires version ${GCC_EXPECTED_VERSION} to build but found ${CMAKE_CXX_COMPILER_VERSION}")
endif()

add_definitions(-DELPP_STL_LOGGING -DELPP_BOOST_LOGGING  -DELPP_THREAD_SAFE)
# add_definitions(-DELPP_DISABLE_DEBUG_LOGS) # 不需要DEBUG日志可以打开这行


include_directories(thirdparty/include)
include_directories(thirdparty/src)
include_directories(.)


file(GLOB SOURCE_FILES "mini/*.c*")
file(GLOB COMMON_SOURCE_FILES "common/*.c*")

add_executable(realtime_asr  ${SOURCE_FILES}  ${COMMON_SOURCE_FILES} thirdparty/src/easylogging/easylogging++.cc)

target_link_libraries(realtime_asr pthread)

if(WITH_SSL_OPTION)
    message(STATUS "add SSL support")
    include(CheckIncludeFileCXX)
    CHECK_INCLUDE_FILE_CXX(openssl/engine.h HAVE_SSL_H)
    if(HAVE_SSL_H)
        target_link_libraries(realtime_asr ssl crypto)
        add_definitions(-DWITH_SSL)
    else()
        message(FATAL_ERROR "openssl not found , in Ubuntu: apt-get install libssl-dev")
    endif(HAVE_SSL_H)
endif(WITH_SSL_OPTION)
