SUMMARY = "Simple Hello Library with Cmake application"
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
            file://CMakeLists.txt \
            file://libhello.c \
            file://libhello.h \
        "

# package version
#PV = "1.0"

S = "${WORKDIR}"

inherit cmake

EXTRA_OECMAKE = ""
