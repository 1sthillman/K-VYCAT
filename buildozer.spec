[app]
title = MXW01 Printer
package.name = mxw01printer
package.domain = org.mxw

source.dir = .
source.include_exts = py,png,jpg,kv,atlas

version = 1.0

requirements = python3,kivy,pillow,pyjnius,android

orientation = portrait
fullscreen = 0

android.permissions = BLUETOOTH,BLUETOOTH_ADMIN,BLUETOOTH_SCAN,BLUETOOTH_CONNECT,ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION
android.api = 31
android.minapi = 26
android.ndk = 25b
android.accept_sdk_license = True

[buildozer]
log_level = 2
warn_on_root = 1
