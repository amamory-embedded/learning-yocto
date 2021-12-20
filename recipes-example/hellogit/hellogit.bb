DESCRIPTION = "Example Hello World application for Yocto build Using git."
SECTION = "examples"
DEPENDS = ""
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=96af5705d6f64a88e035781ef00e98a8"

FILESEXTRAPATHS_prepend := "${THISDIR}/${PN}-${PV}:"

SRCREV = "c96b1fdd0767a9a13b9fca9d91fd3975c44c9de4"
SRC_URI = "git://github.com/gbmhunter/YoctoHelloWorldApp.git"

S = "${WORKDIR}/git"

inherit autotools

PARALLEL_MAKE = ""
