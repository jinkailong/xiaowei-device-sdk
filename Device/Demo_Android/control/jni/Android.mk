LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := libCtrlModule
LOCAL_SRC_FILES :=../../../CtrlModule/out/$(TARGET_ARCH_ABI)/libCtrlModule.so
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE   := control
LOCAL_SRC_FILES := Utils.cpp
LOCAL_SRC_FILES += XWeiControl_jni.cpp
LOCAL_SRC_FILES += XWeiApp_jni.cpp
LOCAL_SRC_FILES += XWeiCommon_jni.cpp
LOCAL_SRC_FILES += XWeiMedia_jni.cpp
LOCAL_SRC_FILES += XWeiOuterSkill_jni.cpp
LOCAL_SRC_FILES += PlayerCallback.cpp
LOCAL_SRC_FILES += OpusDecoder.cpp
LOCAL_SRC_FILES += ScopedJNIEnv.cpp

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../CtrlModule/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../CtrlModule/library/rapidjson/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../CtrlModule/library/log4c/src
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../CtrlModule/library/opus-1.2.1/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../../../../DeviceSDK/interface/linux

LOCAL_CPP_FEATURES += rtti
LOCAL_CPP_FEATURES += exceptions
APP_CPPFLAGS += -fexceptions

LOCAL_LDFLAGS += $(LOCAL_PATH)/../../../CtrlModule/out/$(TARGET_ARCH_ABI)/libCtrlModule.so
LOCAL_LDFLAGS += $(LOCAL_PATH)/../../../../DeviceSDK/release/Android/$(TARGET_ARCH_ABI)/libxiaoweiSDK.so
LOCAL_LDFLAGS += -llog

include $(BUILD_SHARED_LIBRARY)
