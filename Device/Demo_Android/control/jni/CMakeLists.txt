cmake_minimum_required(VERSION 3.6)

# include_directories(${ANDROID_NDK}/sources/android/support/include)
# include_directories(${ANDROID_NDK}/sources/cxx-stl/stlport/stlport/)
include_directories(../../)
include_directories(../../CtrlModule/include)
include_directories(../../../DeviceSDK/interface/linux)
include_directories(../../CtrlModule/library/opus-1.2.1/include)
include_directories(../../CtrlModule/library/log4c/src)


SET(TARGET_ARCH_ABI "armeabi")

add_library( # 生成函数库的名字
             control

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
              jni/OpusDecoder.cpp
              jni/PlayerCallback.cpp
              jni/ScopedJNIEnv.cpp
              jni/XWeiControl_jni.cpp
              jni/XWeiApp_jni.cpp
              jni/XWeiCommon_jni.cpp
              jni/XWeiMedia_jni.cpp
              jni/XWeiOuterSkill_jni.cpp
              jni/Utils.cpp
             )

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.

              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       control

                       ${CMAKE_CURRENT_SOURCE_DIR}/../../CtrlModule/out/${TARGET_ARCH_ABI}/libCtrlModule.so

                       ${log-lib}


                       # Links the target library to the log library
                       # included in the NDK.
                       )
