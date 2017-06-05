LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_PRELINK_MODULE := false

LOCAL_SRC_FILES:= \
	LedService.cpp

LOCAL_SHARED_LIBRARIES := \
	libutils \
	libbinder \
	libhardware \

LOCAL_MODULE:= libled

LOCAL_MODULE_TAGS := optional

include $(BUILD_SHARED_LIBRARY)
