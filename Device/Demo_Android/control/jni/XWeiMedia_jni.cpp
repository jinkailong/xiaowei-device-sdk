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
#include <time.h>
#include <android/log.h>

#include "AudioApp.h"
#include "Player.h"
#include "Media.h"
#include "Playlist.h"
#include "ScopedJNIEnv.h"

#ifdef __cplusplus
extern "C" {
#endif

//jobject g_xwei_media_jni_obj = NULL;
CGlobalJNIEnv *g_xwei_media_jni = NULL;
jclass g_class_player_info = NULL;
jclass g_class_mediainfo = NULL;
jclass g_class_play_list_info = NULL;

JNIEXPORT void JNICALL Java_com_tencent_xiaowei_control_XWeiMedia_nativeInit(JNIEnv *env, jclass service) {
    g_xwei_media_jni = new CGlobalJNIEnv;
    g_xwei_media_jni->InitJNIObject(env, service);

    jclass cls_player_info = env->FindClass("com/tencent/xiaowei/control/info/XWeiPlayerInfo");
    g_class_player_info = (jclass) env->NewGlobalRef(cls_player_info);

    jclass cls_media_info = env->FindClass("com/tencent/xiaowei/control/info/XWeiMediaInfo");
    g_class_mediainfo = (jclass) env->NewGlobalRef(cls_media_info);

    jclass cls_play_list_info = env->FindClass("com/tencent/xiaowei/control/info/XWeiPlaylistInfo");
    g_class_play_list_info = (jclass) env->NewGlobalRef(cls_play_list_info);
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiMedia_nativeUninit(JNIEnv *env, jclass service) {
    delete g_xwei_media_jni;
    g_xwei_media_jni = NULL;
}

JNIEXPORT void JNICALL
Java_com_tencent_xiaowei_control_XWeiMedia_txcPlayerStateChange(JNIEnv *_env, jclass service, jint id,
                                                        jint state_code) {
    __android_log_print(ANDROID_LOG_DEBUG, "XWeiControlJNI_",
                        "Java_com_tencent_xiaowei_control_XWeiMedia_txcPlayerStateChange, %d", state_code);
    txc_player_statechange(id, TXC_PLAYER_STATE(state_code));
}

JNIEXPORT jboolean JNICALL
Java_com_tencent_xiaowei_control_XWeiMedia_txcPlayerControl(JNIEnv *_env, jclass service, jint id,
                                                    jint control_code, jint arg1, jint arg2) {
    __android_log_print(ANDROID_LOG_DEBUG, "XWeiControlJNI_",
                        "Java_com_tencent_xiaowei_control_XWeiMedia_txcPlayerControl, %d",
                        control_code);
    return (jboolean) txc_player_control(id, player_control(control_code), arg1, arg2);
}

JNIEXPORT jobject JNICALL
Java_com_tencent_xiaowei_control_XWeiMedia_txcGetPlayerInfo(JNIEnv *env, jclass service, jint id) {
    jobject result_info = NULL;
    const txc_player_info_t *player_info = txc_get_player_info(id);
    if (player_info) {
        JNIEnv *penv = env;
        if (penv) {
            jclass cls_info = penv->FindClass("com/tencent/xiaowei/control/info/XWeiPlayerInfo");
            if (cls_info) {
                result_info = penv->AllocObject(cls_info);
                if (result_info) {
                    jfieldID fld_status = penv->GetFieldID(cls_info, "status", "I");
                    jfieldID fld_repeatMode = penv->GetFieldID(cls_info, "repeatMode", "I");

                    jfieldID fld_playlist_id = penv->GetFieldID(cls_info, "playlistId", "I");

                    jfieldID fld_volume = penv->GetFieldID(cls_info, "volume", "I");
                    jfieldID fld_qulity = penv->GetFieldID(cls_info, "quality", "I");

                    if (fld_status && fld_repeatMode
                        && fld_playlist_id
                        && fld_volume && fld_qulity) {
                        penv->SetIntField(result_info, fld_status, player_info->status);
                        penv->SetIntField(result_info, fld_repeatMode, player_info->repeatMode);

                        penv->SetIntField(result_info, fld_playlist_id, player_info->playlist_id);

                        penv->SetIntField(result_info, fld_volume, player_info->volume);
                        penv->SetIntField(result_info, fld_qulity, player_info->qulity);
                    }

                    //  TODO:  release local ref of result_info???
                }

                penv->DeleteLocalRef(cls_info);
            }
        }
    }

    return result_info;
}

JNIEXPORT jobject JNICALL
Java_com_tencent_xiaowei_control_XWeiMedia_txcGetPlaylistInfo(JNIEnv *env, jclass service,
                                                      jint session_id) {
    jobject result_info = NULL;
    const txc_player_info_t *player_info = txc_get_player_info(session_id);
    if (player_info == NULL) {
        return result_info;
    }

    const txc_playlist_t *playlist_info = txc_get_medialist_info(player_info->playlist_id);

    if (playlist_info == NULL) {
        return result_info;
    }

    JNIEnv *penv = env;
    if (penv) {
        if (g_class_player_info) {
            result_info = penv->AllocObject(g_class_play_list_info);
            if (result_info) {
                jfieldID fld_playlist_id = penv->GetFieldID(g_class_play_list_info, "playlistId",
                                                            "I");
                jfieldID fld_type = penv->GetFieldID(g_class_play_list_info, "type", "I");
                jfieldID fld_count = penv->GetFieldID(g_class_play_list_info, "count", "I");
                jfieldID fld_hasMore = penv->GetFieldID(g_class_play_list_info, "hasMore", "Z");

                __android_log_print(ANDROID_LOG_DEBUG, "XWeiControlJNI_",
                                    "Java_com_tencent_xiaowei_control_XWeiMedia_txcGetPlaylistInfo, %d",
                                    playlist_info->hasMore);

                if (fld_playlist_id
                    && fld_type && fld_count && fld_hasMore) {
                    penv->SetIntField(result_info, fld_playlist_id, playlist_info->playlist_id);
                    penv->SetIntField(result_info, fld_type, playlist_info->type);
                    penv->SetIntField(result_info, fld_count, playlist_info->count);
                    penv->SetBooleanField(result_info, fld_hasMore,
                                          (jboolean) playlist_info->hasMore);
                }
            }
        }
    }

    return result_info;
}

JNIEXPORT jobject JNICALL
Java_com_tencent_xiaowei_control_XWeiMedia_txcGetMedia(JNIEnv *env, jclass service, jint playlist_id,
                                               jlong index) {
    jobject result_info = NULL;
    const txc_media_t *media_info = txc_get_media(playlist_id, index);
    if (media_info) {
        JNIEnv *penv = env;
        if (penv) {
            if (g_class_mediainfo) {
                result_info = penv->AllocObject(g_class_mediainfo);
                if (result_info) {
                    jfieldID fld_resId = penv->GetFieldID(g_class_mediainfo, "resId",
                                                          "Ljava/lang/String;");
                    jfieldID fld_content = penv->GetFieldID(g_class_mediainfo, "content",
                                                            "Ljava/lang/String;");
                    jfieldID fld_mediaType = penv->GetFieldID(g_class_mediainfo, "mediaType", "I");

                    jfieldID fld_description = penv->GetFieldID(g_class_mediainfo, "description",
                                                                "Ljava/lang/String;");

                    if (fld_description
                        && fld_resId && fld_mediaType && fld_content) {
                        jstring str_resId = env->NewStringUTF(media_info->res_id);
                        penv->SetObjectField(result_info, fld_resId, str_resId);
                        env->DeleteLocalRef(str_resId);

                        jstring str_content = env->NewStringUTF(media_info->content);
                        penv->SetObjectField(result_info, fld_content, str_content);
                        env->DeleteLocalRef(str_content);

                        penv->SetIntField(result_info, fld_mediaType, media_info->type);

                        jstring str_description = env->NewStringUTF(media_info->description);
                        penv->SetObjectField(result_info, fld_description, str_description);
                        env->DeleteLocalRef(str_description);
                    }
                }
            }
        }
    }

    return result_info;
}

#ifdef __cplusplus
}
#endif
