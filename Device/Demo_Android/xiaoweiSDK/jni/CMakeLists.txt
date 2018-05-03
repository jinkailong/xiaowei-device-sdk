cmake_minimum_required(VERSION 3.6)

# include_directories(${ANDROID_NDK}/sources/android/support/include)
# include_directories(${ANDROID_NDK}/sources/cxx-stl/stlport/stlport/)
include_directories(../../../DeviceSDK/interface/linux)
include_directories(../../../DeviceSDK/interface/android)
include_directories(../../../DeviceSDK/xiaoweiSDK/sdkimpl)
include_directories(../../../DeviceSDK/xiaoweiSDK/sdkimpl/ipcamera)
include_directories(../../../DeviceSDK/xiaoweiSDK/modules/MsgCenterEx)
include_directories(../../../DeviceSDK/xiaoweiSDK/modules/PublicDevice)
include_directories(../../../DeviceSDK/xiaoweiSDK/zmq/include)
include_directories(../../../DeviceSDK/xiaoweiSDK/library/SuperEasyJSON)

add_library( # Specifies the name of the library.
             xiaowei

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             jni/XWSDK.cpp
             jni/Device.cpp
             jni/FileTransfer.cpp
             jni/BinderList.cpp
             jni/WifiDecoder.cpp
             jni/OTA.cpp
             jni/QCloudAudio.cpp
             jni/Util.cpp
             jni/base64.cpp
             jni/QQCall.cpp)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       xiaowei

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

target_link_libraries( # Specifies the target library.
                      xiaowei

                      # Links the target library to the log library
                      # included in the NDK.
                      ${CMAKE_CURRENT_SOURCE_DIR}/../../../DeviceSDK/release/Android/${ANDROID_ABI}/libxiaoweiSDK.so)
