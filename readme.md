
# Learning Yocto

This tutorial goes through the process, step-by-step, of adding your own software into a Linux distribution generate by Yocto.
It's initial goal was to document my own learning process, but, perhaps it can also be usefull for other begginers in Yocto.
This tutorial is not meant to be a complete Yocto reference. The idea is to be that initial little push, so you canstart figuring out the Yocto workflow. 

## Instalation

We use a docker container with Yocto and VNC installed. Check-out the cointerner manual to see it's features and how to install it.

## Building the image for raspberry pi3

$ bitbake core-image-minimal -c populate_sdk


## Creating a Custom Layer

Everywhere you read about Yocto recommends that you create your own layer to dploy your software in the Linux image. So this section goes through this process of creating the layer where your recipes will be added.

 source: https://www.yoctoproject.org/docs/current/mega-manual/mega-manual.html#creating-your-own-general-layer

```
$ bitbake-layers show-layers
$ bitbake-layers create-layer meta-fred
$ bitbake-layers add-layer meta-fred
$ bitbake-layers show-layers
NOTE: Starting bitbake server...
layer                 path                                      priority
==========================================================================
meta                  /opt/yocto/dunfell/src/poky/meta          5
meta-poky             /opt/yocto/dunfell/src/poky/meta-poky     5
meta-yocto-bsp        /opt/yocto/dunfell/src/poky/meta-yocto-bsp  5
meta-fred             /home/build/rpi/build/meta-fred           6
meta-raspberrypi      /opt/yocto/dunfell/src/meta-raspberrypi   9
meta-oe               /opt/yocto/dunfell/src/meta-openembedded/meta-oe  6
meta-networking       /opt/yocto/dunfell/src/meta-openembedded/meta-networking  5
meta-python           /opt/yocto/dunfell/src/meta-openembedded/meta-python
$ tree meta-fred/
meta-fred/
├── conf
│   └── layer.conf
├── COPYING.MIT
├── README
└── recipes-example
    └── example
        └── example_0.1.bb
```

Next we start configuring the recipes based on the steps described in [here](https://www.yoctoproject.org/docs/current/mega-manual/mega-manual.html#new-recipe-writing-a-new-recipe).

```
$ cat meta-fred/recipes-example/example/example_0.1.bb
SUMMARY = "bitbake-layers recipe"
DESCRIPTION = "Recipe created by bitbake-layers"
LICENSE = "MIT"

python do_display_banner() {
    bb.plain("***********************************************");
    bb.plain("*                                             *");
    bb.plain("*  Example recipe created by bitbake-layers   *");
    bb.plain("*                                             *");
    bb.plain("***********************************************");
}

addtask display_banner before do_build
```

This example recipe does not properly configure any software. It only prints some messages.

```
cd build
$ bitbake example
Parsing recipes: 100% |#################################################################################################################| Time: 0:00:20
Parsing of 2120 .bb files complete (0 cached, 2120 parsed). 3212 targets, 134 skipped, 0 masked, 0 errors.
NOTE: Resolving any missing task queue dependencies

Build Configuration:
BB_VERSION           = "1.46.0"
BUILD_SYS            = "x86_64-linux"
NATIVELSBSTRING      = "universal"
TARGET_SYS           = "arm-poky-linux-gnueabi"
MACHINE              = "raspberrypi3"
DISTRO               = "poky"
DISTRO_VERSION       = "3.1.12"
TUNE_FEATURES        = "arm vfp cortexa7 neon vfpv4 thumb callconvention-hard"
TARGET_FPU           = "hard"
meta                 
meta-poky            
meta-yocto-bsp       = "dunfell:cf5a00721f721d5077c73d1f4e812e5c79833fba"
meta-fred            = "<unknown>:<unknown>"
meta-raspberrypi     = "dunfell:934064a01903b2ba9a82be93b3f0efdb4543a0e8"
meta-oe              
meta-networking      
meta-python          = "dunfell:69f94af4d91215e7d4e225bab54bf3bcfee42f1c"

Initialising tasks: 100% |##############################################################################################################| Time: 0:00:00
Sstate summary: Wanted 7 Found 0 Missed 7 Current 134 (0% match, 95% complete)
NOTE: Executing Tasks
***********************************************
*                                             *
*  Example recipe created by bitbake-layers   *
*                                             *
***********************************************
NOTE: Tasks Summary: Attempted 570 tasks of which 554 didn't need to be rerun and all succeeded.
```

Run the following command to find out the recipe workdir

```
$ bitbake -e example | grep ^WORKDIR=
$ ll /mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/example/0.1-r0
...
```

As mentioned before, this recipe only runs some python command to print a message. No code is actually compiled.
The next step is to create another recipe, this time, compiling a code.
We are using devtoll add to add this new recipe to out layer
https://www.yoctoproject.org/docs/current/mega-manual/mega-manual.html#sdk-a-closer-look-at-devtool-add
and here there is a more complete explanation
https://github.com/joaocfernandes/Learn-Yocto/blob/master/develop/Recipe-c.md

First, lets create a propor dir structure to save the new recipe

```
$ cd build
$ mkdir meta-fred/recipes-example/hello
$ cd meta-fred/recipes-example/hello
$ nano hello.bb
SUMMARY = "Simple helloworld application"
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://helloworld.c"

TARGET_CC_ARCH += "${LDFLAGS}"

S = "${WORKDIR}"

do_compile() {
	${CC} helloworld.c -o helloworld
}

do_install() {
	install -d ${D}${bindir}
	install -m 0755 helloworld ${D}${bindir}
}
```

This is the place to store the source files:

```
$ mkdir files
$ cd files
$ nano helloworld.c

#include <stdio.h>
  
int main()
{
    printf("Hello World");
    return 0;
}
```

This recipe created is an example of a recipe where the code is locally stores within the recipe itself.
This is not an usual configuration and is recommemded only for testing purposes of small applications.
Next, return to the build dir and compile the new recipe:

```
$ cd build
$ bitbake hello
$ bitbake -e hello | grep ^WORKDIR=
WORKDIR="/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hello/1.0-r0"
$ cd /mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hello/1.0-r0
$ ll hello*
-rwxr-xr-x 1 build build 11556 Dec 17 20:08 helloworld*
-rw-r--r-- 1 build build    80 Dec 17 20:03 helloworld.c
```

To install this last application into a new built image, we need to add the following line into to `meta-fred/conf/local.conf`:

```
# add here the name of the recipes to be included into the image
IMAGE_INSTALL_append = " example"
IMAGE_INSTALL_append = " hello"
...
```

Pay attention to the initial space before hello, this space is required. 

## Recipe HelloMake

https://www.yoctoproject.org/docs/current/mega-manual/mega-manual.html#new-recipe-makefile-based-package

The next recipe shows how to write a recipe for a project with Make.

First, lets create a propor dir structure to save the new recipe:

```
$ cd build
$ mkdir meta-fred/recipes-example/hello
$ cd meta-fred/recipes-example/hello
$ nano hello.bb
SUMMARY = "Simple helloworld application"
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://helloworld.c"

TARGET_CC_ARCH += "${LDFLAGS}"

S = "${WORKDIR}"

do_compile() {
	${CC} helloworld.c -o helloworld
}

do_install() {
	install -d ${D}${bindir}
	install -m 0755 helloworld ${D}${bindir}
}
```

This is the place to store the source files :

```
$ mkdir files
$ cd files
$ nano helloworld.c

#include <stdio.h>
  
int main()
{
    printf("Hello Make");
    return 0;
}
```

## Recipe HelloCMake

source : https://github.com/joaocfernandes/Learn-Yocto/blob/master/develop/Recipe-CMake.md

The next recipe shows how to write a recipe for a project with CMake.

First, lets create a propor dir structure to save the new recipe:

```
$ cd build
$ mkdir meta-fred/recipes-example/hellocmake
$ cd meta-fred/recipes-example/hellocmake
$ nano hellocmake.bb

SUMMARY = "Simple Hello World Cmake application"
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
            file://CMakeLists.txt \
            file://hellocmake.cpp \
        "

S = "${WORKDIR}"

inherit cmake

EXTRA_OECMAKE = ""SUMMARY = "Simple Hello World Cmake application"
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
            file://CMakeLists.txt \
            file://hellocmake.c \
        "

S = "${WORKDIR}"

inherit cmake

EXTRA_OECMAKE = ""
```

This is the place to store the source files :

```
$ mkdir files
$ cd files
$ nano hellocmake.c
#include <stdio.h>
  
int main()
{
    printf("Hello CMake");
    return 0;
}
```

```
$ nano CMakeLists.txt
cmake_minimum_required(VERSION 3.9)
project (hellocmake)
add_executable(hellocmake hellocmake.c)
install(TARGETS hellocmake RUNTIME DESTINATION bin)
```

```
$ cd build
$ bitbake hellocmake
$ bitbake -e hellocmake | grep ^WORKDIR=
WORKDIR="/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hellocmake/1.0-r0"
$ cd /mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hellocmake/1.0-r0
ll build/hello*
-rwxr-xr-x 1 build build 14024 Dec 17 20:45 build/hellocmake*
```

## Recipe LibHello

This example shows how to build a library (shared or static), using cmake. 
Yocto requires a certain format for libraries, i.e. naming convention and also requirements from the building system as seen next. 

References:
 - [Shared libraries with Yocto](https://stackoverflow.com/questions/54026504/yocto-poky-install-and-use-shared-library-so-on-separate-layers)
 - [Creating Libraries in a PetaLinux Project](https://xilinx-wiki.atlassian.net/wiki/spaces/A/pages/18842475/PetaLinux+Yocto+Tips?view=blog)

First, create the directories as in the previous examples for the recipe libhello. The recipe file has nothing special, related to library.

```
$ nano hellolib.bb

SUMMARY = "Simple Hello Library with Cmake application"
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
            file://CMakeLists.txt \
            file://hellolib.c \
            file://hellolib.h \
        "

S = "${WORKDIR}"

inherit cmake

EXTRA_OECMAKE = ""
```

The super fency library:

```
$ nano files/hellolib.c
 
char * hello()
{
    return "My Lib";
}
```

```
$ nano files/hellolib.h

#ifndef _HELLOLIB_H
#define _HELLOLIB_H
char * hello();
#endif
```

And the cmake for building the library with Yocto.
This cmake file is consideralby more complex compared to the cmake for an application.
This is because Yocto requires a certain format for libraries, i.e. name must be lib*.
It also requires a library version and an install rule for the library and the headers, if available.
T

```
$ nano files/CMakeLists.txt

cmake_minimum_required(VERSION 3.9)
# the project version is required
project (hello VERSION 1.0 DESCRIPTION "My Hello Library")

# chooseee the library format: static or dynamic by
# replacing SHARED by STATIC to change the library type
#add_library(${CMAKE_PROJECT_NAME} SHARED libhello.c)
add_library(${CMAKE_PROJECT_NAME} STATIC libhello.c)

# VERSION and SOVERSION are required
set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES
    VERSION ${PROJECT_VERSION}
    SOVERSION 1
    PUBLIC_HEADER libhello.h)

# replace LIBRARY by ARCHIVE depending on the library format
install(TARGETS ${CMAKE_PROJECT_NAME}
	#LIBRARY DESTINATION lib
	ARCHIVE DESTINATION lib
	PUBLIC_HEADER DESTINATION include
)

# any addition file that needs to be installed
#install(FILES <filename> DESTINATION <dir>)

```

Check it out for more [cmake library configuration options](https://stackoverflow.com/questions/17511496/how-to-create-a-shared-library-with-cmake).


## Recipe HelloDep

This example creates and application that uses the library defined in the precious section. So, we need to define a depedency among these two recipes.
This example is also built with cmake.

Create the directories as in the previous examples.


```
SUMMARY = "Simple Hello World Cmake application that requires a library"
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
            file://CMakeLists.txt \
            file://hellodep.c \
        "

S = "${WORKDIR}"

inherit cmake

EXTRA_OECMAKE = ""
```

```
#include <stdio.h>
#include <hello.h>
  
int main()
{
    printf("Hello %s\n",hello());
    return 0;
}

```

/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hellodep/1.0-r0/recipe-sysroot/usr/lib/libhello.a

## Recipe HelloGit

https://blog.mbedded.ninja/programming/embedded-linux/yocto-project/adding-a-custom-app-to-a-yocto-build/

Follow the previous steps to create a new recipe called hellogit. 
The directory files is not required in this configurations.

```
DESCRIPTION = "Example Hello, World application for Yocto build using git."
SECTION = "examples"
DEPENDS = ""
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=96af5705d6f64a88e035781ef00e98a8"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}:"

SRCREV = "c96b1fdd0767a9a13b9fca9d91fd3975c44c9de4"
SRC_URI = "git://github.com/gbmhunter/YoctoHelloWorldApp.git"

S = "${WORKDIR}/git"

inherit autotools

# The autotools configuration I am basing this on seems to have a problem with a race condition when parallel make is enabled
PARALLEL_MAKE = ""
```

```
$ cd build
$ bitbake hellogit
```

## Run Linux Build And Test the Custom Apps

https://blog.mbedded.ninja/programming/embedded-linux/yocto-project/adding-a-custom-app-to-a-yocto-build/

???? p incluir o toolchain na image ?!?!?!?s

Build your Image $ bitbake -k core-image-sato -c populate_sdk


Default yocto images
https://www.yoctoproject.org/docs/current/ref-manual/ref-manual.html#ref-images





rootfs
/mnt/yocto/tmp/work/raspberrypi3-poky-linux-gnueabi/core-image-minimal/1.0-r0/rootfs/


crosscompiling
cmake .. -DCMAKE_SYSROOT=/mnt/yocto/tmp/work/raspberrypi3-poky-linux-gnueabi/core-image-minimal/1.0-r0/rootfs/

