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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.tencent.xiaowei.util.QLog;
import android.content.Context;
import android.content.res.AssetManager;

/**
 * 这个类主要功能是把assert目录的“libwxvoiceembed.bin”拷贝到app目录下
 * 
 * @author cillinzhang
 * 
 */
class GrammarResource {
	// String TAG = "InnerGrammar";

	private boolean mIsInit = false;

	private String mPath = null;

	private static String mData = "libwxvoiceembed.bin";

	public String getPath() {
		return mPath;
	}

	public String getData() {
		return mData;
	}

	public int init(Context context, String fileName) {
		if (mIsInit)
			return 0;

		mData = fileName;
		QLog.d("WakeupManager", "init by " + fileName);

		String path = context.getFilesDir().getAbsolutePath()
				+ "/wxvoiceembed/";
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}

		path = path + "grammar/";
		file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}

		String name = path + SDKVersion.Ver;
		file = new File(name);
		AssetManager am = context.getAssets();
		if (!file.exists()) {
			File existfile = new File(path);
			File[] childFile = existfile.listFiles();
			if (childFile != null) {
				for (File f : childFile)
					deleteFile(f);
			}
			file.mkdir();
			path = name + "/";
			try {
				copyFileToLocal(am, mData, path, null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
		} else {
			path = name + "/";
			try {
				if (!isFileExists(mData, path)) {
					copyFileToLocal(am, mData, path, null);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return -1;
			}
		}

		mPath = path;
		mIsInit = true;
		return 0;
	}

	private void copyFileToLocal(AssetManager am, String fileName, String path,
			String dirpath) throws IOException {
		InputStream in = am.open(fileName);
		byte[] buf = new byte[2048];
		int byteread = 0;
		String name = path + fileName;
		// Log.d(TAG, name);
		if (dirpath != null) {
			File dir = new File(dirpath);
			if (!dir.exists())
				dir.mkdir();
		}

		File file = new File(name);
		file.createNewFile();
		FileOutputStream fout = new FileOutputStream(file);
		while ((byteread = in.read(buf)) != -1) {
			fout.write(buf, 0, byteread);
		}
		fout.close();
		in.close();
	}

	private boolean isFileExists(String fileName, String path) {
		String name = path + fileName;
		// Log.d(TAG, name);
		File file = new File(name);
		return file.exists();
	}

	private void deleteFile(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}

		if (file.isDirectory()) {
			File[] childFile = file.listFiles();
			if (childFile == null || childFile.length == 0) {
				file.delete();
				return;
			}

			for (File f : childFile) {
				deleteFile(f);
			}
			file.delete();
		}
	}
}