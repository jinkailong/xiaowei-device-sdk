/*
 * Tencent is pleased to support the open source community by making  XiaoweiSDK Demo Codes available.
 *
 * Copyright (C) 2017 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

#include "XWeiJNIDef.h"
#include "ScopedJNIEnv.h"

CGlobalJNIEnv::CGlobalJNIEnv() {
    jni_object_ = NULL;
    jni_class_ = NULL;

}

CGlobalJNIEnv::~CGlobalJNIEnv() {
    bool needRelease = false;
    JNIEnv *env = GetJNIEnv(&needRelease);
    if (env) {
        if (jni_object_) {
            env->DeleteGlobalRef(jni_object_);
        }
        if (jni_class_) {
            env->DeleteGlobalRef(jni_class_);
        }
    }
    if (needRelease) {
        Util_ReleaseEnv();
    }
}

void CGlobalJNIEnv::InitJNIObject(JNIEnv *env, jobject jniObject) {
    jni_object_ = env->NewGlobalRef(jniObject);
    jni_class_ = env->GetObjectClass(jniObject);
}

jobject CGlobalJNIEnv::GetObject() {
    return jni_object_;
}

jclass CGlobalJNIEnv::GetClass() {
    return jni_class_;
}

JavaVM *CGlobalJNIEnv::s_JVM = NULL;

JNIEnv *CGlobalJNIEnv::GetJNIEnv(bool *pNeedRelease) {
    if (pNeedRelease) *pNeedRelease = false;
    JNIEnv *env = NULL;
    if (!s_JVM) {
        __android_log_write(ANDROID_LOG_ERROR, LOGFILTER, "JVM is NULL, no JVM yet");
    } else {
        if (JNI_OK != s_JVM->GetEnv(reinterpret_cast<void **> (&env), JNI_VER)) {
            if (JNI_OK == s_JVM->AttachCurrentThread(&env, NULL)) {
                if (pNeedRelease) *pNeedRelease = true;
            } else {
                env = NULL;
                __android_log_write(ANDROID_LOG_ERROR, LOGFILTER, "JVM could not create JNI env");
            }
        }
    }

    return env;
}

void CGlobalJNIEnv::Util_ReleaseEnv() {
    if (s_JVM) {
        if (JNI_OK != s_JVM->DetachCurrentThread()) {
            __android_log_write(ANDROID_LOG_ERROR, LOGFILTER, "JVM could not release JNI env");
        }
    }
}
