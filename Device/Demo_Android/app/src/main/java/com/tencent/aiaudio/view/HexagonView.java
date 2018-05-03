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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * 六边形绘制
 */
public class HexagonView {
    private static final float SQRT_3_4 = 0.866025f;
    private float mRadius;
    private int mX;
    private int mY;
    private int mColor;
    private int mAlpha = 255;

    private float mPointX[] = new float[6];
    private float mPointY[] = new float[6];

    private Path mPath;
    private Paint mPaint;

    public HexagonView() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
    }

    public void draw(Canvas canvas) {
        mPointX[0] = -mRadius;
        mPointY[0] = 0f;

        mPointX[1] = -mRadius * 0.5f;
        mPointY[1] = -mRadius * SQRT_3_4;

        mPointX[2] = mRadius * 0.5f;
        mPointY[2] = -mRadius * SQRT_3_4;

        mPointX[3] = mRadius;
        mPointY[3] = 0f;

        mPointX[4] = mRadius * 0.5f;
        mPointY[4] = mRadius * SQRT_3_4;

        mPointX[5] = -mRadius * 0.5f;
        mPointY[5] = mRadius * SQRT_3_4;

        mPath.rewind();
        for (int i = 0; i < mPointX.length; i++) {
            if (i == 0) {
                mPath.moveTo(mPointX[0], mPointY[0]);
            } else {
                mPath.lineTo(mPointX[i], mPointY[i]);
            }
        }
        mPath.close();
        mPaint.setColor(mColor);
        mPaint.setAlpha(mAlpha);

        canvas.save();
        canvas.rotate(90, 0, 0);
        canvas.translate(mY, -mX);
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
    }

    public void setParams(int x, int y, float radius, int color, int alpha) {
        mX = x;
        mY = y;
        mColor = color;
        mRadius = radius;
        mAlpha = alpha;
    }
}
