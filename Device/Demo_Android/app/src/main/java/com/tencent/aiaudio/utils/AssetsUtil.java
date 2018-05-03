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
package com.tencent.aiaudio.utils;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class AssetsUtil {

    private static final String TAG = "AssetsUtil";
    private static String assetsNames[] = {"please_speak.pcm", "network_connected.pcm", "network_disconnected.pcm","online.pcm","offline.pcm","hi.pcm",};
    private static ConcurrentHashMap<String, byte[]> mRings = new ConcurrentHashMap<>(assetsNames.length);

    private static Context mContext;

    public static void init(Context context) {
        if (context == null) {
            return;
        }
        if (mContext == null) {
            mContext = context.getApplicationContext();
            for (String fileName : assetsNames) {
                initBytes(fileName);
            }
        }
    }

    private static boolean initBytes(String fileName) {
        if (mContext == null) {
            Log.e(TAG, "context is null.");
            return false;
        }
        try {
            InputStream is = mContext.getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            mRings.put(fileName, buffer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获得assets里面的指定文件的内容
     *
     * @param fileName
     * @return
     */
    public static byte[] getRing(String fileName) {
        byte[] buffer = mRings.get(fileName);
        if (buffer == null) {
            if (initBytes(fileName)) {
                buffer = mRings.get(fileName);
            }
        }
        return buffer;
    }
}
