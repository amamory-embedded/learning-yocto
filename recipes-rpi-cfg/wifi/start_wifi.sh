#!/bin/sh

# you can change the default static IP 
ifconfig wlan0 192.168.1.150
route add default gw 192.168.1.1

wpa_supplicant -Dnl80211 -iwlan0 -c/etc/wpa_supplicant.conf
: exit 0
