LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES :=  glide-3.7.0:libs/glide-3.7.0.jar \
                                         disklrucache:libs/disklrucache.jar \
                                         gson-2.8.5:libs/gson-2.8.5.jar \
include $(BUILD_MULTI_PREBUILT)
include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := optional
LOCAL_MANIFEST_FILE := src/main/AndroidManifest.xml
LOCAL_SRC_FILES := $(call all-java-files-under, src/main/java) \
                   $(call all-java-files-under, ../mp3id3library/src/main/java) \
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/src/main/res \
                      $(LOCAL_PATH)/../ijkplayer/src/main/res \
LOCAL_JAVA_LIBRARIES := framework
LOCAL_STATIC_JAVA_LIBRARIES :=  glide-3.7.0 \
                                disklrucache \
                                gson-2.8.5 \
								android-support-v4 \
								android-support-v7-appcompat \
LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages android.support.v7.recyclerview
LOCAL_PACKAGE_NAME := JAC-MediaScanner
LOCAL_CERTIFICATE := platform
include $(BUILD_PACKAGE)
