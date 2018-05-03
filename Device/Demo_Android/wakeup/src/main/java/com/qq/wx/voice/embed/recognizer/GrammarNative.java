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
package com.qq.wx.voice.embed.recognizer;

public class GrammarNative {
	static {
		System.loadLibrary("wxvoiceembed");
	}

	/**
	 * 初始化
	 * 
	 * @param path
	 *            libwxvoiceembed.bin所在绝对路径
	 * @param name
	 *            填libwxvoiceembed.bin，如果“libwxvoiceembed.bin”被重命名则填新名称
	 * @param personNameList
	 *            唤醒场景没用填null
	 * @return [-1,0] -1:失败，一般为路径错误 ;0:成功.
	 */
	public static native int init(byte[] path, byte[] name,
			byte[] personNameList);

	/**
	 * 
	 * @param nKeywordSetIndex
	 *            关键词集合序号，约定好的
	 * @return [-1,0] -1:失败;0:成功.
	 */
	public static native int setKeywordSetIndex(int nKeywordSetIndex);

	/**
	 * 告知引擎新一轮识别要开始了
	 * 
	 * @return [-1,0] -1:失败 ;0:成功.
	 */
	public static native int begin();

	/**
	 * 传入语音流
	 * 
	 * @param wav
	 *            语音数组
	 * @param len
	 *            语音长度
	 * @return [-1,0,1] -1:失败 ;0:成功;1:唤醒;2:半词标志.
	 */
	public static native int recognize(byte[] wav, int len);

	/**
	 * 如果尚未唤醒，语音传完了，告知引擎没得传了
	 * 
	 * @return [-1,0] -1:失败 ;0:成功.
	 */
	public static native int end();

	/**
	 * 获取识别结果
	 * 
	 * @param grammar
	 *            通过Grammar从ndk层回调把识别结果送回
	 * 
	 * @return [-1,0] -1:失败 ;0:成功.
	 */
	public static native int getResult(Grammar grammar);

	/**
	 * 销毁语音识别
	 * 
	 * @return [-1,0] -1:失败 ;0:成功.
	 */
	public static native int destroy();

	/**
	 * 获取版本号
	 *
	 * @return [-1,0] -1:失败 ;0:成功.
	 */
	public static native int getVersion(Grammar grammar);
}