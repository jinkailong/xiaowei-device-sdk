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

class CGlobalJNIEnv {
public:
    explicit CGlobalJNIEnv();

    ~CGlobalJNIEnv();

    void InitJNIObject(JNIEnv *env, jobject jniObject);

    jobject GetObject();

    jclass GetClass();

    static JNIEnv *GetJNIEnv(bool *pNeedRelease);

    static void Util_ReleaseEnv();

private:
    friend jint JNI_OnLoad(JavaVM *vm, void *reserved);

    static JavaVM *s_JVM;

    jobject jni_object_;
    jclass jni_class_;
};