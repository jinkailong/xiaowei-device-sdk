# export NDKHOME=/Users/cejachen/Work/CaseDiskSpace/Android/android-ndk-r14b.mac
function debug_build_xwei_control_jni {
	PROGDIR=$(cd $(dirname $0); pwd -P)/control

	BUILDCONFIG=android_arm_device

	PROJECT_PATH=$PROGDIR/jni
	ndk-build NDK_APPLICATION_MK=$PROJECT_PATH/Application.mk APP_BUILD_SCRIPT=$PROJECT_PATH/Android.mk NDK_PROJECT_PATH=$PROJECT_PATH NDK_LIBS_OUT=$PROJECT_PATH/../libs
}

function debug_build_xwei_sdk_jni {
	PROGDIR=$(cd $(dirname $0); pwd -P)/xiaoweiSDK

	BUILDCONFIG=android_arm_device

	PROJECT_PATH=$PROGDIR/jni
	ndk-build NDK_APPLICATION_MK=$PROJECT_PATH/Application.mk APP_BUILD_SCRIPT=$PROJECT_PATH/Android.mk NDK_PROJECT_PATH=$PROJECT_PATH NDK_LIBS_OUT=$PROJECT_PATH/../libs
}

function debug_build_delete_shared_jni {
	PROGDIR=$(cd $(dirname $0); pwd -P)
	rm $PROGDIR/xiaoweiSDK/libs/armeabi/libstlport_shared.so
}

function debug_build_project {
echo "****************************       build jni xiaowei_sdk          *****************************"
	debug_build_xwei_sdk_jni
echo "****************************       build jni xiaowei_ctrl         *****************************"
	debug_build_xwei_control_jni

	debug_build_delete_shared_jni
}
debug_build_project
