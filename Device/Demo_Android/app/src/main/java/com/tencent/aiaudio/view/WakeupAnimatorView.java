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
package com.tencent.aiaudio.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.utils.UIUtils;
import com.tencent.xiaowei.util.QLog;

/**
 * 唤醒动画
 */
public class WakeupAnimatorView extends SurfaceView implements SurfaceHolder.Callback {

    public static final String TAG = "WakeupAnimatorView";


    private State mState = State.IDLE;

    // 圆
    private int mColor1 = Color.parseColor("#006eff");
    private int mColor2 = Color.parseColor("#00a4ff");
    private int mColor3 = Color.parseColor("#00D9FF");

    /**
     * 圆半径
     */
    private float mStaticCircleRadius;

    private int mHeight;
    private int mWidthCircle;
    private boolean isThinking;

    private int mStokeWidth = UIUtils.dip2px(getContext(), 4);
    private int mLineOffsetY = 0; //圆变直线过程中的y轴偏移，从半径到0变化

    private Paint mPaintCircle = new Paint();
    private int mInter = 0;
    private int mFreshCount = 0;
    private Path mPathCircle = new Path();
    private ValueAnimator mEnteringAnimator;
    private ValueAnimator mWaitingAnimator;

    // 波形

    public static final boolean DEBUG_DRAW = false;
    private static final int FRAME_PERIOD = 25;
    private static final int MIN_SLEEP_TIME = 3;
    private static final float DEFAULT_AMPLIDUTE = 0.05f; //0.8f; //0.05f;
    private static final float SPEED_FAST = 0.05f;
    private static final float SPEED_SLOW = 0.014f;
    private static final float WAVE_FREQ = 0.16f;

    private boolean mIsAnimator = true;
    private final Object mSurfaceLock = new Object();
    private DrawThread mThread;
    private int mWidth = 0;
    private int mHeight_2 = 0;
    private int mMaxHeight = 0;
    private int mTruncateX;

    private Path mPathLine;
    private Path mPath;
    private Paint mPaint;
    private float mLineWidth = 0;

    private float mAmplidute = DEFAULT_AMPLIDUTE;
    private float mNewAmplidute = DEFAULT_AMPLIDUTE;

    private float mPhase = 0;
    private float mSpeed = SPEED_SLOW;

    private int mDensity = 58;
    private float[] mAttrCache;
    private float[] mLineCacheY;
    private float[] mXPosMap;

    private ParticleAnimator mParticleAnimator;
    private boolean mParticleVisible = false;

    private int line_1_start_color;
    private int line_1_end_color;
    private int line_2_start_color;
    private int line_2_end_color;
    private int line_3_color;
    private int region_1_start_color;
    private int region_1_end_color;
    private int region_2_start_color;
    private int region_2_end_color;

    private int mStartX = 0;
    private boolean mDismissFlag = false;

    private State oldState = State.IDLE;
    private Bitmap mBitmapIcon;

    /**
     * 获得当前State
     *
     * @return
     */
    public State getState() {
        return mState;

    }

    /**
     * 状态 空闲、等待唤醒、入场、听、思考、出场
     */
    public enum State {
        IDLE, WAITE, IN, LISTEN, THINK, OUT,
    }

    public WakeupAnimatorView(Context context) {
        super(context);
        init(context);
    }

    public WakeupAnimatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WakeupAnimatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // 关闭硬件加速

        mPaintCircle.setAntiAlias(true);
        mPaintCircle.setStrokeWidth(mStokeWidth);
        mPaintCircle.setStyle(Paint.Style.STROKE);

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        mPath = new Path();
        mPathLine = new Path();
        mPaint = new Paint();
        initCache();
        mLineWidth = context.getResources().getDimensionPixelSize(R.dimen.wave_line_width);
        mTruncateX = context.getResources().getDimensionPixelSize(R.dimen.wave_truncate_x);
        mParticleAnimator = new ParticleAnimator(context);

        line_1_start_color = getResources().getColor(R.color.wave_line_1_start_color);
        line_1_end_color = getResources().getColor(R.color.wave_line_1_end_color);
        line_2_start_color = getResources().getColor(R.color.wave_line_2_start_color);
        line_2_end_color = getResources().getColor(R.color.wave_line_2_end_color);
        line_3_color = getResources().getColor(R.color.white_20_color);

        region_1_start_color = getResources().getColor(R.color.wave_region_1_start_color);
        region_1_end_color = getResources().getColor(R.color.wave_region_1_end_color);
        region_2_start_color = getResources().getColor(R.color.wave_region_2_start_color);
        region_2_end_color = getResources().getColor(R.color.wave_region_2_end_color);

        mXPosMap = new float[mDensity];

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        startAnimator();
        setState(State.WAITE, null);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        mBitmapIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_float_speak);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidthCircle = w;
        mHeight = h;
        mStaticCircleRadius = Math.min(w, h) / 2 - UIUtils.dip2px(getContext(), 10);
        mLineOffsetY = (int) mStaticCircleRadius;
    }


    public interface OnCompletionListener {
        void onCompletion(boolean cancel, State oldState, State newState);
    }

    private OnCompletionListener mOnCompletionListener;

    /**
     * 设置动画状态
     *
     * @param state    {@link State}
     * @param listener State.IN 和State.OUT需要时间，可以设置listener
     */
    public void setState(State state, OnCompletionListener listener) {
        if (state == mState) {
            if (listener != null)
                listener.onCompletion(true, mState, state);
            return;
        }

        if (mOnCompletionListener != null) {
            OnCompletionListener lis = mOnCompletionListener;
            mOnCompletionListener = null;
            lis.onCompletion(true, mState, state);
        }
        mOnCompletionListener = listener;
        oldState = mState;

        QLog.d(TAG, "setState oldState:" + oldState.name() + " state:" + state.name());
        if (oldState != State.WAITE && state == State.IN) {
            return;
        }
        if (oldState != State.LISTEN && state == State.THINK) {
            return;
        }
        if ((oldState != State.THINK && oldState != State.LISTEN) && state == State.OUT) {
            return;
        }
        if ((oldState != State.IDLE && oldState != State.OUT) && state == State.WAITE) {
            return;
        }
        mState = state;
        cancelAnimator();
        if (mState == State.WAITE) {
//            startWaitingAnimator();
        } else if (mState == State.IN) {// Lis
            startEnteringAnimator();
        } else if (mState == State.LISTEN) {
            reset();
            invalidate();
        } else if (mState == State.THINK) {
            if (oldState != State.OUT) {
                thinking();
            }
        } else if (mState == State.OUT) {// lis
            dismissWave();
        }
    }


    private void drawCircle(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (!mIsAnimator)
            return;
        int layerId = canvas.saveLayer(0, 0, mWidthCircle, mHeight, null, Canvas.ALL_SAVE_FLAG);
        {
            if (mState == State.IN) {
                drawEnteringView(canvas);
            } else if (mState == State.WAITE) {
                int width = UIUtils.dip2px(getContext(), 100);
                int height = width * mBitmapIcon.getHeight() / mBitmapIcon.getWidth();
                canvas.drawBitmap(mBitmapIcon, new Rect(0, 0, mBitmapIcon.getWidth(), mBitmapIcon.getHeight()), new Rect((getMeasuredWidth() - width) / 2, (getMeasuredHeight() - height) / 2, (getMeasuredWidth() - width) / 2 + width, (getMeasuredHeight() - height) / 2 + height)
                        , mPaintCircle);

//                mPaintCircle.setColor(mColor1);
//                canvas.drawCircle((float) (mWidthCircle / 2 - mInter * Math.cos(2 * mFreshCount * Math.PI / 20)), (float) (mHeight / 2 - mInter * Math.sin(2 * mFreshCount * Math.PI / 20)), mStaticCircleRadius, mPaintCircle);
//                mPaintCircle.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
//                mPaintCircle.setColor(mColor2);
//                canvas.drawCircle((float) (mWidthCircle / 2 - mInter * 0.7 * Math.cos(2 * mFreshCount * Math.PI / 20)), (float) (mHeight / 2 - mInter * 0.7 * Math.sin(2 * mFreshCount * Math.PI / 20)), mStaticCircleRadius, mPaintCircle);
                mPaintCircle.setColor(mColor3);
//                canvas.drawCircle((float) (mWidthCircle / 2 - mInter * 0.3 * Math.cos(2 * mFreshCount * Math.PI / 20)), (float) (mHeight / 2 - mInter * 0.3 * Math.sin(2 * mFreshCount * Math.PI / 20)), mStaticCircleRadius, mPaintCircle);
//                mPaintCircle.setXfermode(null);
            }


        }
        canvas.restoreToCount(layerId);
    }

    /**
     * 开始动画
     */
    public void startAnimator() {
        mIsAnimator = true;
        reset();
    }

    private void startEnteringAnimator() {
        if (mEnteringAnimator != null) {
            mEnteringAnimator.cancel();
            mEnteringAnimator = null;
        }
        mEnteringAnimator = ValueAnimator.ofInt((int) mStaticCircleRadius, 0);
        mEnteringAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mEnteringAnimator.setDuration(500);
        mEnteringAnimator.setRepeatCount(0);
        mEnteringAnimator.start();
        mEnteringAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int curValue = (int) animation.getAnimatedValue();
                if (mIsAnimator && mLineOffsetY != curValue) {
                    mLineOffsetY = curValue;
                    mStokeWidth = Math.max((int) ((curValue / mStaticCircleRadius) * UIUtils.dip2px(getContext(), 2)), UIUtils.dip2px(getContext(), 2));
                    invalidate();
                }
            }
        });
        mEnteringAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mLineOffsetY = (int) mStaticCircleRadius;
                if (mOnCompletionListener != null) {
                    OnCompletionListener lis = mOnCompletionListener;
                    mOnCompletionListener = null;
                    lis.onCompletion(false, oldState, State.IN);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if (mOnCompletionListener != null) {
                    OnCompletionListener lis = mOnCompletionListener;
                    mOnCompletionListener = null;
                    lis.onCompletion(true, oldState, State.IN);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void startWaitingAnimator() {
        if (mWaitingAnimator != null) {
            mWaitingAnimator.cancel();
            mWaitingAnimator = null;
        }
        mWaitingAnimator = ValueAnimator.ofInt(0, 10);
        mWaitingAnimator.setInterpolator(new LinearInterpolator());
        mWaitingAnimator.setDuration(2000);
        mWaitingAnimator.setRepeatCount(Animation.INFINITE);
        mWaitingAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mWaitingAnimator.start();
        mWaitingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int curValue = (int) animation.getAnimatedValue();
                if (mIsAnimator && mInter != curValue) {
                    mInter = curValue;
                    ++mFreshCount;
                    if (mFreshCount == 40) {
                        mFreshCount = 0;
                    }
                    invalidate();
                }
            }
        });

    }

    private void cancelAnimator() {
        if (mEnteringAnimator != null) {
            mEnteringAnimator.cancel();
            mEnteringAnimator = null;
        }
        if (mWaitingAnimator != null) {
            mWaitingAnimator.cancel();
            mWaitingAnimator = null;
        }
    }


    private void drawEnteringView(Canvas canvas) {

        mPaintCircle.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        mPathCircle.reset();

        mPaintCircle.setStrokeWidth(mStokeWidth);


        float offsetX = (mWidthCircle / 2 - mStaticCircleRadius) * mLineOffsetY / mStaticCircleRadius;


        //椭圆
        RectF oval = new RectF(offsetX, mHeight / 2 - mLineOffsetY, mWidthCircle - offsetX, mHeight / 2 + mLineOffsetY);
        canvas.drawOval(oval, mPaintCircle);
    }


    private class DrawThread extends Thread {
        private static final String TAG = "DrawThread";
        private SurfaceHolder mHolder;
        private boolean mIsRun = false;


        public DrawThread(SurfaceHolder holder) {
            super(TAG);
            mHolder = holder;
        }

        @Override
        public void run() {
            long startTime, useTime, sleepTime;
            int count = 0;
            Log.d(TAG, "DrawThread start run!");
            while (mIsRun) {
                if (mIsAnimator) {
                    synchronized (mSurfaceLock) {
                        if (!mIsRun) {
                            return;
                        }
                        if (mDismissFlag && mStartX < mDensity) {
                            mStartX += (mDensity / (250 / FRAME_PERIOD));
                        }
                        startTime = System.nanoTime();
                        Canvas canvas = mHolder.lockCanvas();
                        if (canvas != null) {
                            if (mState == WakeupAnimatorView.State.WAITE || mState == WakeupAnimatorView.State.IN) {
                                drawCircle(canvas);
                            } else {
                                doDraw(canvas);
                            }
                            mHolder.unlockCanvasAndPost(canvas);
                        }
                        useTime = System.nanoTime() - startTime;
                    }
                } else {
                    useTime = 0;
                }

                sleepTime = FRAME_PERIOD - useTime / (1000L * 1000L);
                sleepTime = Math.max(MIN_SLEEP_TIME, sleepTime);
                if (DEBUG_DRAW && ++count > 200) {
                    count = 0;
                    Log.d(TAG, "useTime:" + useTime / (1000L * 1000L) + ", sleepTime" + sleepTime);
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }
            Log.d(TAG, "DrawThread out!");
        }

        public void setRun(boolean isRun) {
            this.mIsRun = isRun;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        mThread = new DrawThread(holder);
        mThread.setRun(true);
        mThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        mWidth = width;
        mHeight_2 = height / 2;
        this.mMaxHeight = getResources().getDimensionPixelSize(R.dimen.wave_view_sin_max_height);
        initXPosMap();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        synchronized (mSurfaceLock) {
            mThread.setRun(false);
        }
        mThread = null;
    }

    /**
     * 停止动画
     */
    public void stopAnimator() {
        mState = State.IDLE;
        oldState = State.IDLE;
        isThinking = false;
        mIsAnimator = false;
        mStartX = 0;
        mDismissFlag = false;
    }

    private void thinking() {
        isThinking = true;

        post(new Runnable() {
            @Override
            public void run() {
                if (isThinking) {
                    setNoise(0.1f);
                    postDelayed(this, 100);
                }
            }
        });
    }

    /**
     * 设置振幅
     *
     * @param noise 0-1f
     */
    public void setNoise(float noise) {
        this.mNewAmplidute = Math.max(Math.min(noise, 1.0f), 0f);
        this.mSpeed = SPEED_FAST;
        mParticleAnimator.setNoise(mNewAmplidute);
    }

    private void reset() {
        mStartX = 0;
        mDismissFlag = false;
        isThinking = false;
        this.mNewAmplidute = DEFAULT_AMPLIDUTE;
        this.mAmplidute = this.mNewAmplidute;
        this.mSpeed = SPEED_SLOW;
        mParticleAnimator.setNoise(mNewAmplidute);
    }

    public void setParticleVisible(boolean isVisible) {
        mParticleVisible = isVisible;
    }

    private void dismissWave() {
        mDismissFlag = true;
        isThinking = false;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mState == State.OUT) {
                    if (mOnCompletionListener != null) {
                        OnCompletionListener lis = mOnCompletionListener;
                        mOnCompletionListener = null;
                        lis.onCompletion(false, oldState, State.OUT);
                    }
                }
            }
        }, 250);
    }

    private void initCache() {
        mLineCacheY = new float[mDensity];
        mAttrCache = new float[mDensity];

        for (int i = 0; i < mDensity; i++) {
            mAttrCache[i] = getGlobAtt(i);
        }
    }

    private void initXPosMap() {
        for (int i = 0; i < mDensity; i++) {
            mXPosMap[i] = getXPos(i);
        }
    }

    /**
     * (4/(4+ x4))2.5   -2 到 2 之间。  如果的是0 - mDensity。 需要转换到-2 到 2
     */
    private float getGlobAtt(int x) {
        float i = (-2 + 4.0f / mDensity * x);
        return (float) Math.pow(4.0f / (4.0f + Math.pow(i, 4)), 2.5);
    }

    private void getRealA() {
        if (mAmplidute < mNewAmplidute) {
            float upStep = 0.05f;
            mAmplidute = ((mAmplidute + upStep) < mNewAmplidute) ?
                    mAmplidute + upStep : mNewAmplidute;
        } else {
            float downStep = 0.015f;
            mAmplidute = ((mAmplidute - downStep) > mNewAmplidute) ?
                    mAmplidute - downStep : mNewAmplidute;
        }
    }

    /**
     * 计算公式是 (4/(4+ x^4))^2.5 * 衰减系数 * sin(freq * i - phase).  输入的x使用0-mDensity
     */
    private float getYPos(int x, float att, float phase) {
        return (float) (this.mHeight_2 + att * mAttrCache[x] * Math.sin(WAVE_FREQ * x - phase));
    }

    private float getXPos(int x) {
        return (float) (x * (mTruncateX * 2 + mWidth)) / (float) mDensity - mTruncateX;
    }

    private boolean isCriticalPoint(int i) {
        if (i == 0) {
            return false;
        }
        /*最后一个点是临界点, 或者以mHeight_2为界*/
        return (i >= (mDensity - 1)) ||
                (mLineCacheY[i - 1] <= mHeight_2 && mLineCacheY[i] > mHeight_2) ||
                (mLineCacheY[i - 1] >= mHeight_2 && mLineCacheY[i] < mHeight_2);

    }

    private void resetPaint() {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    private void drawCloseSinRegion(Canvas canvas, int startX, int endX, float regionHeight,
                                    int gradientStartColor, int gradientEndColor) {
        mPath.rewind();
        mPath.moveTo(mXPosMap[startX], mLineCacheY[startX]);

        for (int i = startX; i <= endX; i++) {
            mPath.lineTo(mXPosMap[i], mLineCacheY[i]);
        }

        for (int i = endX; i >= startX; i--) {
            mPath.lineTo(mXPosMap[i], -mLineCacheY[i] + mHeight_2 * 2);
        }
        mPath.close();

        resetPaint();
        float startY = regionHeight + mHeight_2;
        float endY = -regionHeight + mHeight_2;
        LinearGradient gradient1 = new LinearGradient(mXPosMap[startX], startY, mXPosMap[startX], endY,
                gradientStartColor, gradientEndColor, Shader.TileMode.CLAMP);

        if (mDismissFlag && startX == mStartX) {
            LinearGradient gradient2 = new LinearGradient(mXPosMap[startX], startY, mXPosMap[endX], startY,
                    0x00000000, 0xFF000000, Shader.TileMode.CLAMP);
            mPaint.setShader(new ComposeShader(gradient1, gradient2, PorterDuff.Mode.DST_ATOP));
        } else {
            mPaint.setShader(gradient1);
        }

        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mPath, mPaint);

    }

    private void drawSinStroke(Canvas canvas, int lineColor,
                               float lineWidth, boolean isClamp, boolean isMirror, int startX) {
        float lineWidth_2 = lineWidth / 2;
        float xPos, yPos;
        mPathLine.rewind();
        if (mStartX != mDensity) {
//            Log.w(TAG, "carol add " + startX);
        }
        for (int i = startX; i < mDensity; i++) {
            yPos = mLineCacheY[i];
            if (isClamp) {
                if (yPos > mHeight_2) {
                    yPos = yPos - lineWidth_2;
                } else if (yPos < mHeight_2) {
                    yPos = yPos + lineWidth_2;
                }
            }
            if (isMirror) {
                yPos = -yPos + mHeight_2 * 2;
            }

            xPos = getXPos(i);
            if (i == startX) {
                mPathLine.moveTo(xPos, yPos);
            } else {
                mPathLine.lineTo(xPos, yPos);
            }
        }

        resetPaint();
        mPaint.setStrokeWidth(lineWidth);
        mPaint.setStyle(Paint.Style.STROKE);

        if (mDismissFlag && startX < mDensity) {
            LinearGradient gradient = new LinearGradient(getXPos(startX), 0, mWidth, 2 * mHeight_2,
                    lineColor & 0x00FFFFFF, lineColor, Shader.TileMode.CLAMP);
            mPaint.setShader(gradient);
        } else {
            mPaint.setColor(lineColor);
        }

        canvas.drawPath(mPathLine, mPaint);
    }

    private void drawWave(Canvas canvas, float attenuation, float phase,
                          int lineStartColor, int lineEndColor,
                          int gradientStartColor, int gradientEndColor,
                          float lineWidth, boolean isClamp, int startX) {
        resetPaint();
        float att = (this.mMaxHeight * this.mAmplidute) / attenuation;
        float maxWaveHeight = 0;  //记录当前一段波形中的最大高度。 用于设置渐变的最大高度
        for (int i = startX; i < mDensity; i++) {
            mLineCacheY[i] = getYPos(i, att, phase);
            if (Math.abs(maxWaveHeight) < Math.abs(mLineCacheY[i] - mHeight_2)) {
                maxWaveHeight = (mLineCacheY[i] - mHeight_2);
            }
            if (isCriticalPoint(i)) {
                drawCloseSinRegion(canvas, startX, i, maxWaveHeight, gradientStartColor, gradientEndColor);
                startX = i;
                maxWaveHeight = 0;
            }
        }
        drawSinStroke(canvas, lineStartColor, lineWidth, isClamp, false, mStartX);
        drawSinStroke(canvas, lineEndColor, lineWidth, true, true, mStartX);
    }

    private void drawLine(Canvas canvas, float attenuation, int lineColor, int startX) {
        resetPaint();
        for (int i = 0; i < mDensity; i++) {
            //此处优化，利用和line1的相位相同，只需在原基础上缩小即可
            mLineCacheY[i] = attenuation * (mLineCacheY[i] - mHeight_2) + mHeight_2;
        }

        mPath.rewind();
        mPaint.setStrokeWidth(mLineWidth);
        mPaint.setStyle(Paint.Style.STROKE);

        for (int i = startX; i < mDensity; i++) {
            if (i == startX) {
                mPath.moveTo(getXPos(i), mLineCacheY[i]);
            } else {
                mPath.lineTo(getXPos(i), mLineCacheY[i]);
            }
        }

        if (mDismissFlag && startX < mDensity) {
            LinearGradient gradient = new LinearGradient(getXPos(startX), 0, mWidth, 2 * mHeight_2,
                    lineColor & 0x00FFFFFF, lineColor, Shader.TileMode.CLAMP);
            mPaint.setShader(gradient);
        } else {
            mPaint.setColor(lineColor);
            mPaint.setShader(null);
            mPaint.setXfermode(null);
        }

        canvas.drawPath(mPath, mPaint);
    }

    private void doDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        getRealA();
        this.mPhase = (float) ((this.mPhase + Math.PI * mSpeed) % (2 * Math.PI));

        drawWave(canvas, 1, mPhase - 0.65f, line_2_start_color, line_2_end_color,
                region_2_start_color, region_2_end_color, mLineWidth, true, mStartX);
        drawWave(canvas, 1, mPhase, line_1_start_color, line_1_end_color,
                region_1_start_color, region_1_end_color, mLineWidth, false, mStartX);
        drawLine(canvas, 0.25f, line_3_color, mStartX);
        if (mParticleVisible) {
            mParticleAnimator.doDraw(canvas);
        }
    }
}
