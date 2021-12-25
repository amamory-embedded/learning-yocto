cmake_minimum_required(VERSION 3.9)
project (hellocmake)
add_executable(hellocmake hellocmake.c)
install(TARGETS hellocmake RUNTIME DESTINATION bin)

