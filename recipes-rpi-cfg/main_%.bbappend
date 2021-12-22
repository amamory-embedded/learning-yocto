FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
     
# add or remove the resources according to your application
#SRC_URI += "\
#            file://uart.cfg \
#            file://wifi.cfg \
#            file://usb-thetering.cfg \
#        "

SRC_URI += "\
            file://uart.cfg \
            file://usb-thetering.cfg \
        "