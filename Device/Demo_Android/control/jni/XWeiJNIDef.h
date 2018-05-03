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
#define LOGFILTER    "XWEI_CONTROL_JNI"

#ifdef JNI_VERSION_1_4
#define JNI_VER JNI_VERSION_1_4
#endif

#ifdef JNI_VERSION_1_5
#undef JNI_VER
#define JNI_VER JNI_VERSION_1_5
#endif
#ifdef JNI_VERSION_1_6
#undef JNI_VER
#define JNI_VER JNI_VERSION_1_6
#endif