APP_STL := gnustl_shared

APP_PLATFORM := android-21

APP_CPPFLAGS += -frtti
APP_CPPFLAGS += -fexceptions
APP_CPPFLAGS += -std=c++11

APP_CPPFLAGS += -DANDROID

APP_ABI := armeabi-v7a
NDK_TOOLCHAIN_VERSION := 4.9

APP_MODULES := freetype_jni
