
# Learning Yocto

This tutorial goes through the process, step-by-step, of adding your own software into a Linux distribution generate by Yocto.
It's initial goal was to document my own learning process, but, perhaps it can also be usefull for other begginers in Yocto.
This tutorial is not meant to be a complete Yocto reference. The idea is to be that initial little push, so you canstart figuring out the Yocto workflow. 

## Instalation

We use a docker container with Yocto and VNC installed. Check-out the [cointerner manual](https://github.com/amamory-embedded/docker-yocto-vnc) to see it's features and how to install it.

## Building the image for raspberry pi3

Lets build the main parts of the Linux image: kernel, rootfs, etc. Later we build our custom recipes on top of this build. This step takes a long time ...

```
$ cd rpi
$ source /opt/yocto/dunfell/src/poky/oe-init-build-env
$ bitbake core-image-minimal -c populate_sdk
```

I am not sure if it is mandatory to include SDK (i.e. `populate_sdk`) in the image. This needs some additional testing in the future.
In addition, there are other default Yocto images besides `core-image-minimal`. Check the [reference images here](https://www.yoctoproject.org/docs/current/ref-manual/ref-manual.html#ref-images).

## Creating a Custom Layer

Everywhere you read about Yocto recommends that you [create your own layer](source: https://www.yoctoproject.org/docs/current/mega-manual/mega-manual.html#creating-your-own-general-layer)
to deploy your software in the Linux image. So this section goes through this process of creating the layer where your recipes will be added.
 
```
$ bitbake-layers show-layers
$ bitbake-layers create-layer meta-learning
$ bitbake-layers add-layer meta-learning
$ bitbake-layers show-layers
NOTE: Starting bitbake server...
layer                 path                                      priority
==========================================================================
meta                  /opt/yocto/dunfell/src/poky/meta          5
meta-poky             /opt/yocto/dunfell/src/poky/meta-poky     5
meta-yocto-bsp        /opt/yocto/dunfell/src/poky/meta-yocto-bsp  5
meta-learning         /home/build/rpi/build/meta-learning           6
meta-raspberrypi      /opt/yocto/dunfell/src/meta-raspberrypi   9
meta-oe               /opt/yocto/dunfell/src/meta-openembedded/meta-oe  6
meta-networking       /opt/yocto/dunfell/src/meta-openembedded/meta-networking  5
meta-python           /opt/yocto/dunfell/src/meta-openembedded/meta-python
$ tree meta-learning/
meta-learning/
├── conf
│   └── layer.conf
├── COPYING.MIT
├── README
└── recipes-example
    └── example
        └── example_0.1.bb
```

Next we start configuring the recipe as described below.

```
$ ~/rpi/build
$ cat meta-learning/recipes-example/example/example_0.1.bb
SUMMARY = "bitbake-layers recipe"
DESCRIPTION = "Recipe created by bitbake-layers"
LICENSE = "MIT"

do_compile() {
	echo "Example recipe created by bitbake-layers" >> ${WORKDIR}/example
}

do_install() {
	install -d ${D}${datadir}
	install -m 0644 ${WORKDIR}/example ${D}${datadir}
}

```

The rules `do_compile` and `do_install` are, in practice, only copying a file into the image.

```
$ cd ~/rpi/build
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
meta-learning            = "<unknown>:<unknown>"
meta-raspberrypi     = "dunfell:934064a01903b2ba9a82be93b3f0efdb4543a0e8"
meta-oe              
meta-networking      
meta-python          = "dunfell:69f94af4d91215e7d4e225bab54bf3bcfee42f1c"

Initialising tasks: 100% |##############################################################################################################| Time: 0:00:00
Sstate summary: Wanted 7 Found 0 Missed 7 Current 134 (0% match, 95% complete)
NOTE: Executing Tasks
NOTE: Tasks Summary: Attempted 570 tasks of which 554 didn't need to be rerun and all succeeded.
```

Run the following command to find out the recipe workdir

```
$ bitbake -e example | grep ^WORKDIR=
WORKDIR="/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/example/0.1-r0"
$ find /mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/example/0.1-r0 -name example
...
/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/example/0.1-r0/image/usr/share/example
$ cat /mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/example/0.1-r0/image/usr/share/example
Example recipe created by bitbake-layers
```

The file got deployed in the `/usr/share` directory.
This link has more information about how to make [a recipe that copies a file into the image](http://embeddedguruji.blogspot.com/2019/02/yocto-recipe-to-copy-single-file-on.html).

## Recipe Hello

As mentioned before, this recipe only runs some python command to print a message. No code is actually compiled.
The next step is to create another recipe, this time, compiling a code.
First, lets create a proper directory structure to save the new recipe:

```
$ cd ~/rpi/build
$ mkdir meta-learning/recipes-example/hello
$ cd meta-learning/recipes-example/hello
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

Check this other [link](http://www.embeddedlinux.org.cn/OEManual/recipes_examples.html) to learn more about how to write `do_install`.

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

The resulting directory tree should be like this one:
```
:~/rpi/build/meta-learning/recipes-example/hello$ tree
.
├── files
│   └── helloworld.c
└── hello.bb
```

This recipe created is an example of a recipe where the code is locally stores within the recipe itself.
This is not an usual configuration and is recommemded only for testing purposes of small applications.
Next, return to the build dir and compile the new recipe:

```
$ cd ~/rpi/build
$ bitbake hello
$ bitbake -e hello | grep ^WORKDIR=
WORKDIR="/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hello/1.0-r0"
$ cd /mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hello/1.0-r0
$ ll hello*
-rwxr-xr-x 1 build build 11556 Dec 17 20:08 helloworld*
-rw-r--r-- 1 build build    80 Dec 17 20:03 helloworld.c
```

To install this last application into a new built image, we need to add the following lines into to `meta-learning/conf/local.conf`:

```
# add here the name of the recipes to be included into the image
IMAGE_INSTALL_append = " example"
IMAGE_INSTALL_append = " hello"
...
```

Pay attention to the initial space before hello, this space is required. 

## Recipe HelloMake

The next recipe shows how to write a recipe for a [project with Make](https://www.yoctoproject.org/docs/current/mega-manual/mega-manual.html#new-recipe-makefile-based-package).
First, lets create a propor dir structure to save the new recipe:

```
$ cd ~/rpi/build
$ mkdir meta-learning/recipes-example/hellomake
$ cd meta-learning/recipes-example/hellomake
$ nano hellomake.bb
SUMMARY = "Simple helloworld application with Make"
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

This is the place to store the source file and the Makefile:

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
$ nano Makefile
....
```

## Recipe HelloCMake

The next recipe shows how to write a recipe for a project with CMake. More info [here](https://github.com/joaocfernandes/Learn-Yocto/blob/master/develop/Recipe-CMake.md)
First, lets create a proper dir structure to save the new recipe:

```
$ cd ~/rpi/build
$ mkdir meta-learning/recipes-example/hellocmake
$ cd meta-learning/recipes-example/hellocmake
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

EXTRA_OECMAKE = ""
```

This is the place to store the source file and the CMakeLists.txt:

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

$ nano CMakeLists.txt
cmake_minimum_required(VERSION 3.9)
project (hellocmake)
add_executable(hellocmake hellocmake.c)
install(TARGETS hellocmake RUNTIME DESTINATION bin)
```

Finally, let's build the recipe:

```
$ cd ~/rpi/build
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

First, create the directories as in the previous examples for the recipe `libhello`. The recipe file has nothing specially related to a library.

```
$ nano hellolib.bb

SUMMARY = "Simple Hello Library with Cmake application"
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
            file://CMakeLists.txt \
            file://hellolib.c \
            file://hello.h \
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
$ nano files/hello.h

#ifndef _HELLOLIB_H
#define _HELLOLIB_H
char * hello();
#endif
```

And the cmake for building the library with Yocto.
This cmake file is consideralby more complex compared to the cmake for an application.
This is because Yocto requires a certain format for libraries, i.e. name must be lib*.
It also requires a library version and an install rule for the library and the headers, if available.


```
$ nano files/CMakeLists.txt

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
	#ARCHIVE DESTINATION /usr/lib
	PUBLIC_HEADER DESTINATION /usr/include
)

# any addition file that needs to be installed
#install(FILES <filename> DESTINATION <dir>)

```

Note in the cmkae file that we are forcing the install to use the default paths for libraries and includes.
This will make it easier to find them later in the next recipe. 
 
Check it out for more [cmake library configuration options](https://stackoverflow.com/questions/17511496/how-to-create-a-shared-library-with-cmake).

## Recipe HelloDep

This example creates and application that uses the library `libhello` defined in the previous section. So, we need to define a depedency among these two recipes.
This example is also built with cmake.

First, create the directories as in the previous examples.
Name the bitbake file as hellodep.bb. Note in the end of the files includes the depedency clauses among the recipes. DEPEDENCY is for build-time depedencies while RDEPENDECY is for runtime dependencies. 
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

DEPENDS += "libhello"
RDEPENDS_${PN} += "libhello"
```

Name the source code as hellodep.c
```
#include <stdio.h>
#include <hello.h>
  
int main()
{
    printf("Hello %s\n",hello());
    return 0;
}
```

And finally the CMakeLists.txt file. Note that this cmake file is very simplified since `libhello` is deployed in the
default `/usr/lib` and `/usr/include` directories. Otherwise, it's recommended to create a cmake module for the library
so that the user applications can easily find the library with the cmake command `find_package`.
See the comments in the CMakeLists.txt for further pointers.
```
cmake_minimum_required(VERSION 3.9)
project (hellodep)

# the source code to tha app
add_executable(${PROJECT_NAME} hellodep.c)

# libraries to be linked
target_link_libraries(${PROJECT_NAME} hello)

INSTALL(TARGETS ${PROJECT_NAME}
        RUNTIME DESTINATION bin
)
```

Now, let's build the new recipe. Sometimes, if you find an error while building a recipe, it's just a matter of cleaning it before building it again.

```
cd ~/rpi/build
bitbake hellodep -c cleanall
bitbake hellodep
```

Let's search the generated files for this new recipe:

```
$ find /mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hellodep/1.0-r0/recipe-sysroot/ -name "*hello*"
/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hellodep/1.0-r0/recipe-sysroot/usr/include/hello.h
/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hellodep/1.0-r0/recipe-sysroot/usr/lib/libhello.so
/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hellodep/1.0-r0/recipe-sysroot/usr/lib/libhello.so.1.0
/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hellodep/1.0-r0/recipe-sysroot/usr/lib/libhello.so.1
/mnt/yocto/tmp/work/cortexa7t2hf-neon-vfpv4-poky-linux-gnueabi/hellodep/1.0-r0/recipe-sysroot/sysroot-providers/libhello
```

We see the library, it's header file, both required to build `hellodep`.
Next, make sure that the meta-learnning layer.conf file has the following lines to include the recipes to the image:

```
IMAGE_INSTALL_append = " libhello"
IMAGE_INSTALL_append = " hellodep"
```

Then build the Linux image again to include the new recipes into the image.

```
$ bitbake core-image-minimal
$ find /mnt/yocto/tmp/work/raspberrypi3-poky-linux-gnueabi/core-image-minimal/1.0-r0/rootfs/ -name *hello*
...
/mnt/yocto/tmp/work/raspberrypi3-poky-linux-gnueabi/core-image-minimal/1.0-r0/rootfs/usr/lib/libhello.so.1.0
/mnt/yocto/tmp/work/raspberrypi3-poky-linux-gnueabi/core-image-minimal/1.0-r0/rootfs/usr/lib/libhello.so.1
/mnt/yocto/tmp/work/raspberrypi3-poky-linux-gnueabi/core-image-minimal/1.0-r0/rootfs/usr/bin/hellodep
```

We can see that both the `libhello` shared library and the `hellodep` application were deployed into the image. Success !

## Recipe HelloGit

As mentioned before, it's not recommended for an actual deployment to combine the Yocto recipes and the soruce code.
So, this new recipe shows how to separate these parts by linking the source code repository into the recipe. 

Follow the previous steps to create a new recipe called `hellogit`. 
The directory `files` is not required in this configurations since it does not include source code.

This is the bitbake file called `hellogit.bb`. This recipe uses autoconf and make.
Note that the variables `SRCREV` and `SRC_URI` define, respectively, the commit hash and the git repository URL.

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

Now, lets test the recipe.

```
$ cd ~/rpi/build
$ bitbake hellogit
```

In the same directory of `hellogit.bb`, let's create another recipe called `hellogitcmake.bb` which uses cmake instead of autoconf.

```
DESCRIPTION = "Example Hello World application for Yocto build Using git and Cmake."
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3d7054b26bdd0f2f5fc6b2e53f28783d"

SRCREV = "47f73c318bb726bb2a5cf8e4d58204ba5fe3d207"
SRC_URI = "git://github.com/amamory-embedded/Hello-World-Cmake.git/;branch=main"

S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE = ""
```

Note that this repository names the master branch as `main`, instead of `master`. This way we have to specify the branch name.
Other than that, we just have to update the hashes (the license and the repository hashes) and inherit cmake.

## Run Linux Build And Test the Custom Apps

Once all recipes we want ot include into the image where tested separately, the next step is to actuall include them into the image.
For this, edit `meta-learning/conf/local.conf` to include the following lines

```
# add here the name of the recipes to be included into the image
IMAGE_INSTALL_append = " example"
IMAGE_INSTALL_append = " hello"
#IMAGE_INSTALL_append = " hellomake"
IMAGE_INSTALL_append = " hellocmake"
IMAGE_INSTALL_append = " libhello"
IMAGE_INSTALL_append = " hellodep"
IMAGE_INSTALL_append = " hellogit"
IMAGE_INSTALL_append = " hellogitcmake"
```

Next, time for building the recipes togheter.

```
$ cd ~/rpi/build
$ bitbake core-image-minimal
$ find /mnt/yocto/tmp/work/raspberrypi3-poky-linux-gnueabi/core-image-minimal/1.0-r0/rootfs/ -name *hello*
```
