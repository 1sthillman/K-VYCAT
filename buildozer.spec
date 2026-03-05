[app]
title = MXW01 Printer
package.name = mxw01printer
package.domain = com.mxw

source.dir = .
source.include_exts = py,png,jpg,kv,atlas

version = 1.0

requirements = python3==3.10.6,hostpython3==3.10.6,kivy==2.2.1,pillow,bleak,qrcode,pyjnius,android

orientation = portrait
fullscreen = 0

android.permissions = BLUETOOTH,BLUETOOTH_ADMIN,BLUETOOTH_SCAN,BLUETOOTH_CONNECT,ACCESS_FINE_LOCATION,ACCESS_COARSE_LOCATION,INTERNET
android.api = 33
android.minapi = 21
android.ndk = 25b
android.accept_sdk_license = True
android.archs = arm64-v8a
android.gradle_dependencies = 

[buildozer]
log_level = 2
warn_on_root = 1
