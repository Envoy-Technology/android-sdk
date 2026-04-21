tag := latest

CPU_ARCH = $(shell uname -p)
GRADLE = ./gradlew
ADB = adb
DEVICE = OZ69FQTOHUKFGENB

.PHONY: screengrab push

screengrab:
	adb shell input keyevent KEYCODE_SYSRQ

debug:
	$(GRADLE) assembleDebug && $(ADB) -s $(DEVICE)  install -r app/build/outputs/apk/debug/app-debug.apk

push:
	$(GRADLE) assembleDebug --stacktrace && $(ADB) -s $(DEVICE) push app/build/outputs/apk/debug/app-debug.apk /sdcard/Download
