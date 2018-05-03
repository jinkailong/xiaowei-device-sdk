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

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.RectF;
import android.util.Log;

import com.tencent.aiaudio.demo.R;

public class ParticleSet {
	private static final String TAG = "ParticleSet";
	public static final float MAX_ALPHA = 0.6f;

	private List<Particle> mSet;
	private Context mContext;
	
	public ParticleSet(Context ctx) {
		mContext = ctx;
		init();
	}
	
	private void init() {
		mSet = new ArrayList<Particle>();
		float activityWidth = 0;
		float activityHeight = 0;
		float activityStartX = 0;
		float activityStartY = 0;
		
		float waveMaxHeight = mContext.getResources().getDimensionPixelSize(R.dimen.wave_view_sin_max_height) * 2.0f;
		
		/*******************1***********************/
		Particle particle = new Particle();
		particle.mDirection = true;
		activityWidth = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_1_wdith);
		activityHeight = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_1_height);
		activityStartX = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_1_x);
		activityStartY = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_1_y);

		particle.mRangeActivity = new RectF(activityStartX, activityStartY - activityHeight, 
				activityStartX + activityWidth, activityStartY);
		particle.mRadius = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_1_radius);


		particle.color = mContext.getResources().getColor(R.color.white);
		particle.mOpacityFactor = 0.2f;
		particle.mMaxOpacity = waveMaxHeight / 100.0f;
		particle.mBlink = true;
		particle.mMaxBlinkAmplitude = 0.13f;
		particle.mMinBlinkAmplitude = 0.02f;
		particle.mBlinkStep = 0.0015f;
		
		particle.mSpeedY = 0.028f;
		particle.mPhaseY = 0.1f;
		particle.mFactorY = -1;
		
		particle.mSpeedX = 0.002f;
		particle.mPhaseX = 0.1f;
		particle.mFactorX = -1;
		mSet.add(particle);		
		

		/*******************2***********************/
		particle = new Particle();
		particle.mDirection = false;
		activityWidth = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_2_wdith);
		activityHeight = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_2_height);
		activityStartX = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_2_x);
		activityStartY = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_2_y);

		particle.mRangeActivity = new RectF(activityStartX, activityStartY , 
				activityStartX + activityWidth, activityStartY + activityHeight);
		
		Log.d(TAG, "particle.mRangeActivity " + particle.mRangeActivity.height() + ", activityHeight " + activityHeight);
		particle.mRadius = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_2_radius);

		particle.color = mContext.getResources().getColor(R.color.particle_green_color);
		particle.mOpacityFactor = 1.8f;
		particle.mMaxOpacity = 0.6f;
		particle.mBlink = true;
		particle.mMaxBlinkAmplitude = 0.10f;
		particle.mMinBlinkAmplitude = 0.017f;
		particle.mBlinkStep = 0.002f;
		
		particle.mSpeedY = 0.025f;
		particle.mPhaseY = 1.1f;
		particle.mFactorY = 1.0f;
		
		particle.mSpeedX = 0.008f;
		particle.mPhaseX = 1.1f;
		particle.mFactorX = -1;
		particle.mWithRandomX = false;
		mSet.add(particle);	
		
		
		/*******************3***********************/
		particle = new Particle();
		particle.mDirection = true;
		activityWidth = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_3_wdith);
		activityHeight = waveMaxHeight * 0.3f;
		activityStartX = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_3_x);
		activityStartY = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_3_y);

		particle.mRangeActivity = new RectF(activityStartX, activityStartY - activityHeight, 
				activityStartX + activityWidth, activityStartY );
		particle.mRadius = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_3_radius);

		particle.color = mContext.getResources().getColor(R.color.white);
		particle.mOpacityFactor = 0.5f;
		particle.mMaxOpacity = waveMaxHeight / 100.0f;
		particle.mBlink = true;
		particle.mMaxBlinkAmplitude = 0.14f;
		particle.mMinBlinkAmplitude = 0.005f;
		particle.mBlinkStep = 0.0018f;
		
		particle.mSpeedY = 0.031f;
		particle.mPhaseY = 1.1f;
		particle.mFactorY = 1.2f;
		
		particle.mSpeedX = 0.008f;
		particle.mPhaseX = 5.1f;
		particle.mFactorX = -1;
		mSet.add(particle);		
		
		/*******************4***********************/
		particle = new Particle();
		particle.mDirection = true;
		activityWidth = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_4_wdith);
		activityHeight = waveMaxHeight * 0.8f;
		activityStartX = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_4_x);
		activityStartY = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_4_y);

		particle.mRangeActivity = new RectF(activityStartX, activityStartY - activityHeight, 
				activityStartX + activityWidth, activityStartY);
		particle.mRadius = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_4_radius);

		particle.color = mContext.getResources().getColor(R.color.particle_green_color);
		particle.mOpacityFactor = 1;
		particle.mMaxOpacity = waveMaxHeight / 100.0f;
		particle.mBlink = true;
		particle.mMaxBlinkAmplitude = 0.10f;
		particle.mMinBlinkAmplitude = 0.01f;
		particle.mBlinkStep = 0.0010f;
		
		
		particle.mSpeedY = 0.035f;
		particle.mPhaseY = 2.1f;
		particle.mFactorY = 1;
		
		particle.mSpeedX = 0.025f;
		particle.mPhaseX = 5.1f;
		particle.mFactorX = -1;
		mSet.add(particle);		
		
		
		/*******************5***********************/
		particle = new Particle();
		particle.mDirection = false;
		activityWidth = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_5_wdith);
		activityHeight = waveMaxHeight * 0.6f;
		activityStartX = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_5_x);
		activityStartY = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_5_y);

		particle.mRangeActivity = new RectF(activityStartX, activityStartY , 
				activityStartX + activityWidth, activityStartY + activityHeight);
		particle.mRadius = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_5_radius);

		particle.color = mContext.getResources().getColor(R.color.white);
		particle.mOpacityFactor = 0.2f;
		particle.mMaxOpacity = waveMaxHeight / 100.0f;
		particle.mBlink = true;
		particle.mMaxBlinkAmplitude = 0.12f;
		particle.mMinBlinkAmplitude = 0.033f;
		particle.mBlinkStep = 0.0016f;
		
		
		particle.mSpeedY = 0.023f;
		particle.mPhaseY = 1.1f;
		particle.mFactorY = 1;
		
		particle.mSpeedX = 0.045f;
		particle.mPhaseX = 2.1f;
		particle.mFactorX = -1;
		mSet.add(particle);

		/*******************6***********************/
		particle = new Particle();
		particle.mDirection = true;
		activityWidth = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_6_wdith);
		activityHeight = waveMaxHeight * 0.6f;
		activityStartX = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_6_x);
		activityStartY = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_6_y);

		particle.mRangeActivity = new RectF(activityStartX, activityStartY - activityHeight, 
				activityStartX + activityWidth, activityStartY);
		particle.mRadius = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_6_radius);

		particle.color = mContext.getResources().getColor(R.color.particle_green_color);
		particle.mOpacityFactor = 0.35f;
		particle.mMaxOpacity = waveMaxHeight / 100.0f;
		particle.mBlink = true;
		particle.mMaxBlinkAmplitude = 0.15f;
		particle.mMinBlinkAmplitude = 0.03f;
		particle.mBlinkStep = 0.0013f;
		
		particle.mSpeedY = 0.032f;
		particle.mPhaseY = 1.1f;
		particle.mFactorY = 1;
		
		particle.mSpeedX = 0.008f;
		particle.mPhaseX = 2.1f;
		particle.mFactorX = -1;
		mSet.add(particle);	
		
		
		particle = new Particle();
		particle.mDirection = false;
		activityWidth = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_7_wdith);
		activityHeight = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_7_height);
		activityStartX = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_7_x);
		activityStartY = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_7_y);

		particle.mRangeActivity = new RectF(activityStartX, activityStartY , 
				activityStartX + activityWidth, activityStartY + activityHeight);
		particle.mRadius = mContext.getResources().getDimensionPixelSize(R.dimen.wave_particale_7_radius);

		particle.color = mContext.getResources().getColor(R.color.white);
		particle.mOpacityFactor = -1;
		particle.mMaxOpacity = 0.2f;
		
		particle.mSpeedY = 0.02f;
		particle.mPhaseY = 0.1f;
		particle.mFactorY = -1;
		
		particle.mSpeedX = 0.013f;
		particle.mPhaseX = 0.1f;
		particle.mFactorX = -1;
		mSet.add(particle);	
	}
	
	
	public List<Particle> getSet() {
		return mSet;
	}
	
	public class Particle {
		boolean mDirection;
		RectF mRangeActivity;
		
		float mOpacityFactor;
		float mMaxOpacity;
		int color;

		boolean mBlink = false;
		float mMaxBlinkAmplitude;
		float mMinBlinkAmplitude;
		float mBlinkStep;
		float mCurrentBlinkAmplitude;
		boolean mBlinkmDirection;
		boolean mIsInited;
		
		float mRadius;
		float mSpeedY;
		float mPhaseY;
		float mFactorY;
		
		float mSpeedX;
		float mPhaseX;
		float mFactorX;
		boolean mWithRandomX = false;
		float mRandomXCount = 0;
		float mLastRandomX = 0;

		HexagonView mHexagon = new HexagonView();
	}
}
