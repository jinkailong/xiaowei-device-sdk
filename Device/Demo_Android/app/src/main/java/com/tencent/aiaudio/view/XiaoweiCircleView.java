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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.utils.UIUtils;

public class XiaoweiCircleView extends View {

    private int DEFAULT_BORDER_WIDTH = UIUtils.dip2px(getContext(), 3);
    private int DEFAULT_BORDER_COLOR = Color.BLUE;

    private final Paint mBorderPaint = new Paint();

    private int mBorderColor = DEFAULT_BORDER_COLOR;// 外面绘制一个圆圈
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;
    private int mRotateOffsetX = 0;
    private int mRotateOffsetY = 0;
    private float mRadius;
    private Point mRadiusPoint = new Point();
    private RotateAnimation mRotateAnimation;


    public XiaoweiCircleView(Context context) {
        super(context);
        init(context, null);
    }

    public XiaoweiCircleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public XiaoweiCircleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelRotate();
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XiaoweiCircle);
            mBorderColor = a.getColor(R.styleable.XiaoweiCircle_xc_border_color, mBorderColor);
            mBorderWidth = (int) a.getDimension(R.styleable.XiaoweiCircle_xc_border_width, mBorderWidth);
            mRotateOffsetX = (int) a.getDimension(R.styleable.XiaoweiCircle_xc_rotate_offset_x, mRotateOffsetX);
            mRotateOffsetY = (int) a.getDimension(R.styleable.XiaoweiCircle_xc_rotate_offset_y, mRotateOffsetY);
            a.recycle();
        }

        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setStyle(Paint.Style.STROKE);
    }

    public void setBorderColor(int color) {
        mBorderColor = color;
        mBorderPaint.setColor(mBorderColor);
        invalidate();
    }

    public void setBorderWidth(int width) {
        mBorderWidth = width;
        mRadius = getMeasuredWidth() / 2 - mBorderWidth - 2;
        mBorderPaint.setStrokeWidth(mBorderWidth);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        rotate();
        mRadiusPoint.set(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
        mRadius = getMeasuredWidth() / 2 - mBorderWidth / 2;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mRadiusPoint.x, mRadiusPoint.y, mRadius, mBorderPaint);
    }

    public void rotate() {
        if (mRotateAnimation != null) {
            mRotateAnimation.cancel();
            mRotateAnimation = null;
        }
        if (mRotateOffsetX != 0 || mRotateOffsetY != 0) {
            mRotateAnimation = new RotateAnimation(0, 359, getMeasuredWidth() / 2 + mRotateOffsetX, getMeasuredHeight() / 2 + mRotateOffsetY);
            mRotateAnimation.setRepeatCount(-1);
            mRotateAnimation.setDuration(2000);
            mRotateAnimation.setInterpolator(new LinearInterpolator());
            startAnimation(mRotateAnimation);
        }
    }

    public void cancelRotate() {
        if (mRotateAnimation != null) {
            mRotateAnimation.cancel();
            mRotateAnimation = null;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (getVisibility() == visibility) {
            return;
        }
        if (visibility != VISIBLE) {
            if (mRotateAnimation != null) {
                mRotateAnimation.cancel();
            }
//            if (animator != null) {
//                animator.cancel();
//            }
        } else {
            rotate();
        }
        super.setVisibility(visibility);
    }
}