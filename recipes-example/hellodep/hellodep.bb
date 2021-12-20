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

# https://embeddedguruji.blogspot.com/2018/12/difference-between-rdepends-and-depends.html
#DEPENDS -> Build Time Dependency
#RDEPENDS -> Run Time Dependency
#DEPENDS: When a recipe 'A' is DEPENDS on recipe 'B'. In this case, Bitbake first builds recipe 'B' and then recipe 'A'
#RDEPENDS: When a recipe 'A' is RDEPENDS on recipe 'B'. In this case, Bitbake deploys 'B' on the target system when it deploys 'A'
DEPENDS += "libhello"
# https://lynxbee.com/yocto-build-time-depends-vs-run-time-rdepends-dependency/
RDEPENDS_${PN} += "libhello"
