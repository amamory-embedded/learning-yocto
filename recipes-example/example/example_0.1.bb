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
