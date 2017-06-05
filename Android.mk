LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := Led
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 GetuiSDK2.10.2.0
LOCAL_JNI_SHARED_LIBRARIES := libgetuiext2

#LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

##################################################
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := android-support-v4:libs/android-support-v4.jar
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := GetuiSDK2.10.2.0:libs/GetuiSDK2.10.2.0.jar
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libgetuiext2:libs/armeabi-v7a/libgetuiext2.so
LOCAL_MODULE_TAGS := optional 
include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
