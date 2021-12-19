cmake_minimum_required(VERSION 3.9)
project (hello VERSION 1.0 DESCRIPTION "My Hello Library")
#add_library(${CMAKE_PROJECT_NAME} SHARED libhello.c)
add_library(${CMAKE_PROJECT_NAME} STATIC libhello.c)
# replace SHARED by STATIC to change the library type
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES
    VERSION ${PROJECT_VERSION}
    SOVERSION 1
    PUBLIC_HEADER libhello.h)
install(TARGETS ${CMAKE_PROJECT_NAME}
	#LIBRARY DESTINATION lib
	ARCHIVE DESTINATION lib
	PUBLIC_HEADER DESTINATION include
)
#install(FILES hellolib.h DESTINATION include)

# source: https://stackoverflow.com/questions/17511496/how-to-create-a-shared-library-with-cmake
# check it out for more library configuration options

