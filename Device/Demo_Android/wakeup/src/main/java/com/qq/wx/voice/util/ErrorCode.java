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
package com.qq.wx.voice.util;

public class ErrorCode {
	/**************
	 * main error *
	 **************/
	/**
	 * 未初始化
	 */
	public static final int WX_VOICE_ERROR_UNINIT = -101;

	/**
	 * 重复开始
	 */
	public static final int WX_VOICE_ERROR_RESTART = -102;

	/*****************
	 * grammar error *
	 *****************/
	/**
	 * ERROR_GRAMMAR_INIT
	 */
	public static final int ERROR_GRAMMAR_INIT = -201;

	/****************
	 * record error *
	 ****************/
	/**
	 * 语音识别: 录音设备错误(recording equipment failure)
	 */
	public static final int WX_VOICE_RECORD_ERROR_STATE = -301;

	/**
	 * 语音识别: 录音开始失败(record start failed)
	 */
	public static final int WX_VOICE_RECORD_ERROR_START_RECORDING = -302;

	/**
	 * 话筒成功打开，但是无法读取到数据，在有些手机如：华为P8上禁用手机话筒后就会出现这个异常
	 */
	public static final int WX_VOICE_RECORD_ERROR_INVALID_SIZE = -303;

	/**
	 * vad初始化失败
	 */
	public static final int WX_VOICE_RECORD_ERROR_VAD_INIT = -304;

	/**
	 * 录音缓冲空间满了
	 */
	public static final int WX_VOICE_RECORD_ERROR_LIST_FULL = -305;

	/**
	 * 语音识别: 录音结束失败(record stop failed)
	 */
	public static final int WX_VOICE_RECORD_ERROR_STOP_RECORDING = -306;

	/**
	 * 录音设备故障/AudioRecord.ERROR_INVALID_OPERATION
	 */
	public static final int WX_VOICE_RECORD_ERROR_INVALID_OPERATION = 10131;

	/**
	 * 录音设备故障/AudioRecord.ERROR_BAD_VALUE
	 */
	public static final int WX_VOICE_RECORD_ERROR_BAD_VALUE = 10132;
}
