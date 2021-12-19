cmake_minimum_required(VERSION 1.9)
project (hellocmake)
add_executable(hellocmake hellocmake.c)
install(TARGETS hellocmake RUNTIME DESTINATION bin)

