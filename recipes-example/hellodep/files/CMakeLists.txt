cmake_minimum_required(VERSION 3.9)
project (hellodep)

# tell cmake to search the depedencies in sysroot when cross compiling
message (STATUS "CROOOOOOOOOSSS: ${CMAKE_SYSROOT}")
# tlls the CMake find_*() functions to search for programs, libraries and headers in the target sysroot directory ${CMAKE_SYSROOT}.
set(CMAKE_FIND_ROOT_PATH ${CMAKE_SYSROOT})
# Search libraries only under *target* paths.
#https://stackoverflow.com/questions/41294056/cmake-using-find-package-cross-compiling
set(CMAKE_FIND_ROOT_PATH_MODE_LIBRARY ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_INCLUDE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PACKAGE ONLY)
set(CMAKE_FIND_ROOT_PATH_MODE_PROGRAM NEVER)

# enforce the depedency to libfred. It must be installed before compiling an application
find_library(LIBHELLO
    NAMES hello
    # add here any probable location for libhello 
    HINTS 
        ${CMAKE_SYSROOT}/usr/lib
)

#if(NOT LIBHELLO)
#  message(FATAL_ERROR "You have to compile and install libhello before compiling the hellodep application")
#endif()

# need to point to the libhello include dir
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/
    ${CMAKE_SYSROOT}/usr/include/
)

# point to the libhello lib dir
link_directories(
    ${CMAKE_SYSROOT}/usr/lib/
)

# the source code to tha app
add_executable(${CMAKE_PROJECT_NAME} hellodep.c)

# libraries to be linked
target_link_libraries(${PROJECT_NAME} hello)

INSTALL(TARGETS ${PROJECT_NAME}
        RUNTIME DESTINATION bin
)
