DESCRIPTION = "Example Hello World application for Yocto build Using git and Cmake."
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=3d7054b26bdd0f2f5fc6b2e53f28783d"

SRCREV = "47f73c318bb726bb2a5cf8e4d58204ba5fe3d207"
SRC_URI = "git://github.com/amamory-embedded/Hello-World-Cmake.git/;branch=main"

S = "${WORKDIR}/git"

inherit cmake

EXTRA_OECMAKE = ""
