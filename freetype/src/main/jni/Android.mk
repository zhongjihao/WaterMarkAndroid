LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := libfreetype
LOCAL_SRC_FILES := $(LOCAL_PATH)/lib/libfreetype.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := libopencv_java3
LOCAL_SRC_FILES := $(LOCAL_PATH)/lib/libopencv_java3.so
include $(PREBUILT_SHARED_LIBRARY)



include $(CLEAR_VARS)

LOCAL_MODULE    	:= freetype_jni

LOCAL_SRC_FILES 	:= \
	./com_sprd_freetype_FreeTypeJni.cpp \
	./src/CvxText.cpp

LOCAL_C_INCLUDES += $(LOCAL_PATH)/include/



LOCAL_SHARED_LIBRARIES := libfreetype libopencv_java3


LOCAL_LDLIBS := -llog -landroid

LOCAL_CFLAGS += -frtti -fexceptions -std=c++11

include $(BUILD_SHARED_LIBRARY)