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
package com.tencent.aiaudio.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.adapter.CommonListAdapter;
import com.tencent.aiaudio.demo.IControlService;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.service.ControlService;
import com.tencent.aiaudio.utils.DemoOnAudioFocusChangeListener;
import com.tencent.aiaudio.utils.UIUtils;
import com.tencent.aiaudio.view.VoiceView;
import com.tencent.xiaowei.control.Constants;
import com.tencent.xiaowei.control.XWeiAudioFocusManager;
import com.tencent.xiaowei.control.XWeiControl;
import com.tencent.xiaowei.control.info.XWeiSessionInfo;
import com.tencent.xiaowei.def.XWCommonDef;
import com.tencent.xiaowei.info.MediaMetaInfo;
import com.tencent.xiaowei.info.XWAppInfo;
import com.tencent.xiaowei.info.XWContextInfo;
import com.tencent.xiaowei.sdk.XWSDK;
import com.tencent.xiaowei.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;

import static com.tencent.aiaudio.service.ControlService.ACTION_MUSIC_ON_KEEP;
import static com.tencent.aiaudio.service.ControlService.ACTION_MUSIC_ON_PAUSE;
import static com.tencent.aiaudio.service.ControlService.ACTION_MUSIC_ON_PLAY;
import static com.tencent.aiaudio.service.ControlService.ACTION_MUSIC_ON_REPEAT_MODE;
import static com.tencent.aiaudio.service.ControlService.ACTION_MUSIC_ON_RESUME;
import static com.tencent.aiaudio.service.ControlService.ACTION_MUSIC_ON_STOP;
import static com.tencent.aiaudio.service.ControlService.ACTION_MUSIC_ON_UNKEEP;
import static com.tencent.aiaudio.service.ControlService.ACTION_MUSIC_ON_UPDATE_PLAY_LIST;
import static com.tencent.aiaudio.service.ControlService.EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID;
import static com.tencent.aiaudio.service.ControlService.EXTRA_KEY_START_SKILL_DATA;
import static com.tencent.aiaudio.service.ControlService.EXTRA_KEY_START_SKILL_SESSION_ID;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_FM;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_MUSIC;
import static com.tencent.xiaowei.control.Constants.SkillIdDef.SKILL_ID_New;
import static com.tencent.xiaowei.def.XWCommonDef.PlayQuality.PLAY_QUALITY_HIGH;
import static com.tencent.xiaowei.def.XWCommonDef.PlayQuality.PLAY_QUALITY_LOSSLESS;
import static com.tencent.xiaowei.def.XWCommonDef.PlayQuality.PLAY_QUALITY_LOW;
import static com.tencent.xiaowei.def.XWCommonDef.PlayQuality.PLAY_QUALITY_NORMAL;

public class MusicActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = MusicActivity.class.getSimpleName();

    //返回按钮
    private View mBack;

    //帮助按钮
    private View mHelp;

    //我的收藏按钮
    private Button mBtnCollection;

    // QQ音乐品质全局设置
    private Button mBtnSettingQuality;

    //歌曲信息
    private RelativeLayout mMusicInfo;

    //歌曲封面
    private ImageView mCoverPic;

    //歌名
    private TextView mMusicName;

    //歌手
    private TextView mSinger;

    //未知
    private TextView mMusicAlbum;

    //歌曲时间(当前时间与歌曲总时间 00:12/04:00)
    private TextView mMusicTime;

    //播放进度条
    private SeekBar mMusicSeek;

    //上一曲
    private View mBefore;

    //暂停或播放
    private ImageView mBtnPlay;

    //下一首
    private View mBtnNext;

    //播放列表
    private ImageView mBtnPlaylist;

    //播放模式(单曲、随机、顺序)
    private ImageView mBtnPlayMode;

    //歌词
    private ImageView mBtnLyric;

    //喜欢(收藏)
    private ImageView mBtnLike;

    //品质
    private ImageView mBtnQuality;

    //收藏列表或者播放列表
    private LinearLayout mList;

    //界面中控制按键的父容器
    private RelativeLayout mCtrlBar;

    private boolean mIsShowLyric;
    private boolean mIsShowList;

    private MediaMetaInfo mCurrentMusicInfo;

    private Handler mHandler = new Handler();
    private Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mMusicService != null) {
                    setProgress(mMusicService.getCurrentPosition(sessionId), mMusicService.getDuration(sessionId));
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mHandler.postDelayed(this, 200);
        }
    };
    private int mPlayMode;

    private boolean isSeek;
    private ListView mListView;
    private CommonListAdapter<MediaMetaInfo> mAdapter;
    private View mParentView;

    private TextView mTvBack;

    private IControlService mMusicService;

    private static int lastSessionId = -1;

    // 品质的全局设置值
    private int settingQuality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mShowStatus = false;
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_qqmusic);
        bindViews();
        bindListener();
        initData(getIntent());

        bindService(new Intent(this, ControlService.class), mServiceConnection, BIND_AUTO_CREATE);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_MUSIC_ON_PLAY);
        filter.addAction(ACTION_MUSIC_ON_PAUSE);
        filter.addAction(ACTION_MUSIC_ON_RESUME);
        filter.addAction(ACTION_MUSIC_ON_REPEAT_MODE);
        filter.addAction(ACTION_MUSIC_ON_UPDATE_PLAY_LIST);
        filter.addAction(ACTION_MUSIC_ON_KEEP);
        filter.addAction(ACTION_MUSIC_ON_UNKEEP);
        filter.addAction(ACTION_MUSIC_ON_STOP);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mBroadcastReceiver, filter);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMusicService = IControlService.Stub.asInterface(service);
            updateUI();
            if (mMusicService != null) {
                try {
                    setMusicInfo(mMusicService.getCurrentMediaInfo(sessionId));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicService = null;
        }
    };

    private void updateUI() {
        try {
            if (mMusicService != null) {
                List<MediaMetaInfo> currentPlayList = mMusicService.getCurrentMediaList(sessionId);
                mCurrentMusicInfo = mMusicService.getCurrentMediaInfo(sessionId);
                if (mCurrentMusicInfo != null) {
                    mBtnLike.setImageResource(mCurrentMusicInfo.favorite ? R.drawable.btn_like_active : R.drawable.btn_like);
                    showCurMusicQuality(mCurrentMusicInfo.quality);
                    handleLyric(mCurrentMusicInfo.lyric);
                }

                mAdapter.clear();
                mAdapter.addAll(currentPlayList);
                mAdapter.notifyDataSetChanged();

                mPlayMode = mMusicService.getCurrentPlayMode(sessionId);

                mBtnPlayMode.setImageResource((mPlayMode == Constants.RepeatMode.REPEAT_MODE_SEQUENCE
                        || mPlayMode == Constants.RepeatMode.REPEAT_MODE_LOOP) ? R.drawable.btn_play_mode_order :
                        (mPlayMode == Constants.RepeatMode.REPEAT_MODE_RANDOM ? R.drawable.btn_play_mode_shuffle : R.drawable.btn_play_mode_single));

                mBtnPlay.setImageResource(mMusicService.isPlaying(sessionId) ? R.drawable.btn_pause : R.drawable.btn_play);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新播放列表
     */
    private void updatePlayList() {
        try {
            if (mMusicService != null) {
                List<MediaMetaInfo> currentPlayList = mMusicService.getCurrentMediaList(sessionId);
                mAdapter.clear();
                mAdapter.addAll(currentPlayList);
                mAdapter.notifyDataSetChanged();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private int mSessionId;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, intent.getAction());
            switch (intent.getAction()) {
                case ACTION_MUSIC_ON_PLAY:
                    mSessionId = intent.getIntExtra(EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID, 0);
                    if (mSessionId != sessionId) {
                        return;
                    }
                    try {
                        if (mMusicService != null) {
                            setMusicInfo(mMusicService.getCurrentMediaInfo(sessionId));
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mMusicTime.setText("00:00/00:00");
                    if (mCurrentMusicInfo != null) {
                        handleLyric(mCurrentMusicInfo.lyric);
                    }
                    Log.d(TAG, "正在播放：" + mCurrentMusicInfo);
                    mBtnPlay.setImageResource(R.drawable.btn_pause);
                    mAdapter.notifyDataSetChanged();
                    break;
                case ACTION_MUSIC_ON_PAUSE:
                    mSessionId = intent.getIntExtra(EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID, 0);
                    if (mSessionId != sessionId) {
                        return;
                    }
                    mBtnPlay.setImageResource(R.drawable.btn_play);
                    break;
                case ACTION_MUSIC_ON_RESUME:
                    mSessionId = intent.getIntExtra(EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID, 0);
                    if (mSessionId != sessionId) {
                        return;
                    }
                    mBtnPlay.setImageResource(R.drawable.btn_pause);
                    break;
                case ACTION_MUSIC_ON_REPEAT_MODE:
                    mSessionId = intent.getIntExtra(EXTRA_KEY_MUSIC_ON_EVENT_SESSION_ID, 0);
                    if (mSessionId != sessionId) {
                        return;
                    }
                    try {
                        mPlayMode = mMusicService.getCurrentPlayMode(sessionId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    mBtnPlayMode.setImageResource((mPlayMode == Constants.RepeatMode.REPEAT_MODE_SEQUENCE
                            || mPlayMode == Constants.RepeatMode.REPEAT_MODE_LOOP) ? R.drawable.btn_play_mode_order :
                            (mPlayMode == Constants.RepeatMode.REPEAT_MODE_RANDOM ? R.drawable.btn_play_mode_shuffle : R.drawable.btn_play_mode_single));
                    break;
                case ACTION_MUSIC_ON_UPDATE_PLAY_LIST:
                    mSessionId = intent.getIntExtra(EXTRA_KEY_START_SKILL_SESSION_ID, 0);
                    if (mSessionId != sessionId) {
                        return;
                    }
                    updateUI();
                    break;
                case ACTION_MUSIC_ON_KEEP:
                    if (mCurrentMusicInfo != null) {
                        mCurrentMusicInfo.favorite = true;
                    }
                    mBtnLike.setImageResource(R.drawable.btn_like_active);
                    UIUtils.showToast("音乐已收藏成功");
                    break;
                case ACTION_MUSIC_ON_UNKEEP:
                    if (mCurrentMusicInfo != null) {
                        mCurrentMusicInfo.favorite = false;
                    }
                    mBtnLike.setImageResource(R.drawable.btn_like);
                    UIUtils.showToast("音乐已取消收藏");
                    break;
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent");
        initData(intent);
        updateUI();
    }

    /**
     * 绑定布局文件中的控件
     */
    private void bindViews() {
        mParentView = findViewById(R.id.v_p);
        mBack = findViewById(R.id.btn_back);
        mTvBack = (TextView) findViewById(R.id.tv_back);
        mHelp = findViewById(R.id.img_music_help);
        mBtnCollection = (Button) findViewById(R.id.txt_collection);
        mBtnSettingQuality = (Button) findViewById(R.id.txt_quality);
        mMusicInfo = (RelativeLayout) findViewById(R.id.music_info);
        mMusicName = (TextView) findViewById(R.id.txt_fm_name);
        mSinger = (TextView) findViewById(R.id.txt_fm_singer);
        mMusicAlbum = (TextView) findViewById(R.id.txt_music_album);
        mMusicTime = (TextView) findViewById(R.id.txt_music_time);
        mMusicSeek = (SeekBar) findViewById(R.id.skb_music_seek);
        mBefore = findViewById(R.id.img_music_before);
        mBtnPlay = (ImageView) findViewById(R.id.img_music_play);
        mBtnNext = findViewById(R.id.img_music_next);
        mBtnPlaylist = (ImageView) findViewById(R.id.img_music_playlist);
        mBtnPlayMode = (ImageView) findViewById(R.id.img_music_mode);
        mBtnQuality = (ImageView) findViewById(R.id.img_music_quality);
        mBtnLyric = (ImageView) findViewById(R.id.img_music_lyric);
        mBtnLike = (ImageView) findViewById(R.id.img_music_like);
        mList = (LinearLayout) findViewById(R.id.music_list);
        mCoverPic = (ImageView) findViewById(R.id.img_music_cover);
        mCtrlBar = (RelativeLayout) findViewById(R.id.rlt_music_ctrlbar);

        mListView = (ListView) findViewById(R.id.lv_list);
        mAdapter = new CommonListAdapter<MediaMetaInfo>() {
            @Override
            protected View initListCell(int position, View convertView, ViewGroup parent) {

                Holder holder;
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.music_list_item, parent, false);
                    holder = new Holder();
                    holder.tvName = (TextView) convertView.findViewById(R.id.item_music_name);
                    holder.tvSingle = (TextView) convertView.findViewById(R.id.item_music_singer);
                    holder.voiceView = (VoiceView) convertView.findViewById(R.id.item_music_select);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.tvName.setText(mAdapter.getItem(position).name != null ? mAdapter.getItem(position).name : "");
                holder.tvSingle.setText(mAdapter.getItem(position).artist != null ? mAdapter.getItem(position).artist : "");
                if (mAdapter.getItem(position).equals(mCurrentMusicInfo)) {
                    holder.voiceView.setVisibility(View.VISIBLE);
                } else {
                    holder.voiceView.setVisibility(View.GONE);
                }
                return convertView;
            }

            class Holder {
                TextView tvName;
                TextView tvSingle;
                VoiceView voiceView;
            }
        };
        mListView.setAdapter(mAdapter);

    }

    private void initData(Intent intent) {
        settingQuality = sp.getInt("SP_KEY_SETTING_QUALITY", XWCommonDef.PlayQuality.PLAY_QUALITY_LOW);
        showSettingQuality(settingQuality);

        sessionId = intent.getIntExtra(EXTRA_KEY_START_SKILL_SESSION_ID, -1);
        String firstMusic = intent.getStringExtra(EXTRA_KEY_START_SKILL_DATA);
        setMusicInfo(JsonUtil.getObject(firstMusic, MediaMetaInfo.class));
        if (sessionId == -1) {
            sessionId = getLastSessionId();
            if (sessionId != -1) {
                XWeiSessionInfo sessionInfo = XWeiControl.getInstance().getAppTool().txcGetSession(sessionId);
                skillName = sessionInfo.skillName;
                skillId = sessionInfo.skillId;
            } else {
                // DO Nothing
            }
        } else {
            setLastSessionId(sessionId);
        }
        if (skillId != null) {
            switch (skillId) {
                case SKILL_ID_MUSIC:
                    mTvBack.setText(skillName);
                    mBtnLyric.setVisibility(View.VISIBLE);
                    mBtnLike.setVisibility(View.VISIBLE);
                    mBtnCollection.setVisibility(View.VISIBLE);
                    mBtnSettingQuality.setVisibility(View.VISIBLE);
                    mBtnQuality.setVisibility(View.VISIBLE);
                    showMusicInfo();
                    break;
                case SKILL_ID_FM:
                    mTvBack.setText(skillName);
                    mBtnLike.setVisibility(View.VISIBLE);
                    mBtnLyric.setVisibility(View.GONE);
                    mBtnCollection.setVisibility(View.VISIBLE);
                    mBtnSettingQuality.setVisibility(View.GONE);
                    mBtnPlayMode.setVisibility(View.GONE);
                    mBtnQuality.setVisibility(View.GONE);
                    break;
                case SKILL_ID_New:
                default:
                    mTvBack.setText(skillName);
                    mBtnLike.setVisibility(View.GONE);
                    mBtnLyric.setVisibility(View.GONE);
                    mBtnCollection.setVisibility(View.GONE);
                    mBtnSettingQuality.setVisibility(View.GONE);
                    mBtnPlayMode.setVisibility(View.GONE);
                    mBtnQuality.setVisibility(View.GONE);
                    break;
            }
        }
    }

    protected int getLastSessionId() {
        return lastSessionId;
    }

    protected void setLastSessionId(int sessionId) {
        lastSessionId = sessionId;
    }

    @Override
    public void onSkillIdle() {
        super.onSkillIdle();
        mCurrentMusicInfo = null;
        finish();
    }

    private void setMusicInfo(MediaMetaInfo info) {
        if (info == null) {
            return;

        }
        mCurrentMusicInfo = info;

        if (!TextUtils.isEmpty(mCurrentMusicInfo.cover)) {
            Picasso.with(getApplicationContext()).load(mCurrentMusicInfo.cover).into(mCoverPic);
        }

        mMusicName.setText(mCurrentMusicInfo.name);
        mSinger.setText(mCurrentMusicInfo.artist);
        mMusicAlbum.setText(mCurrentMusicInfo.album);
        mBtnLike.setImageResource(mCurrentMusicInfo.favorite ? R.drawable.btn_like_active : R.drawable.btn_like);

        showCurMusicQuality(info.quality);

        if (!TextUtils.isEmpty(mCurrentMusicInfo.lyric)) {
            handleLyric(mCurrentMusicInfo.lyric);
        }
    }

    private void bindListener() {
        mBack.setOnClickListener(this);
        mBtnPlay.setOnClickListener(this);
        mBtnPlaylist.setOnClickListener(this);
        mBtnLyric.setOnClickListener(this);
        mBtnLike.setOnClickListener(this);
        mBefore.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnCollection.setOnClickListener(this);
        mBtnPlayMode.setOnClickListener(this);
        mBtnQuality.setOnClickListener(this);
        mHelp.setOnClickListener(this);
        mBtnSettingQuality.setOnClickListener(this);
        mMusicSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
                isSeek = fromUser;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                try {
                    if (mMusicService != null) {
                        mMusicService.seekTo(sessionId, progress);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                isSeek = false;
            }
        });


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                boolean result = XWeiControl.getInstance().
                        getMediaTool().txcPlayerControl(sessionId, Constants.XWeiControlCode.PLAYER_PLAY, position, 0);
                if (!result) {
                    UIUtils.showToast("切歌失败");
                }
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (view.getLastVisiblePosition() >= view.getCount() - 2) {
                        try {
                            mMusicService.getMoreList(sessionId);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void setProgress(int current, int max) {
        mMusicSeek.setMax(max);
        if (!isSeek) {
            mMusicSeek.setProgress(current);
        }
        mMusicTime.setText(getFormatText(current) + "/" + getFormatText(max));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.post(mTimerRunnable);
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (XWeiAudioFocusManager.getInstance().needRequestFocus(AudioManager.AUDIOFOCUS_GAIN)) {
            int ret = mAudioManager.requestAudioFocus(DemoOnAudioFocusChangeListener.getInstance(), AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (ret == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                XWeiAudioFocusManager.getInstance().setAudioFocusChange(AudioManager.AUDIOFOCUS_GAIN);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
        unbindService(mServiceConnection);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_music_play:
                playMusic();
                break;
            case R.id.img_music_lyric:
//                if (mIsShowLyric)
//                    showMusicInfo();
//                else
//                    showLyric();
                UIUtils.showToast("请自行实现歌词显示功能");
                break;
            case R.id.img_music_playlist:
                showList();
                break;
            case R.id.img_music_like:
                if (mCurrentMusicInfo != null) {
                    XWAppInfo appInfo = new XWAppInfo();
                    appInfo.ID = skillId;
                    appInfo.name = skillName;
                    XWSDK.getInstance().setFavorite(appInfo, mCurrentMusicInfo.playId, !mCurrentMusicInfo.favorite);
                } else {
                    UIUtils.showToast("当前没有歌曲在播放");
                }
                break;
            case R.id.img_music_before:
                XWeiControl.getInstance().
                        getMediaTool().txcPlayerControl(sessionId, Constants.XWeiControlCode.PLAYER_NEXT, -1, 0);
                break;
            case R.id.img_music_next:
                XWeiControl.getInstance().
                        getMediaTool().txcPlayerControl(sessionId, Constants.XWeiControlCode.PLAYER_NEXT, 1, 0);
                break;
            case R.id.btn_back:
                finish();
                break;
            case R.id.txt_collection:
                playFavorite();

                break;
            case R.id.txt_quality:
                int tempQuality = (settingQuality + 1) % (XWCommonDef.PlayQuality.PLAY_QUALITY_LOSSLESS + 1);
                if (XWSDK.getInstance().setMusicQuality(tempQuality) == XWCommonDef.ErrorCode.ERROR_NULL_SUCC) {
                    settingQuality = tempQuality;
                    editor.putInt("SP_KEY_SETTING_QUALITY", settingQuality);
                    editor.apply();
                    showSettingQuality(settingQuality);

                    try {
                        mMusicService.refreshPlayList(sessionId);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.img_music_mode:
                mPlayMode++;
                // 产品不希望区分循环和顺序。在音乐只列表循环，其余只列表顺序。
                if (mPlayMode >= Constants.RepeatMode.REPEAT_MODE_SEQUENCE) {
                    mPlayMode = Constants.RepeatMode.REPEAT_MODE_RANDOM;
                }

                XWeiControl.getInstance()
                        .getMediaTool()
                        .txcPlayerControl(sessionId, Constants.XWeiControlCode.PLAYER_REPEAT, mPlayMode, 0);

                break;
            case R.id.img_music_help:
                Intent intent = new Intent(this, HelperActivity.class);
                ArrayList<String> helps = new ArrayList<>();
                switch (skillId) {
                    case SKILL_ID_MUSIC:
                        intent.putExtra("title", "QQ音乐");
                        helps.add(getString(R.string.help_music_1));
                        helps.add(getString(R.string.help_music_2));
                        helps.add(getString(R.string.help_music_3));
                        helps.add(getString(R.string.help_music_4));
                        intent.putExtra("helps", helps);
                        break;
                    case SKILL_ID_FM:
                        intent.putExtra("title", "企鹅FM");
                        helps.add("小微，播放《中国之声》");
                        helps.add("小微，我想听电台");
                        helps.add("小微，讲个笑话");
                        helps.add("小微，播放睡前故事");
                        intent.putExtra("helps", helps);
                        break;
                    case SKILL_ID_New:
                        intent.putExtra("title", "新闻");
                        helps.add("小微，播放新闻");
                        helps.add("小微，我想听新闻");
                        helps.add("小微，我想听科技新闻");
                        intent.putExtra("helps", helps);
                        break;
                }

                startActivity(intent);
                break;
        }
    }

    protected void playFavorite() {
        XWSDK.getInstance().request(XWCommonDef.RequestType.TEXT, "播放我收藏的音乐".getBytes(), new XWContextInfo());
    }

    private void playMusic() {
        boolean isPlaying = false;
        try {
            isPlaying = mMusicService.isPlaying(sessionId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (!isPlaying) {
            XWeiControl.getInstance()
                    .getMediaTool()
                    .txcPlayerControl(sessionId, Constants.XWeiControlCode.PLAYER_RESUME, 0, 0);
        } else {
            XWeiControl.getInstance()
                    .getMediaTool()
                    .txcPlayerControl(sessionId, Constants.XWeiControlCode.PLAYER_PAUSE, 0, 0);
        }
    }

    private void showLyric() {
        mIsShowLyric = true;
        mBtnLyric.setImageResource(R.drawable.btn_lyric_active);
        mMusicInfo.setVisibility(View.GONE);
    }

    private void showMusicInfo() {
        mIsShowLyric = false;
        mBtnLyric.setImageResource(R.drawable.btn_lyric);
        mMusicInfo.setVisibility(View.VISIBLE);

//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showLyric();
//            }
//        }, 5000);
    }

    //显示播放列表或收藏列表
    private void showList() {
        mIsShowList = !mIsShowList;
        int marginRight;
        if (mIsShowList) {
            marginRight = 20;
            mList.setVisibility(View.VISIBLE);
            mBtnPlaylist.setImageResource(R.drawable.btn_play_list_active);
        } else {
            marginRight = 50;
            mList.setVisibility(View.GONE);
            mBtnPlaylist.setImageResource(R.drawable.btn_play_list);
        }
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mMusicSeek.getLayoutParams();
        UIUtils.changeMargins(mMusicSeek, params.leftMargin, params.topMargin, UIUtils.dip2px(this, marginRight), params.bottomMargin);
        params = (ViewGroup.MarginLayoutParams) mCtrlBar.getLayoutParams();
        UIUtils.changeMargins(mCtrlBar, params.leftMargin, params.topMargin, UIUtils.dip2px(this, marginRight), params.bottomMargin);
        params = (ViewGroup.MarginLayoutParams) mMusicTime.getLayoutParams();
        UIUtils.changeMargins(mMusicTime, params.leftMargin, params.topMargin, UIUtils.dip2px(this, marginRight), params.bottomMargin);
    }

    /**
     * 显示当前歌曲的品质信息
     *
     * @param quality 音乐品质值
     */
    private void showCurMusicQuality(int quality) {
        switch (quality) {
            case PLAY_QUALITY_LOSSLESS:
                mBtnQuality.setImageResource(R.drawable.btn_quality_lossless);
                break;
            case PLAY_QUALITY_HIGH:
                mBtnQuality.setImageResource(R.drawable.btn_quality_high);
                break;
            case PLAY_QUALITY_NORMAL:
                mBtnQuality.setImageResource(R.drawable.btn_quality_mid);
                break;
            case PLAY_QUALITY_LOW:
                mBtnQuality.setImageResource(R.drawable.btn_quality_low);
                break;
            default:
                break;
        }
    }

    /**
     * 显示全局的音乐品质设定
     *
     * @param quality 音乐品质
     */
    private void showSettingQuality(int quality) {
        switch (quality) {
            case PLAY_QUALITY_LOSSLESS:
                mBtnSettingQuality.setText(R.string.loss_less_quality);
                break;
            case PLAY_QUALITY_HIGH:
                mBtnSettingQuality.setText(R.string.high_quality);
                break;
            case PLAY_QUALITY_NORMAL:
                mBtnSettingQuality.setText(R.string.normal_quality);
                break;
            case PLAY_QUALITY_LOW:
                mBtnSettingQuality.setText(R.string.low_quality);
                break;
            default:
                break;
        }
    }

    /**
     * 歌词处理
     */
    private void handleLyric(String lyric) {
        // 请自行处理歌词的显示
    }


    private String getFormatText(int duration) {
        duration /= 1000;
        StringBuffer sb = new StringBuffer();
        int min = duration / 60;
        if (min < 10) {
            sb.append("0");
        }
        sb.append(min);
        sb.append(":");
        int sec = duration % 60;
        if (sec < 10) {
            sb.append("0");
        }
        sb.append(sec);
        return sb.toString();
    }
}
