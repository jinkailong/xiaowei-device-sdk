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

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioManager;

public class ParticleAnimator {
	private static final String TAG = "ParticleAnimator"; 
	
	private final static float mUpStep = 0.03f;
	private final static float mDownStep = 0.008f;
	
	private static final float DEFAULT_AMPLIDUTE = 0.1f;
	
	private static final int MAX_RANDOM_X_TRANS = 7;
	
	private static final int RANDOM_X_TRANS_FREQ = 8;
	
	private ParticleSet mSet;
	
	private float mAmplitude = DEFAULT_AMPLIDUTE;
	
	private float mNewAmplidute = DEFAULT_AMPLIDUTE;

	private Context mContext;
	
	public ParticleAnimator(Context ctx) {
		mContext = ctx;
		mSet = new ParticleSet(mContext);
	}
	
	public void setNoise(float a) {
		this.mNewAmplidute = Math.max(Math.min(a, 1.0f), DEFAULT_AMPLIDUTE);
	}
	
	private void getRealA() {
    	if (mAmplitude < mNewAmplidute ) {
    		mAmplitude = ((mAmplitude + mUpStep) < mNewAmplidute) ? 
    				mAmplitude + mUpStep : mNewAmplidute;
    	} else {
    		mAmplitude = ((mAmplitude - mDownStep) > mNewAmplidute) ? 
    				mAmplitude - mDownStep : mNewAmplidute;
    	}
    }
	
	private float getRandomSpeed(float randomSpeedSeed) {
		return (float) (Math.random() * (randomSpeedSeed * 2) - randomSpeedSeed);
	}
	
	private float getRandomXDiff() {
		return (float) (Math.random() * (MAX_RANDOM_X_TRANS * 2) - MAX_RANDOM_X_TRANS);
	}
	
	private float getRealXDiff(float width, float diffX) {
		float x = getRandomXDiff();
		
		if (diffX + x <= 0 || diffX + x >= width) {
			return -x;
		} else {
			return x;
		}
	}
	
	private void getDismissAmplitude(ParticleSet.Particle particle) {
		if (particle.mCurrentBlinkAmplitude <= 0) {
			particle.mBlinkmDirection = true;
		} else if (particle.mCurrentBlinkAmplitude >= particle.mMaxBlinkAmplitude) {
			particle.mBlinkmDirection = false;
		}
		
		if (particle.mBlinkmDirection) {
			particle.mCurrentBlinkAmplitude += particle.mBlinkStep * Math.random();
		} else {
			particle.mCurrentBlinkAmplitude -= particle.mBlinkStep * Math.random();
		}
	}
	
	private float getAlpha(ParticleSet.Particle particle) {
		float alpha = 0;
		if (particle.mOpacityFactor < 0) {
			alpha = particle.mMaxOpacity;
		} else {
			float min = 0;
			float max = 1;
			if (particle.mBlink) {
				min = particle.mMinBlinkAmplitude;
			}
			
			if (particle.mBlink && mAmplitude <= particle.mMaxBlinkAmplitude) {
				if (!particle.mIsInited) {
					particle.mIsInited = true;
					particle.mCurrentBlinkAmplitude = mAmplitude;
				}
				getDismissAmplitude(particle);
				alpha = particle.mOpacityFactor * particle.mMaxOpacity *
						(particle.mCurrentBlinkAmplitude - min) / (max - min);
			} else {
				particle.mIsInited = false;
				
				alpha = particle.mOpacityFactor * particle.mMaxOpacity *
						(mAmplitude - min) / (max - min);
			}

			if (alpha > particle.mMaxOpacity) {
				alpha = particle.mMaxOpacity;
			}
		}
		
		if (alpha > 1.0f) {
			alpha = 1.0f;
		} else if (alpha < 0f) {
			alpha = 0f;
		}
		alpha *= ParticleSet.MAX_ALPHA;
		return alpha;
	}
	
	private void drawParticle(Canvas canvas, ParticleSet.Particle particle) {
		float speedY = particle.mSpeedY * mAmplitude / 2f + particle.mSpeedY;
		particle.mPhaseY = (float) ((particle.mPhaseY + Math.PI * (speedY + getRandomSpeed(speedY / 1.2f))) % (2 * Math.PI));
		
		float aY = particle.mFactorY > 0 ? particle.mFactorY * mAmplitude : 1.0f;
		float diffY = (float) (particle.mRangeActivity.height() * aY * (Math.sin(particle.mPhaseY) + 1.0f) / 2.0f);
		float y = particle.mDirection ? particle.mRangeActivity.bottom - Math.abs(diffY) : particle.mRangeActivity.top + Math.abs(diffY);

		float speedX = particle.mSpeedX * mAmplitude / 3f + particle.mSpeedX;
		particle.mPhaseX = (float) ((particle.mPhaseX + Math.PI * (speedX + getRandomSpeed(speedX / 1.2f)) ) % (2 * Math.PI));
		
		float diffX = (float) (particle.mRangeActivity.width() * mAmplitude * (Math.sin(particle.mPhaseX) + 1.0f) / 2.0f);
		
		if (particle.mWithRandomX) {
			
			if (particle.mRandomXCount >= RANDOM_X_TRANS_FREQ) {
				particle.mRandomXCount = 0;
				particle.mLastRandomX = mAmplitude * getRealXDiff(particle.mRangeActivity.width(), Math.abs(diffX));
			}
			particle.mRandomXCount ++;
			
			diffX += particle.mLastRandomX;
		}
		
		float x = particle.mDirection ? particle.mRangeActivity.right - Math.abs(diffX) : particle.mRangeActivity.left + Math.abs(diffX);

		int alpha = (int) (getAlpha(particle) * 255.0f);

		particle.mHexagon.setParams((int)x, (int)y, particle.mRadius, particle.color, alpha);
		particle.mHexagon.draw(canvas);
	}
	
	public void doDraw(Canvas canvas) {
		List<ParticleSet.Particle> particles = mSet.getSet();
		getRealA();
		if (particles == null) {
			return ;
		}
		int len = particles.size();
		for (int i = 0; i < len; i++) {
			drawParticle(canvas, particles.get(i));
		}
		
	}
}
