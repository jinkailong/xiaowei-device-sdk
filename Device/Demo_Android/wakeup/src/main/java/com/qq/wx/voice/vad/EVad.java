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
package com.qq.wx.voice.vad;

public class EVad {
	private long handle = 0;

	private EVadNative evad_inst = new EVadNative();

	public int Init(int sample_rate, int sil_time, float s_n_ration, int bwin,
			int bconfirm) {
		handle = evad_inst.Init(sample_rate, sil_time, s_n_ration, bwin,
				bconfirm);
		if (handle == 0) {
			return EVadNative.VAD_ERROR;
		}
		return EVadNative.VAD_SUCCESS;
	}

	public int Reset() {
		if (handle == 0) {
			return EVadNative.VAD_ERROR;
		}
		return evad_inst.Reset(handle);
	}

	public int AddData(byte[] data, int dsize) {
		short[] shortData = new short[dsize / 2];
		for (int j = 0; j < dsize; j += 2)
			shortData[j / 2] = (short) (((data[j + 1] & 0xff) << 8) | (data[j] & 0xff));
		return AddData(shortData, dsize / 2);
	}

	public int AddData(short[] data, int dsize) {
		if (handle == 0) {
			return EVadNative.VAD_ERROR;
		}
		return evad_inst.AddData(handle, data, dsize);
	}

	public int Release() {
		if (handle == 0) {
			return EVadNative.VAD_ERROR;
		}
		return evad_inst.Release(handle);
	}
}
