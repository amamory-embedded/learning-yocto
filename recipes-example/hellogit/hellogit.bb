DESCRIPTION = "Example Hello World application for Yocto build Using git and Autoconf."
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=0ec5801450d6b777d973eb956c021332"

SRCREV = "22f4bf448930e5d92195c36a10bdf3662c577699"
SRC_URI = "git://github.com/amamory-embedded/Hello-World-Autoconf.git"

S = "${WORKDIR}/git"

inherit autotools

PARALLEL_MAKE = ""
