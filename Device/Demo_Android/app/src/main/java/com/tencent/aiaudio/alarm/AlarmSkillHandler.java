package com.tencent.aiaudio.alarm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.tencent.utils.MusicPlayer;
import com.tencent.xiaowei.control.Constants;
import com.tencent.xiaowei.control.XWeiAudioFocusManager;
import com.tencent.xiaowei.control.XWeiOuterSkill;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.info.XWAppInfo;
import com.tencent.xiaowei.info.XWPlayStateInfo;
import com.tencent.xiaowei.info.XWResGroupInfo;
import com.tencent.xiaowei.info.XWResponseInfo;
import com.tencent.xiaowei.sdk.XWSDK;

import static com.tencent.xiaowei.control.Constants.SKILL_NAME.SKILL_NAME_TRIGGER_ALARM;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_ALARM;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_GLOBAL;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_TRIGGER_ALARM;

public class AlarmSkillHandler implements XWeiOuterSkill.OuterSkillHandler {

    public static final String EXTRA_KEY_CLOSE_ALARM = "extra_key_alarm_close";
    private Context mContext;
    private XWeiAudioFocusManager.OnAudioFocusChangeListener listener;

    public AlarmSkillHandler(Context context) {
        this.mContext = context;
    }

    @Override
    public boolean handleResponse(int sessionId, XWResponseInfo responseInfo) {
        boolean handled = false;
        switch (responseInfo.appInfo.ID) {
            case SKILL_ID_ALARM:
                handled = handleAlarm(responseInfo);
                break;
            case SKILL_ID_TRIGGER_ALARM:
                handled = handlerTriggerAlarm(responseInfo);
                break;
            default:
                break;
        }

        return handled;
    }

    // 处理提醒类
    private boolean handleAlarm(final XWResponseInfo responseInfo) {
        boolean handled = DeviceSkillAlarmManager.instance().isSetAlarmOperation(responseInfo);
        final boolean isSnooze = DeviceSkillAlarmManager.instance().isSnoozeAlarm(responseInfo);

        if (handled) {
            handled = DeviceSkillAlarmManager.instance().operationAlarmSkill(responseInfo);
        }

        // 除了增删改闹钟场景外，有可能还有存在单TTS播放资源
        if (handled || (responseInfo.resources.length > 0 && responseInfo.resources[0].resources.length > 0)) {
            listener = new XWeiAudioFocusManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {
                    if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT) {
                        XWPlayStateInfo stateInfo = new XWPlayStateInfo();
                        stateInfo.appInfo = new XWAppInfo();
                        stateInfo.appInfo.ID = Constants.SkillIdDef.SKILL_ID_ALARM;
                        stateInfo.appInfo.name = Constants.SKILL_NAME.SKILL_NAME_ALARM;
                        stateInfo.state = XWCommonDef.PlayState.START;
                        XWSDK.getInstance().reportPlayState(stateInfo);

                        if (responseInfo.resources != null
                                && responseInfo.resources.length == 1
                                && responseInfo.resources[0].resources[0].format == XWCommonDef.ResourceFormat.TTS) {
                            MusicPlayer.getInstance().playMediaInfo(responseInfo.resources[0].resources[0], new MusicPlayer.OnPlayListener() {
                                @Override
                                public void onCompletion(int error) {
                                    if (isSnooze) {
                                        sendBroadcast(EXTRA_KEY_CLOSE_ALARM, null);
                                    }
                                    XWeiAudioFocusManager.getInstance().abandonAudioFocus(listener);
                                }

                            });
                        }
                    } else if (focusChange == XWeiAudioFocusManager.AUDIOFOCUS_LOSS) {
                        XWPlayStateInfo stateInfo = new XWPlayStateInfo();
                        stateInfo.appInfo = new XWAppInfo();
                        stateInfo.appInfo.ID = Constants.SkillIdDef.SKILL_ID_ALARM;
                        stateInfo.appInfo.name = Constants.SKILL_NAME.SKILL_NAME_ALARM;
                        stateInfo.state = XWCommonDef.PlayState.FINISH;
                        XWSDK.getInstance().reportPlayState(stateInfo);

                        if (isSnooze) {
                            sendBroadcast(EXTRA_KEY_CLOSE_ALARM, null);
                        }
                        MusicPlayer.getInstance().stop();
                    }
                }
            };

            XWeiAudioFocusManager.getInstance().requestAudioFocus(listener, XWeiAudioFocusManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }

        return handled || (responseInfo.resources.length > 0 && responseInfo.resources[0].resources.length > 0);
    }

    // 处理闹钟触发场景
    private boolean handlerTriggerAlarm(XWResponseInfo responseInfo) {
        boolean handled = false;

        int command = checkCommandId(responseInfo.resources);
        switch (command) {
            case Constants.PROPERTY_ID.PAUSE:
            case Constants.PROPERTY_ID.STOP:
            case Constants.PROPERTY_ID.EXIT:
                // 停止闹钟
                sendBroadcast(EXTRA_KEY_CLOSE_ALARM, null);
                handled = true;
                break;
        }

        return handled;

    }

    private int checkCommandId(XWResGroupInfo[] resources) {
        if (resources != null && resources.length > 0 && resources[0].resources.length > 0
                && resources[0].resources[0].format == XWCommonDef.ResourceFormat.COMMAND) {
            String id = resources[0].resources[0].ID;
            try {
                return Integer.valueOf(id);
            } catch (Exception e) {
            }
        }
        return 0;
    }

    private String getCommandValue(XWResGroupInfo[] resources) {
        if (resources != null && resources.length > 0 && resources[0].resources.length > 0
                && resources[0].resources[0].format == XWCommonDef.ResourceFormat.COMMAND) {
            return resources[0].resources[0].content;
        }
        return null;
    }

    private void sendBroadcast(String action, Bundle extra) {
        Intent intent = new Intent(action);
        if (extra != null)
            intent.putExtras(extra);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
