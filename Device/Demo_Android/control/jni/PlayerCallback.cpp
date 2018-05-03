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

#include "AudioApp.h"
#include "Player.h"

#include "PlayerCallback.h"
#include "ScopedJNIEnv.h"
#include "Playlist.h"

extern jobject g_xwei_control_jin_obj;

jobject s_obj_XWeiControl = NULL;
jclass s_class_XWeiControl = NULL;
jclass s_class_Response = NULL;
jclass s_class_SkillInfo = NULL;
jclass s_class_ContextInfo = NULL;
jclass s_class_Resource = NULL;
jclass s_class_ResGroup = NULL;
jclass CPlayerCallback::s_class_MediaInfo = NULL;
jclass CPlayerCallback::s_class_PlayState = NULL;
jclass s_class_MsgInfo = NULL;

bool CPlayerCallback::txc_event_processor(SESSION id, XWM_EVENT event, XWPARAM arg1, XWPARAM arg2) {
    bool handled = false;
    if (XWM_LIST_ADDED == event) {
        handled = OnXWMPlaylistAddItem(id, long(arg1), long(arg2));
    }

    return handled;
}

bool CPlayerCallback::OnXWMPlaylistAddItem(SESSION id, long begin_index, long count) {
    bool handled = false;

    const txc_player_info_t *player_info = txc_get_player_info(id);
    if (player_info) {

        const txc_playlist_t *playlist_info = txc_get_medialist_info(player_info->playlist_id);
        if (playlist_info
            && playlist_info->count > 0) {
        }
    }
    return handled;
}

XWPARAM
CPlayerCallback::txc_control_android_callback(SESSION id, TXC_PLAYER_ACTION action, XWPARAM arg1,
                                             XWPARAM arg2) {
    __android_log_print(ANDROID_LOG_INFO, "XWeiControlJNI_",
                        "txc_control_android_callback #, %d, %ld, %ld", action, long(arg1),
                        long(arg2));
    bool handled = false;
    switch (action) {
        case ACT_PLAYER_FINISH:
            handled = OnActFinish(id);
            break;
        //case ACT_PLAYER_STOP:
          //  handled = OnActStop(id);
            break;
        case ACT_PLAYER_PAUSE:
            OnActPause(id, bool(arg1));
            break;
        case ACT_CHANGE_VOLUME:
            OnActVolume(id, reinterpret_cast<long>(arg1));
            break;
        case ACT_PLAYER_SET_REPEAT_MODE:
            OnActSetRepeat(id, reinterpret_cast<long>(arg1));
            break;
        case ACT_ADD_ALBUM:
            handled = OnAddAlbum(id, reinterpret_cast<const txc_media_t *>(arg1),
                                 reinterpret_cast<long>(arg2));
            break;
        case ACT_PLAYLIST_ADD_ITEM:
            handled = OnPlaylistAddItem(id, reinterpret_cast<const txc_media_t **>(arg1),
                                        false, reinterpret_cast<long>(arg2));
            break;
        case ACT_PLAYLIST_ADD_ITEM_FRONT:
            handled = OnPlaylistAddItem(id, reinterpret_cast<const txc_media_t **>(arg1),
                                        true, reinterpret_cast<long>(arg2));
            break;
        case ACT_PLAYLIST_REMOVE_ITEM:
            handled = OnPlaylistRemoveItem(id, reinterpret_cast<const txc_media_t **>(arg1),
                                           reinterpret_cast<long>(arg2));
            break;
        case ACT_PLAYLIST_UPDATE_ITEM:
            handled = OnPlaylistUpdateItem(id, reinterpret_cast<const txc_media_t **>(arg1),
                                           reinterpret_cast<long>(arg2));
            break;
        case ACT_MUSIC_PUSH_MEDIA:
            handled = OnPushMedia(id, reinterpret_cast<const txc_media_t *>(arg1), (bool)arg2);
            break;
        case ACT_PROGRESS:
            handled = OnProgress(id, reinterpret_cast<const txc_progress_t *>(arg1 ));
            break;
        case ACT_NEED_SUPPLEMENT:
            handled = OnSupplement(id, reinterpret_cast<long>(arg1),
                                   reinterpret_cast<const TXCA_PARAM_RESPONSE *>(arg2));
            break;
        case ACT_NEED_TIPS:
            OnTips(id, reinterpret_cast<long>(arg1));
            break;
        case ACT_REPORT_PLAY_STATE: {
            TXCA_PARAM_STATE *state = reinterpret_cast<TXCA_PARAM_STATE *>(arg1);
            ReportPlayState(id, state);
            break;
        }
        case ACT_DOWNLOAD_MSG: {
            OnDownloadMsg(id, reinterpret_cast<txc_download_msg_data_t*>(arg1));
            break;
        }
        case ACT_AUDIOMSG_RECORD: {
            OnAudioMsgRecord(id);
            break;
        }
        case ACT_AUDIOMSG_SEND: {
            OnAudioMsgSend(id, *((unsigned long long*)arg1));
            break;
        }

        default:
            break;
    }
    return NULL;

}

bool CPlayerCallback::OnActStop(SESSION id) {
    bool handled = false;
    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "stopPlayer", "(I)Z");
        if (methodID) {
            handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

bool CPlayerCallback::OnActFinish(SESSION id) {
    bool handled = false;
    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "playFinish", "(I)Z");
        if (methodID) {
            handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

bool CPlayerCallback::OnActPause(SESSION id, bool pause) {
    bool handled = false;

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env && s_class_XWeiControl) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "pausePlayer", "(IZ)Z");
        if (methodID) {
            handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id, pause);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

bool CPlayerCallback::OnActVolume(SESSION id, int volume) {
    bool handled = false;

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env && s_class_XWeiControl) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "changeVolume", "(II)Z");
        if (methodID) {
            handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id, volume);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

bool CPlayerCallback::OnActSetRepeat(SESSION id, int repeatMode) {
    bool handled = false;

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env && s_class_XWeiControl) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onSetRepeatMode", "(II)Z");
        if (methodID) {
            handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id, repeatMode);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

bool CPlayerCallback::OnAddAlbum(SESSION id, const txc_media_t *album, long index) {
    bool handled = false;

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env && album != NULL) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onPlaylistAddAlbum",
                                              "(I[Lcom/tencent/xiaowei/control/info/XWeiMediaInfo;)Z");
        if (methodID) {
            if (s_class_MediaInfo) {
                jmethodID init = env->GetMethodID(s_class_MediaInfo, "<init>", "()V");
                jobjectArray objMediaInfoArray = env->NewObjectArray(1, s_class_MediaInfo, NULL);

                jobject objMediaInfo = env->NewObject(s_class_MediaInfo, init);

                jstring strResId = env->NewStringUTF(album->res_id);
                jstring strContent = env->NewStringUTF(album->content);
                jstring strDesc = env->NewStringUTF(album->description);

                jfieldID fieldID = env->GetFieldID(s_class_MediaInfo, "resId",
                                                   "Ljava/lang/String;");
                env->SetObjectField(objMediaInfo, fieldID, strResId);

                fieldID = env->GetFieldID(s_class_MediaInfo, "content", "Ljava/lang/String;");
                env->SetObjectField(objMediaInfo, fieldID, strContent);

                fieldID = env->GetFieldID(s_class_MediaInfo, "mediaType", "I");
                env->SetIntField(objMediaInfo, fieldID, album->type);

                fieldID = env->GetFieldID(s_class_MediaInfo, "description", "Ljava/lang/String;");
                env->SetObjectField(objMediaInfo, fieldID, strDesc);

                env->SetObjectArrayElement(objMediaInfoArray, 0, objMediaInfo);

                if (strResId)
                    env->DeleteLocalRef(strResId);
                if (strDesc)
                    env->DeleteLocalRef(strDesc);
                if (strContent) {
                    env->DeleteLocalRef(strContent);
                }

                env->DeleteLocalRef(objMediaInfo);

                handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id,
                                                 objMediaInfoArray);

                env->DeleteLocalRef(objMediaInfoArray);
            }
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

bool
CPlayerCallback::OnPlaylistAddItem(SESSION id, const txc_media_t **list, bool isFront, long count) {
    bool handled = false;

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env && list != NULL && count > 0) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onPlaylistAddItem",
                                              "(IZ[Lcom/tencent/xiaowei/control/info/XWeiMediaInfo;)Z");
        if (methodID) {
            if (s_class_MediaInfo) {
                jmethodID init = env->GetMethodID(s_class_MediaInfo, "<init>", "()V");
                jobjectArray objMediaInfoArray = env->NewObjectArray(count, s_class_MediaInfo,
                                                                     NULL);

                for (int i = 0; i < count; i++) {
                    const txc_media_t *media = list[i];
                    if (media) {
                        jobject objMediaInfo = env->NewObject(s_class_MediaInfo, init);

                        jstring strResId = env->NewStringUTF(media->res_id);
                        jstring strContent = env->NewStringUTF(media->content);
                        jstring strDesc = env->NewStringUTF(media->description);

                        jfieldID fieldID = env->GetFieldID(s_class_MediaInfo, "resId",
                                                           "Ljava/lang/String;");
                        env->SetObjectField(objMediaInfo, fieldID, strResId);

                        fieldID = env->GetFieldID(s_class_MediaInfo, "content",
                                                  "Ljava/lang/String;");
                        env->SetObjectField(objMediaInfo, fieldID, strContent);

                        fieldID = env->GetFieldID(s_class_MediaInfo, "mediaType", "I");
                        env->SetIntField(objMediaInfo, fieldID, media->type);

                        fieldID = env->GetFieldID(s_class_MediaInfo, "description",
                                                  "Ljava/lang/String;");
                        env->SetObjectField(objMediaInfo, fieldID, strDesc);

                        env->SetObjectArrayElement(objMediaInfoArray, i, objMediaInfo);

                        if (strResId)
                            env->DeleteLocalRef(strResId);
                        if (strDesc)
                            env->DeleteLocalRef(strDesc);
                        if (strContent) {
                            env->DeleteLocalRef(strContent);
                        }
                        env->DeleteLocalRef(objMediaInfo);

                    }
                }
                handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id, isFront,
                                                 objMediaInfoArray);

                env->DeleteLocalRef(objMediaInfoArray);
            }
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

bool CPlayerCallback::OnPlaylistUpdateItem(SESSION id, const txc_media_t **list, long count) {
    bool handled = false;

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env && list != NULL && count > 0) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onPlaylistUpdateItem",
                                              "(I[Lcom/tencent/xiaowei/control/info/XWeiMediaInfo;)Z");
        if (methodID) {
            if (s_class_MediaInfo) {
                jmethodID init = env->GetMethodID(s_class_MediaInfo, "<init>", "()V");
                jobjectArray objMediaInfoArray = env->NewObjectArray(count, s_class_MediaInfo,
                                                                     NULL);

                for (int i = 0; i < count; i++) {
                    const txc_media_t *media = list[i];
                    if (media) {
                        jobject objMediaInfo = env->NewObject(s_class_MediaInfo, init);

                        jstring strResId = env->NewStringUTF(media->res_id);
                        jstring strContent = env->NewStringUTF(media->content);
                        jstring strDesc = env->NewStringUTF(media->description);

                        jfieldID fieldID = env->GetFieldID(s_class_MediaInfo, "resId",
                                                           "Ljava/lang/String;");
                        env->SetObjectField(objMediaInfo, fieldID, strResId);

                        fieldID = env->GetFieldID(s_class_MediaInfo, "content",
                                                  "Ljava/lang/String;");
                        env->SetObjectField(objMediaInfo, fieldID, strContent);

                        fieldID = env->GetFieldID(s_class_MediaInfo, "mediaType", "I");
                        env->SetIntField(objMediaInfo, fieldID, media->type);

                        fieldID = env->GetFieldID(s_class_MediaInfo, "description",
                                                  "Ljava/lang/String;");
                        env->SetObjectField(objMediaInfo, fieldID, strDesc);

                        env->SetObjectArrayElement(objMediaInfoArray, i, objMediaInfo);

                        if (strResId)
                            env->DeleteLocalRef(strResId);
                        if (strDesc)
                            env->DeleteLocalRef(strDesc);
                        if (strContent) {
                            env->DeleteLocalRef(strContent);
                        }
                        env->DeleteLocalRef(objMediaInfo);

                    }
                }
                handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id,
                                                 objMediaInfoArray);

                env->DeleteLocalRef(objMediaInfoArray);
            }
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

bool CPlayerCallback::OnPlaylistRemoveItem(SESSION id, const txc_media_t **list, long count) {
    bool handled = false;

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env && list != NULL && count > 0) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onPlaylistRemoveItem",
                                              "(I[Lcom/tencent/xiaowei/control/info/XWeiMediaInfo;)Z");
        if (methodID) {
            if (s_class_MediaInfo) {
                jmethodID init = env->GetMethodID(s_class_MediaInfo, "<init>", "()V");
                jobjectArray objMediaInfoArray = env->NewObjectArray(count, s_class_MediaInfo,
                                                                     NULL);

                for (int i = 0; i < count; i++) {
                    const txc_media_t *media = list[i];
                    if (media) {
                        jobject objMediaInfo = env->NewObject(s_class_MediaInfo, init);

                        jstring strResId = env->NewStringUTF(media->res_id);
                        jstring strContent = env->NewStringUTF(media->content);
                        jstring strDesc = env->NewStringUTF(media->description);

                        jfieldID fieldID = env->GetFieldID(s_class_MediaInfo, "resId",
                                                           "Ljava/lang/String;");
                        env->SetObjectField(objMediaInfo, fieldID, strResId);

                        fieldID = env->GetFieldID(s_class_MediaInfo, "content",
                                                  "Ljava/lang/String;");
                        env->SetObjectField(objMediaInfo, fieldID, strContent);

                        fieldID = env->GetFieldID(s_class_MediaInfo, "mediaType", "I");
                        env->SetIntField(objMediaInfo, fieldID, media->type);

                        fieldID = env->GetFieldID(s_class_MediaInfo, "description",
                                                  "Ljava/lang/String;");
                        env->SetObjectField(objMediaInfo, fieldID, strDesc);

                        env->SetObjectArrayElement(objMediaInfoArray, i, objMediaInfo);

                        if (strResId)
                            env->DeleteLocalRef(strResId);
                        if (strDesc)
                            env->DeleteLocalRef(strDesc);
                        if (strContent) {
                            env->DeleteLocalRef(strContent);
                        }
                        env->DeleteLocalRef(objMediaInfo);

                    }
                }
                handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id,
                                                 objMediaInfoArray);

                env->DeleteLocalRef(objMediaInfoArray);
            }
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

bool CPlayerCallback::OnPushMedia(SESSION id, const txc_media_t *media, bool need_release) {
    bool handled = false;

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env && media != NULL) {

        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onPushMedia",
                                              "(ILcom/tencent/xiaowei/control/info/XWeiMediaInfo;Z)Z");

        if (methodID) {
            if (s_class_MediaInfo) {

                jmethodID init = env->GetMethodID(s_class_MediaInfo, "<init>", "()V");
                jobject objMediaInfo = env->NewObject(s_class_MediaInfo, init);

                jstring strResId = env->NewStringUTF(media->res_id);
                jstring strContent = env->NewStringUTF(media->content);
                jstring strDesc = env->NewStringUTF(media->description);

                jfieldID fieldID = env->GetFieldID(s_class_MediaInfo, "resId",
                                                   "Ljava/lang/String;");
                env->SetObjectField(objMediaInfo, fieldID, strResId);

                fieldID = env->GetFieldID(s_class_MediaInfo, "content", "Ljava/lang/String;");
                env->SetObjectField(objMediaInfo, fieldID, strContent);

                fieldID = env->GetFieldID(s_class_MediaInfo, "mediaType", "I");
                env->SetIntField(objMediaInfo, fieldID, media->type);

                fieldID = env->GetFieldID(s_class_MediaInfo, "offset", "I");
                env->SetIntField(objMediaInfo, fieldID, media->offset);

                fieldID = env->GetFieldID(s_class_MediaInfo, "description", "Ljava/lang/String;");
                env->SetObjectField(objMediaInfo, fieldID, strDesc);

                handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id, objMediaInfo, need_release);

                env->DeleteLocalRef(strResId);
                env->DeleteLocalRef(strDesc);
                env->DeleteLocalRef(strContent);
                env->DeleteLocalRef(objMediaInfo);
            }
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

bool CPlayerCallback::OnProgress(SESSION id, const txc_progress_t *progress) {
    bool handled = false;


    return handled;
}

bool
CPlayerCallback::OnSupplement(SESSION id, long wait_time, const TXCA_PARAM_RESPONSE *response) {
    bool handled = false;

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onSupplement",
                                              "(ILjava/lang/String;IIJ)Z");
        if (methodID) {
            jstring jstr_context_id = env->NewStringUTF(response->context.id);
            handled = env->CallBooleanMethod(s_obj_XWeiControl, methodID, id, jstr_context_id,
                                             response->context.speak_timeout,
                                             response->context.silent_timeout,
                                             response->context.request_param);
            env->DeleteLocalRef(jstr_context_id);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return handled;
}

void CPlayerCallback::OnTips(SESSION id, int tipsType) {

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onTips", "(II)V");
        if (methodID) {
            env->CallVoidMethod(s_obj_XWeiControl, methodID, id, (jint) tipsType);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
}

long long CPlayerCallback::ReportPlayState(SESSION id, TXCA_PARAM_STATE *state) {

    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onNeedReportPlayState",
                                              "(ILcom/tencent/xiaowei/control/info/XWeiPlayState;)V");
        if (methodID) {
            jmethodID init = env->GetMethodID(s_class_PlayState, "<init>", "()V");
            jobject obj = env->NewObject(s_class_PlayState, init);

            jstring strResId = env->NewStringUTF(state->play_id);
            jstring strContent = env->NewStringUTF(state->play_content);
            jstring strSkillId = env->NewStringUTF(state->skill_info.id);
            jstring strSkillName = env->NewStringUTF(state->skill_info.name);

            jfieldID fieldID = env->GetFieldID(s_class_PlayState, "resId",
                                               "Ljava/lang/String;");
            env->SetObjectField(obj, fieldID, strResId);

            fieldID = env->GetFieldID(s_class_PlayState, "content", "Ljava/lang/String;");
            env->SetObjectField(obj, fieldID, strContent);

            fieldID = env->GetFieldID(s_class_PlayState, "playState", "I");
            env->SetIntField(obj, fieldID, state->play_state);

            fieldID = env->GetFieldID(s_class_PlayState, "playMode", "I");
            env->SetIntField(obj, fieldID, state->play_mode);

            fieldID = env->GetFieldID(s_class_PlayState, "skillId", "Ljava/lang/String;");
            env->SetObjectField(obj, fieldID, strSkillId);

            fieldID = env->GetFieldID(s_class_PlayState, "skillName", "Ljava/lang/String;");
            env->SetObjectField(obj, fieldID, strSkillName);

            env->CallVoidMethod(s_obj_XWeiControl, methodID, id, obj);

            env->DeleteLocalRef(strResId);
            env->DeleteLocalRef(strContent);
            env->DeleteLocalRef(strSkillId);
            env->DeleteLocalRef(strSkillName);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
    return 0;
}

void CPlayerCallback::OnDownloadMsg(SESSION id, const txc_download_msg_data_t* data)
{
    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env && data != NULL) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onDownloadMsgFile",
                                              "(IJIILjava/lang/String;Ljava/lang/String;II)V");

        __android_log_print(ANDROID_LOG_ERROR, "CPlayerCallback", "OnDownloadMsg id:%d tinyId:%llu channel:%d type:%d key1:%s mini_token:%s duration:%d timestamp:%d",
            id, data->tinyId, data->channel, data->type, data->key, data->mini_token, data->duration, data->timestamp);
        if (methodID) {
            jstring jstr_key1 = env->NewStringUTF(data->key);
            jstring jstr_key2 = NULL;
            if (data->mini_token)
                jstr_key2 = env->NewStringUTF(data->mini_token);

            env->CallVoidMethod(s_obj_XWeiControl, methodID, id, (jlong)data->tinyId, data->channel,
                data->type, jstr_key1, jstr_key2, data->duration, data->timestamp);

            env->DeleteLocalRef(jstr_key1);
            if (jstr_key2 != NULL)
                env->DeleteLocalRef(jstr_key2);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
}

void CPlayerCallback::OnAudioMsgRecord(SESSION id)
{
    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env) {
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onAudioMsgRecord", "(I)V");
        if (methodID) {
            env->CallVoidMethod(s_obj_XWeiControl, methodID, id);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
}

void CPlayerCallback::OnAudioMsgSend(SESSION id, unsigned long long tinyId)
{
    bool needRelease = false;
    JNIEnv *env = CGlobalJNIEnv::GetJNIEnv(&needRelease);
    if (env) {
        __android_log_print(ANDROID_LOG_ERROR, "CPlayerCallback", "OnAudioMsgSend tinyId:%llu", tinyId);
        jmethodID methodID = env->GetMethodID(s_class_XWeiControl, "onAudioMsgSend", "(IJ)V");
        if (methodID){
            env->CallVoidMethod(s_obj_XWeiControl, methodID, id, (jlong)tinyId);
        }
    }
    if (needRelease) {
        CGlobalJNIEnv::Util_ReleaseEnv();
    }
}


void CPlayerCallback::InitClasses(JNIEnv *env, jclass service) {
    s_obj_XWeiControl = env->NewGlobalRef(service);

    jclass cls_XWeiControl = env->GetObjectClass(service);
    s_class_XWeiControl = (jclass) env->NewGlobalRef(cls_XWeiControl);

    jclass cls_Response = env->FindClass("com/tencent/xiaowei/info/XWResponseInfo");
    s_class_Response = (jclass) env->NewGlobalRef(cls_Response);

    jclass cls_SkillInfo = env->FindClass("com/tencent/xiaowei/info/XWAppInfo");
    s_class_SkillInfo = (jclass) env->NewGlobalRef(cls_SkillInfo);

    jclass cls_ContextInfo = env->FindClass("com/tencent/xiaowei/info/XWContextInfo");
    s_class_ContextInfo = (jclass) env->NewGlobalRef(cls_ContextInfo);

    jclass cls_Resource = env->FindClass("com/tencent/xiaowei/info/XWResourceInfo");
    s_class_Resource = (jclass) env->NewGlobalRef(cls_Resource);

    jclass cls_ResGroup = env->FindClass("com/tencent/xiaowei/info/XWResGroupInfo");
    s_class_ResGroup = (jclass) env->NewGlobalRef(cls_ResGroup);

    jclass cls_MediaInfo = env->FindClass("com/tencent/xiaowei/control/info/XWeiMediaInfo");
    s_class_MediaInfo = (jclass) env->NewGlobalRef(cls_MediaInfo);

    jclass cls_PlayState = env->FindClass("com/tencent/xiaowei/control/info/XWeiPlayState");
    s_class_PlayState = (jclass) env->NewGlobalRef(cls_PlayState);

    jclass cls_MsgInfo = env->FindClass("com/tencent/xiaowei/control/info/XWeiMsgInfo");
    s_class_MsgInfo = (jclass) env->NewGlobalRef(cls_MsgInfo);
}
