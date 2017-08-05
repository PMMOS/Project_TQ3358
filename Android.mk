LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := Led
LOCAL_CERTIFICATE := platform

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 GetuiSDK2.10.2.0 commons-lang-2.5 Core LibFunSDK stickygridheaders dewarp
LOCAL_JNI_SHARED_LIBRARIES := libgetuiext2 libFunSDK libeznat libh264tomp4

#LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)

##################################################
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := android-support-v4:libs/android-support-v4.jar 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := GetuiSDK2.10.2.0:libs/GetuiSDK2.10.2.0.jar 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := commons-lang-2.5:libs/commons-lang-2.5.jar 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := Core:libs/Core.jar 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := dewarp:libs/dewarp.jar 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := LibFunSDK:libs/LibFunSDK.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := stickygridheaders:libs/stickygridheaders.jar
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)
LOCAL_PREBUILT_LIBS := libgetuiext2:libs/armeabi-v7a/libgetuiext2.so
LOCAL_PREBUILT_LIBS := libFunSDK:libs/armeabi/libFunSDK.so
LOCAL_PREBUILT_LIBS := libeznat:libs/armeabi/libeznat.so
LOCAL_PREBUILT_LIBS := libh264tomp4:libs/armeabi/libh264tomp4.so
LOCAL_MODULE_TAGS := optional 
include $(BUILD_MULTI_PREBUILT)

# Use the folloing include to make our test apk.
include $(call all-makefiles-under,$(LOCAL_PATH))
