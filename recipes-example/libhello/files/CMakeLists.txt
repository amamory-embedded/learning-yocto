cmake_minimum_required(VERSION 3.9)
# the project version is required
project (hello VERSION 1.0 DESCRIPTION "My Hello Library")

# chooseee the library format: static or dynamic by
# replacing SHARED by STATIC to change the library type
add_library(${CMAKE_PROJECT_NAME} SHARED libhello.c)
#add_library(${CMAKE_PROJECT_NAME} STATIC libhello.c)

# VERSION and SOVERSION are required
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES
    VERSION ${PROJECT_VERSION}
    SOVERSION 1
    PUBLIC_HEADER hello.h)

# replace LIBRARY by ARCHIVE depending on the library format
install(TARGETS ${CMAKE_PROJECT_NAME}
	DESTINATION /usr
	LIBRARY DESTINATION /usr/lib
	#ARCHIVE DESTINATION lib
	PUBLIC_HEADER DESTINATION /usr/include
)

# any addition file that needs to be installed
#install(FILES <filename> DESTINATION <dir>)

# source: https://stackoverflow.com/questions/17511496/how-to-create-a-shared-library-with-cmake
# check it out for more library configuration options
