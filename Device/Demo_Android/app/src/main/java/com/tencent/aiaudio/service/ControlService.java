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
package com.tencent.aiaudio.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.tencent.aiaudio.CommonApplication;
import com.tencent.aiaudio.activity.ActivityManager;
import com.tencent.aiaudio.activity.FMActivity;
import com.tencent.aiaudio.activity.MusicActivity;
import com.tencent.aiaudio.activity.NewsActivity;
import com.tencent.aiaudio.activity.OtherActivity;
import com.tencent.aiaudio.activity.WeatherActivity;
import com.tencent.aiaudio.alarm.AlarmSkillHandler;
import com.tencent.aiaudio.alarm.DeviceSkillAlarmManager;
import com.tencent.aiaudio.demo.IControlService;
import com.tencent.aiaudio.player.XWeiPlayerMgr;
import com.tencent.aiaudio.tts.TTSManager;
import com.tencent.utils.MusicPlayer;
import com.tencent.utils.ThreadManager;
import com.tencent.xiaowei.control.Constants;
import com.tencent.xiaowei.control.IXWeiPlayer;
import com.tencent.xiaowei.control.XWeiAudioFocusManager;
import com.tencent.xiaowei.control.XWeiControl;
import com.tencent.xiaowei.control.info.XWeiMediaInfo;
import com.tencent.xiaowei.control.info.XWeiPlayerInfo;
import com.tencent.xiaowei.control.info.XWeiPlaylistInfo;
import com.tencent.xiaowei.control.info.XWeiSessionInfo;
import com.tencent.xiaowei.info.MediaMetaInfo;
import com.tencent.xiaowei.info.XWAppInfo;
import com.tencent.xiaowei.info.XWContextInfo;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_ALARM;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_FM;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_MUSIC;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_New;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_QQ_CALL;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_TRIGGER_ALARM;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_Unknown;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_WEATHER;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_WIKI_AI_LAB;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_WIKI_Calculator;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_WIKI_HISTORY;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_WIKI_Time;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_WX_Chat;
import static com.tencent.xiaowei.control.XWMediaType.TYPE_MUSIC_URL;

/**
 * 用于启动各个Skill场景
 */
public class ControlService extends Service implements XWeiPlayerMgr.SkillUIEventListener {
    public static final String EXTRA_KEY_START_SKILL_DATA = "extra_key_start_skill_data";
    public static final String EXTRA_KEY_START_SKILL_SESSION_ID = "extra_key_start_skill_session_id";
    public static final String EXTRA_KEY_START_SKILL_NAME = "extra_key_start_skill_app_name";
    public static final String EXTRA_KEY_START_SKILL_ID = "extra_key_start_skill_app_id";
    public static final String EXTRA_KEY_START_SKILL_ANSWER = "extra_key_start_skill_answer";
    public static final String EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID = "extra_key_music_on_event_session_id";
    public static final String EXTRA_KEY_MUSIC_ON_EVENT_PLAY_ID = "extra_key_music_on_event_play_id";
    public static final String EXTRA_KEY_MUSIC_ON_UPDATE_ITEM_DETAIL = "extra_key_music_on_update_item_detail";
    public static final String ACTION_MUSIC_ON_PLAY = "action_music_on_play";
    public static final String ACTION_MUSIC_ON_PAUSE = "action_music_on_pause";
    public static final String ACTION_MUSIC_ON_RESUME = "action_music_on_resume";
    public static final String ACTION_MUSIC_ON_REPEAT_MODE = "action_music_on_repeat_mode";
    public static final String ACTION_MUSIC_ON_STOP = "action_music_on_stop";
    public static final String ACTION_MUSIC_ON_UPDATE_PLAY_LIST = "action_music_on_update_play_list";
    public static final String ACTION_MUSIC_ON_UNKEEP = "action_music_on_unkeep";
    public static final String ACTION_MUSIC_ON_KEEP = "action_music_on_keep";
    private static final String TAG = ControlService.class.getSimpleName();
    private SparseArray<String> session2CurPlayId = new SparseArray<>();
    private long lastUpdateTime = 0;
    private SparseArray<ArrayList<String>> session2PlayIdArray = new SparseArray<>();
    private HashMap<String, MediaMetaInfo> id2PlayInfo = new HashMap<>();
    private volatile boolean isLoadingMore;
    private Handler mHandler = new Handler();

    private XWeiAudioFocusManager.OnAudioFocusChangeListener listener;
    private AlarmSkillHandler alarmSkillHandler; // 处理AlarmSkillHandler

    @Override
    public void onCreate() {
        super.onCreate();

        XWeiPlayerMgr.setPlayerEventListener(this);
        alarmSkillHandler = new AlarmSkillHandler(getApplicationContext());
        XWeiControl.getInstance().getXWeiOuterSkill().registerSkillIdOrSkillName(SKILL_ID_ALARM, alarmSkillHandler);
        XWeiControl.getInstance().getXWeiOuterSkill().registerSkillIdOrSkillName(SKILL_ID_TRIGGER_ALARM, alarmSkillHandler);

        DeviceSkillAlarmManager.instance().init(getApplication());
        DeviceSkillAlarmManager.instance().startDeviceAllAlarm();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new IControlServiceImpl();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        XWeiPlayerMgr.setPlayerEventListener(null);
        alarmSkillHandler = null;
        XWeiControl.getInstance().getXWeiOuterSkill().unRegisterSkillIdOrSkillName(SKILL_ID_ALARM);
        XWeiControl.getInstance().getXWeiOuterSkill().unRegisterSkillIdOrSkillName(SKILL_ID_TRIGGER_ALARM);
        XWeiControl.getInstance().getXWeiOuterSkill().unRegisterSkillIdOrSkillName(SKILL_ID_QQ_CALL);
    }

    @Override
    public void onPlaylistAddAlbum(int sessionId, XWeiMediaInfo mediaInfo) {
        XWeiPlaylistInfo playlistInfo = XWeiControl.getInstance().getMediaTool().txcGetPlaylistInfo(sessionId);
        Log.d(TAG, "onPlaylistAddAlbum: " + playlistInfo.count);
        startSkillUI(sessionId, mediaInfo);
    }

    @Override
    public void onPlaylistAddItem(int sessionId, boolean isFront, XWeiMediaInfo[] mediaInfoArray) {
        addPlayListForSkill(sessionId, isFront, mediaInfoArray);
    }

    @Override
    public void onPlaylistUpdateItem(int sessionId, XWeiMediaInfo[] mediaInfoArray) {
        updatePlayListForSkill(sessionId, mediaInfoArray);
    }

    @Override
    public void onPlayListRemoveItem(int sessionId, XWeiMediaInfo[] mediaInfoArray) {
        removePlayListForSkill(sessionId, mediaInfoArray);
    }

    @Override
    public void onPlay(int sessionId, XWeiMediaInfo mediaInfo, boolean fromUser) {

        Log.d(TAG, "onPlay resId: " + mediaInfo.resId);
        XWeiSessionInfo sessionInfo = XWeiControl.getInstance().getAppTool().txcGetSession(sessionId);
        if (sessionInfo == null || (TextUtils.isEmpty(sessionInfo.skillName) || TextUtils.isEmpty(sessionInfo.skillId))) {
            return;
        }

        if (sessionInfo.skillId.equals(SKILL_ID_MUSIC) || sessionInfo.skillId.equals(SKILL_ID_FM) || sessionInfo.skillName.contains("skills")) {

            if (mediaInfo.mediaType == TYPE_MUSIC_URL) {
                MediaMetaInfo currentPlayInfo = JsonUtil.getObject(mediaInfo.description, MediaMetaInfo.class);


                if (currentPlayInfo != null) {
                    session2CurPlayId.put(sessionId, currentPlayInfo.playId);
                    Bundle bundle = new Bundle();
                    bundle.putInt(EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID, sessionId);
                    sendBroadcast(ACTION_MUSIC_ON_PLAY, bundle);

                    getDetailInfoIfNeed(sessionInfo.skillName, sessionInfo.skillId, id2PlayInfo.get(currentPlayInfo.playId));

                    refreshPlayListIfNeed(sessionId, false);

                    // 预加载播放资源
                    ArrayList<String> playIdArray = session2PlayIdArray.get(sessionId);
                    int index = playIdArray == null ? -1 : playIdArray.indexOf(currentPlayInfo.playId);
                    if (index != -1 && index + 1 >= playIdArray.size()) {
                        loadMorePlayList(sessionId);
                    }
                }
            }
        } else if (sessionInfo.skillId.equals(SKILL_ID_New)) {
            if (mediaInfo.mediaType == TYPE_MUSIC_URL) {
                MediaMetaInfo currentPlayInfo = JsonUtil.getObject(mediaInfo.description, MediaMetaInfo.class);


                if (currentPlayInfo != null) {
                    session2CurPlayId.put(sessionId, currentPlayInfo.playId);
                    Bundle bundle = new Bundle();
                    bundle.putInt(EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID, sessionId);
                    sendBroadcast(ACTION_MUSIC_ON_PLAY, bundle);
                }
            }
        }
    }

    @Override
    public void onPause(int sessionId) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID, sessionId);
        sendBroadcast(ACTION_MUSIC_ON_PAUSE, bundle);
    }

    @Override
    public void onResume(int sessionId) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID, sessionId);
        sendBroadcast(ACTION_MUSIC_ON_RESUME, bundle);
    }

    @Override
    public void onSetPlayMode(int sessionId, int repeatMode) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID, sessionId);
        sendBroadcast(ACTION_MUSIC_ON_REPEAT_MODE, bundle);
    }

    @Override
    public void onFinish(int sessionId) {
        ActivityManager.getInstance().finish(sessionId);
        TTSManager.getInstance().release(sessionId);
    }

    @Override
    public void onFavoriteEvent(String event, String playId) {
        boolean isKeep = event.equals("收藏");
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_KEY_MUSIC_ON_EVENT_PLAY_ID, playId);
        sendBroadcast(isKeep ? ACTION_MUSIC_ON_KEEP : ACTION_MUSIC_ON_UNKEEP, bundle);

        MediaMetaInfo mediaMetaInfo = id2PlayInfo.get(playId);
        if (mediaMetaInfo != null) {
            mediaMetaInfo.favorite = isKeep;
        }
    }

    @Override
    public void onTips(int tipsType) {
        switch (tipsType) {
            case Constants.TXPlayerTipsType.PLAYER_TIPS_NEXT_FAILURE:
                XWSDK.getInstance().requestTTS("当前列表没有更多了，您可以重新点播".getBytes(), new XWContextInfo(), new XWSDK.RequestListener() {
                    @Override
                    public boolean onRequest(int event, final XWResponseInfo rspData, byte[] extendData) {
                        listener = new XWeiAudioFocusManager.OnAudioFocusChangeListener() {
                            @Override
                            public void onAudioFocusChange(int focusChange) {
                                if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
                                    MusicPlayer.getInstance().playMediaInfo(rspData.resources[0].resources[0], new MusicPlayer.OnPlayListener() {
                                        @Override
                                        public void onCompletion(int error) {
                                            XWeiAudioFocusManager.getInstance().abandonAudioFocus(listener);
                                        }

                                    });
                                } else {
                                    MusicPlayer.getInstance().stop();
                                }

                            }
                        };
                        XWeiAudioFocusManager.getInstance().requestAudioFocus(listener, XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
                        return true;
                    }
                });
                break;
            case Constants.TXPlayerTipsType.PLAYER_TIPS_PREV_FAILURE:
                XWSDK.getInstance().requestTTS("当前列表没有上一首了".getBytes(), new XWContextInfo(), new XWSDK.RequestListener() {
                    @Override
                    public boolean onRequest(int event, final XWResponseInfo rspData, byte[] extendData) {
                        listener = new XWeiAudioFocusManager.OnAudioFocusChangeListener() {
                            @Override
                            public void onAudioFocusChange(int focusChange) {
                                if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK) {
                                    MusicPlayer.getInstance().playMediaInfo(rspData.resources[0].resources[0], new MusicPlayer.OnPlayListener() {
                                        @Override
                                        public void onCompletion(int error) {
                                            XWeiAudioFocusManager.getInstance().abandonAudioFocus(listener);
                                        }

                                    });
                                } else {
                                    MusicPlayer.getInstance().stop();
                                }

                            }
                        };
                        XWeiAudioFocusManager.getInstance().requestAudioFocus(listener, XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
                        return true;
                    }
                });
                break;
        }
    }

    @Override
    public void onAutoWakeup(int sessionId, String contextId, int speakTimeout, int silentTimeout, long requestParam) {
        AIAudioService service = AIAudioService.getInstance();
        if (service != null) {
            service.wakeup(contextId, speakTimeout, silentTimeout, requestParam);
        }
    }

    /**
     * 根据响应结果启动通用SKill场景界面，例如天气，新闻，百科
     *
     * @param sessionId 与场景关联的sessionId
     * @param mediaInfo 封面媒体信息
     */
    private void startSkillUI(int sessionId, XWeiMediaInfo mediaInfo) {
        XWeiSessionInfo sessionInfo = XWeiControl.getInstance().getAppTool().txcGetSession(sessionId);
        if (sessionInfo == null || (TextUtils.isEmpty(sessionInfo.skillName) || TextUtils.isEmpty(sessionInfo.skillId))) {
            return;
        }
        // 第三方skill Test
        if(sessionInfo.skillName.contains("skills")){
            Intent intent = new Intent(getApplicationContext(), MusicActivity.class);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EXTRA_KEY_START_SKILL_SESSION_ID, sessionId);
            intent.putExtra(EXTRA_KEY_START_SKILL_DATA, mediaInfo.description);
            intent.putExtra(EXTRA_KEY_START_SKILL_NAME, sessionInfo.skillName);
            intent.putExtra(EXTRA_KEY_START_SKILL_ID, sessionInfo.skillId);
            startActivity(intent);
            return;
        }

        switch (sessionInfo.skillId) {
            case SKILL_ID_WEATHER: {
                Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EXTRA_KEY_START_SKILL_DATA, mediaInfo.description);
                intent.putExtra(EXTRA_KEY_START_SKILL_SESSION_ID, sessionId);
                startActivity(intent);
                break;
            }
            case SKILL_ID_MUSIC: {
                lastUpdateTime = System.currentTimeMillis();
                Intent intent = new Intent(getApplicationContext(), MusicActivity.class);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EXTRA_KEY_START_SKILL_SESSION_ID, sessionId);
                intent.putExtra(EXTRA_KEY_START_SKILL_DATA, mediaInfo.description);
                intent.putExtra(EXTRA_KEY_START_SKILL_NAME, sessionInfo.skillName);
                intent.putExtra(EXTRA_KEY_START_SKILL_ID, sessionInfo.skillId);
                startActivity(intent);
                getHistoryList(sessionId);
                break;
            }
            case SKILL_ID_FM: {
                Intent intent = new Intent(getApplicationContext(), FMActivity.class);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EXTRA_KEY_START_SKILL_SESSION_ID, sessionId);
                intent.putExtra(EXTRA_KEY_START_SKILL_DATA, mediaInfo.description);
                intent.putExtra(EXTRA_KEY_START_SKILL_NAME, sessionInfo.skillName);
                intent.putExtra(EXTRA_KEY_START_SKILL_ID, sessionInfo.skillId);
                startActivity(intent);
                break;
            }
            case SKILL_ID_New: {
                XWeiPlaylistInfo playlistInfo = XWeiControl.getInstance().getMediaTool().txcGetPlaylistInfo(sessionId);

                if (playlistInfo.count <= 1) {
                    Intent intent = new Intent(getApplicationContext(), OtherActivity.class);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(EXTRA_KEY_START_SKILL_SESSION_ID, sessionId);
                    intent.putExtra(EXTRA_KEY_START_SKILL_NAME, sessionInfo.skillName);
                    intent.putExtra(EXTRA_KEY_START_SKILL_ANSWER, mediaInfo.content);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), NewsActivity.class);
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(EXTRA_KEY_START_SKILL_SESSION_ID, sessionId);
                    intent.putExtra(EXTRA_KEY_START_SKILL_NAME, sessionInfo.skillName);
                    intent.putExtra(EXTRA_KEY_START_SKILL_ID, sessionInfo.skillId);
                    startActivity(intent);
                }
                break;
            }
            case SKILL_ID_WIKI_HISTORY:
            case SKILL_ID_WIKI_AI_LAB:
            case SKILL_ID_WIKI_Time:
            case SKILL_ID_WX_Chat:
            case SKILL_ID_Unknown:
            case SKILL_ID_WIKI_Calculator: {
                Intent intent = new Intent(getApplicationContext(), OtherActivity.class);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EXTRA_KEY_START_SKILL_SESSION_ID, sessionId);
                intent.putExtra(EXTRA_KEY_START_SKILL_NAME, sessionInfo.skillName);
                intent.putExtra(EXTRA_KEY_START_SKILL_ID, sessionInfo.skillId);
                intent.putExtra(EXTRA_KEY_START_SKILL_ANSWER, mediaInfo.content);
                startActivity(intent);
                break;
            }
            default:
                if (!TextUtils.isEmpty(mediaInfo.content))
                    CommonApplication.showToast(mediaInfo.content);
                break;
        }
    }

    /**
     * 添加播放资源
     *
     * @param sessionId      场景sessionId
     * @param mediaInfoArray 播放资源
     */
    private void addPlayListForSkill(int sessionId, boolean isFront, XWeiMediaInfo[] mediaInfoArray) {
        ArrayList<String> playIdArray = session2PlayIdArray.get(sessionId);
        if (playIdArray == null) {
            playIdArray = new ArrayList<>();
        }

        Log.d(TAG, "addPlayListForSkill mediaInfoArray.length :" + mediaInfoArray.length);
        int i = 0;
        for (XWeiMediaInfo info : mediaInfoArray) {
            MediaMetaInfo item = JsonUtil.getObject(info.description, MediaMetaInfo.class);
            if (item != null) {
                if (isFront) {
                    playIdArray.add(i, item.playId);
                } else {
                    playIdArray.add(item.playId);
                }
                id2PlayInfo.put(item.playId, item);
                i++;
            }
        }

        session2PlayIdArray.put(sessionId, playIdArray);

        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_KEY_START_SKILL_SESSION_ID, sessionId);
        sendBroadcast(ACTION_MUSIC_ON_UPDATE_PLAY_LIST, bundle);
    }

    /**
     * 更新列表资源项
     *
     * @param sessionId      场景sessionId
     * @param mediaInfoArray 播放资源
     */
    private void updatePlayListForSkill(int sessionId, XWeiMediaInfo[] mediaInfoArray) {
        ArrayList<String> playIdArray = session2PlayIdArray.get(sessionId);
        if (playIdArray == null) {
            playIdArray = new ArrayList<>();
        }

        Log.d(TAG, "updatePlayListForSkill mediaInfoArray.length :" + mediaInfoArray.length);
        for (XWeiMediaInfo info : mediaInfoArray) {
            try {
                MediaMetaInfo item = JsonUtil.getObject(info.description, MediaMetaInfo.class);
                if (item != null) {
                    id2PlayInfo.put(item.playId, item);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        session2PlayIdArray.put(sessionId, playIdArray);

        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_KEY_START_SKILL_SESSION_ID, sessionId);
        sendBroadcast(ACTION_MUSIC_ON_UPDATE_PLAY_LIST, bundle);
    }

    /**
     * 删减播放资源
     *
     * @param sessionId      场景sessionId
     * @param mediaInfoArray 播放资源
     */
    private void removePlayListForSkill(int sessionId, XWeiMediaInfo[] mediaInfoArray) {
        ArrayList<String> playIdArray = session2PlayIdArray.get(sessionId);
        if (playIdArray == null) {
            return;
        }

        ArrayList<String> removePlayIdArray = new ArrayList<>();
        for (XWeiMediaInfo info : mediaInfoArray) {
            MediaMetaInfo item = JsonUtil.getObject(info.description, MediaMetaInfo.class);
            if (item != null) {
                removePlayIdArray.add(item.playId);
                id2PlayInfo.remove(item.playId);
            }
        }

        playIdArray.removeAll(removePlayIdArray);
        session2PlayIdArray.put(sessionId, playIdArray);

        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_KEY_START_SKILL_SESSION_ID, sessionId);
        sendBroadcast(ACTION_MUSIC_ON_UPDATE_PLAY_LIST, bundle);
    }

    private void sendBroadcast(String action, Bundle extra) {
        Intent intent = new Intent(action);
        if (extra != null)
            intent.putExtras(extra);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * 拉取播放资源的详情信息
     *
     * @param skillName     场景名
     * @param skillId       场景Id
     * @param mediaMetaInfo 播放资源
     */
    private void getDetailInfoIfNeed(final String skillName, final String skillId, final MediaMetaInfo mediaMetaInfo) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                if (mediaMetaInfo != null && (TextUtils.isEmpty(mediaMetaInfo.lyric) || mediaMetaInfo.duration == 0)) {
                    XWAppInfo appInfo = new XWAppInfo();
                    appInfo.name = skillName;
                    appInfo.ID = skillId;
                    XWSDK.getInstance().getPlayDetailInfo(appInfo, new String[]{mediaMetaInfo.playId}, new XWSDK.RequestListener() {
                        @Override
                        public boolean onRequest(int event, XWResponseInfo rspData, byte[] extendData) {
                            // 不处理
                            return XWeiControl.getInstance().processResponse(rspData.voiceID, rspData, extendData);
                        }
                    });
                }
            }
        };

        ThreadManager.getInstance().start(runnable);
    }

    /**
     * url过期则刷新列表
     *
     * @param sessionId 场景id
     */
    private void refreshPlayListIfNeed(final int sessionId, boolean isForce) {
        final XWeiSessionInfo sessionInfo = XWeiControl.getInstance().getAppTool().txcGetSession(sessionId);

        if (!isForce && (System.currentTimeMillis() - lastUpdateTime) < 24 * 3600 * 1000
                && sessionInfo.skillId.equals(SKILL_ID_MUSIC)) {
            return;
        }


        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                XWAppInfo appInfo = new XWAppInfo();
                appInfo.name = sessionInfo.skillName;
                appInfo.ID = sessionInfo.skillId;
                ArrayList<String> playIdArray = session2PlayIdArray.get(sessionId);
                String[] data = new String[playIdArray.size()];
                playIdArray.toArray(data);

                XWSDK.getInstance().refreshPlayList(appInfo, data, new XWSDK.RequestListener() {
                    @Override
                    public boolean onRequest(int event, XWResponseInfo rspData, byte[] extendData) {
                        lastUpdateTime = System.currentTimeMillis();
                        return XWeiControl.getInstance().processResponse(rspData.voiceID, rspData, extendData);
                    }
                });
            }
        });
    }


    /**
     * 根据sessionId预加载播放资源
     *
     * @param sessionId 场景sessionId
     */
    private void loadMorePlayList(int sessionId) {
        ArrayList<String> playIdList = session2PlayIdArray.get(sessionId);
        if (playIdList == null) {
            return;
        }

        XWeiPlaylistInfo playlistInfo = XWeiControl.getInstance().getMediaTool().txcGetPlaylistInfo(sessionId);
        XWeiSessionInfo sessionInfo = XWeiControl.getInstance().getAppTool().txcGetSession(sessionId);

        Log.d(TAG, "hasMore: " + playlistInfo.hasMore + " isLoadingMore: " + isLoadingMore);
        if (playlistInfo.hasMore && !isLoadingMore) {
            isLoadingMore = true;

            XWAppInfo appInfo = new XWAppInfo();
            appInfo.name = sessionInfo.skillName;
            appInfo.ID = sessionInfo.skillId;

            XWSDK.getInstance().getMorePlaylist(appInfo, playIdList.get(playIdList.size() - 1), 6, false, new XWSDK.RequestListener() {
                @Override
                public boolean onRequest(int event, XWResponseInfo rspData, byte[] extendData) {
                    // 让控制层处理具体的数据
                    isLoadingMore = false;
                    return XWeiControl.getInstance().processResponse(rspData.voiceID, rspData, extendData);
                }
            });
        }
    }

    /**
     * 可以获取到历史播放过的歌单，后台会存储一部分数据
     *
     * @param sessionId
     */
    private void getHistoryList(final int sessionId) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                XWeiSessionInfo sessionInfo = XWeiControl.getInstance().getAppTool().txcGetSession(sessionId);
                ArrayList<String> playIdList = session2PlayIdArray.get(sessionId);
                if (playIdList == null) {
                    mHandler.postDelayed(this, 500);
                    return;
                }

                XWAppInfo appInfo = new XWAppInfo();
                appInfo.name = sessionInfo.skillName;
                appInfo.ID = sessionInfo.skillId;

                XWSDK.getInstance().getMorePlaylist(appInfo, playIdList.get(0), 20, true, new XWSDK.RequestListener() {
                    @Override
                    public boolean onRequest(int event, XWResponseInfo rspData, byte[] extendData) {
                        // 让控制层处理具体的数据
                        return XWeiControl.getInstance().processResponse(rspData.voiceID, rspData, extendData);
                    }
                });
            }
        }, 300);

    }

    private class IControlServiceImpl extends IControlService.Stub {

        @Override
        public int getCurrentPlayMode(int sessionId) throws RemoteException {
            XWeiPlayerInfo playerInfo = XWeiControl.getInstance().getMediaTool().txcGetPlayerInfo(sessionId);
            if (playerInfo != null) {
                return playerInfo.repeatMode;
            } else {
                return Constants.RepeatMode.REPEAT_MODE_SEQUENCE;
            }
        }

        @Override
        public boolean isPlaying(int sessionId) throws RemoteException {

            XWeiPlayerInfo playerInfo = XWeiControl.getInstance().getMediaTool().txcGetPlayerInfo(sessionId);
            return playerInfo != null && playerInfo.status == Constants.XWeiInnerPlayerStatus.STATUS_PLAY;
        }

        @Override
        public int getCurrentPosition(int sessionId) throws RemoteException {
            IXWeiPlayer player = XWeiControl.getInstance().getXWeiPlayerMgr().getXWeiPlayer(sessionId);

            return player != null ? player.getCurrentPosition() : 0;
        }

        @Override
        public int getDuration(int sessionId) throws RemoteException {
            IXWeiPlayer player = XWeiControl.getInstance().getXWeiPlayerMgr().getXWeiPlayer(sessionId);

            return player != null ? player.getDuration() : 0;
        }

        @Override
        public void seekTo(int sessionId, int position) throws RemoteException {
            IXWeiPlayer player = XWeiControl.getInstance().getXWeiPlayerMgr().getXWeiPlayer(sessionId);

            if (player != null) {
                player.seekTo(position);
            }
        }

        @Override
        public List<MediaMetaInfo> getCurrentMediaList(int sessionId) throws RemoteException {
            ArrayList<MediaMetaInfo> playList = new ArrayList<>();

            ArrayList<String> playIdArray = session2PlayIdArray.get(sessionId);

            if (playIdArray != null) {
                for (String playId : playIdArray) {
                    playList.add(id2PlayInfo.get(playId));
                }
            }

            return playList;
        }

        @Override
        public String[] getCurrentPlayIdList(int sessionId) throws RemoteException {

            ArrayList<String> playIdArray = session2PlayIdArray.get(sessionId);
            String[] data = new String[playIdArray.size()];

            return playIdArray.toArray(data);
        }

        @Override
        public MediaMetaInfo getCurrentMediaInfo(int sessionId) throws RemoteException {
            String playId = session2CurPlayId.get(sessionId);

            return id2PlayInfo.get(playId);
        }

        @Override
        public void getMoreList(int sessionId) throws RemoteException {
            loadMorePlayList(sessionId);
        }

        @Override
        public void refreshPlayList(final int sessionId) throws RemoteException {
            refreshPlayListIfNeed(sessionId, true);
        }

    }
}
