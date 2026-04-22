tag := latest

# Load variables from .env if present (e.g. DEVICE=XXXX)
-include .env
export

CPU_ARCH = $(shell uname -p)
GRADLE = ./gradlew
ADB = adb
DEVICE ?= $(error DEVICE is not set. Define it in .env (see .env.example) or pass DEVICE=... on the command line)

.PHONY: screengrab push
.PHONY: env_decrypt env_encrypt

screengrab:
	adb shell input keyevent KEYCODE_SYSRQ

debug:
	$(GRADLE) assembleDebug && $(ADB) -s $(DEVICE)  install -r app/build/outputs/apk/debug/app-debug.apk

push:
	$(GRADLE) assembleDebug --stacktrace && $(ADB) -s $(DEVICE) push app/build/outputs/apk/debug/app-debug.apk /sdcard/Download

env_decrypt:
	gpg --batch --yes --decrypt .env.gpg > .env

env_encrypt:
	gpg -c --yes .env