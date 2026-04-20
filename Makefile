tag := latest

CPU_ARCH = $(shell uname -p)

.PHONY: screengrab

screengrab:
	adb shell input keyevent KEYCODE_SYSRQ