SUMMARY = "Simple Hello World Make application"
SECTION = "examples"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "\
            file://Makefile \
            file://hellomake.c \
        "

S = "${WORKDIR}"

inherit make

EXTRA_OEMAKE = ""

# in case it's necessary to add more flgas than those already defined inthe Makefile
#CFLAGS_prepend = "-I ${S}/include"

# in case it's necessary to do additional steps before running make ... The waythis the compile task is defined, could be removed since this is the default behaviour.
do_compile() {
        oe_runmake
}

# if the makefile does not have a 'install' rule, then it's necessary to specify where the app will be installed.
do_install() {
        install -d ${D}${bindir}
        install -m 0755 ${S}/hellomake ${D}${bindir}
}