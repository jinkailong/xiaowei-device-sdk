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

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import com.tencent.aiaudio.utils.UIUtils;

public class VoiceView extends View {
    static final String TAG = "VoiceView";
    Paint mPaint;
    private final float lineWidth = UIUtils.dip2px(this.getContext(), 2);
    private int minHeight = UIUtils.dip2px(this.getContext(), 5);
    private int maxHeight = 28;
    private boolean mPlaying;
    Line[] mLines;

    private ValueAnimator anim;

    public VoiceView(Context context) {
        this(context, null);
    }

    public VoiceView(Context context, AttributeSet attributes) {
        super(context, attributes);
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#009FFF"));
        mPaint.setStrokeWidth(lineWidth);                        //设置线宽
        mLines = new Line[4];
        for (int i = 0; i < mLines.length; i++)
            mLines[i] = new Line();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        maxHeight = height;
        int width = (int) (getMeasuredWidth() - lineWidth * 4 - UIUtils.dip2px(this.getContext(), 1));
//        Log.i(TAG, "measuredwidth: " + getMeasuredWidth());
//        Log.i(TAG, "linewidth: " + mPaint.getStrokeWidth());
//        Log.i(TAG, "width: " + width);
        int lineSpace = width / 3;
        int lineHeight = UIUtils.dip2px(this.getContext(), 5);
        mLines[0].startX = UIUtils.dip2px(this.getContext(), 1);
        mLines[0].startY = (height - lineHeight) / 2;
        mLines[0].endX = mLines[0].startX;
        mLines[0].endY = mLines[0].startY + lineHeight;
        mLines[0].stretch = true;

        lineHeight = UIUtils.dip2px(this.getContext(), 10);
        mLines[1].startX = lineWidth + mLines[0].startX + lineSpace;
        mLines[1].startY = (height - lineHeight) / 2;
        mLines[1].endX = mLines[1].startX;
        mLines[1].endY = mLines[1].startY + lineHeight;
        mLines[0].stretch = true;

        mLines[2].startX = lineWidth + mLines[1].startX + lineSpace;
        mLines[2].startY = 0;
        mLines[2].endX = mLines[2].startX;
        mLines[2].endY = height;
        mLines[0].stretch = false;

        lineHeight = UIUtils.dip2px(this.getContext(), 5);
        mLines[3].startX = lineWidth + mLines[2].startX + lineSpace;
        mLines[3].startY = (height - lineHeight) / 2;
        mLines[3].endX = mLines[3].startX;
        mLines[3].endY = mLines[3].startY + lineHeight;
        mLines[0].stretch = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);                                            //设置背景颜色
        for (Line line : mLines)
            canvas.drawLine(line.startX, line.startY, line.endX, line.endY, mPaint);    //绘制四条直线
    }

    private void startAnimation() {
        anim = ValueAnimator.ofObject(new LineEvaluator(), mLines, null);               //设置属性动画
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLines = (Line[]) animation.getAnimatedValue();
                invalidate();
            }
        });
        anim.setInterpolator(new LinearInterpolator());                                 //动画匀速变化(fraction参数均匀变化)
        anim.setDuration(1000);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();
    }

    private class LineEvaluator implements TypeEvaluator {
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            Line[] lines = (Line[]) startValue;
            for (Line line : lines) {
                float currentHeight = line.endY - line.startY;
                float nextHeight;
                if (Math.abs(currentHeight - maxHeight) < 0.1)          //线段已经伸展到最大值,应变短
                {
                    line.stretch = false;
                } else if (Math.abs(currentHeight - minHeight) < 0.1)     //线段已经缩短到最小值,应变长
                {
                    line.stretch = true;
                }
                if (line.stretch)
                    nextHeight = currentHeight + Math.abs(maxHeight - currentHeight) * 0.4f;
                else
                    nextHeight = currentHeight - Math.abs(currentHeight - minHeight) * 0.4f;
                line.startY = (maxHeight - nextHeight) / 2;
                line.endY = line.startY + nextHeight;
            }
            return lines;
        }
    }

    private class Line {
        float startX;            //线段横坐标
        float endX;
        float startY;
        float endY;
        boolean stretch;         //伸长(true)或缩短(false)
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            startAnim();
        } else {
            stopAnim();
        }
    }

    /**
     * 开启动画效果
     */
    private void startAnim() {
        if (!mPlaying) {
            startAnimation();
            mPlaying = true;
        }
    }

    private void stopAnim() {
        if (mPlaying) {
            anim.cancel();
            mPlaying = false;
        }
    }
}
