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
package com.tencent.aiaudio;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * PID相关信息
 */
public class PidInfoConfig {
    public static long pid;
    public static String sn;
    public static String licence;
    public static String srvPubKey;

    public static boolean init() {
        getPidAndPubKey();
        getSN();
        if (pid > 0 && sn != null && sn.length() == 16) {
            return true;
        }
        return false;
    }

    private static void getPidAndPubKey() {
        File file = new File(Environment.getExternalStorageDirectory() + "/pid.txt");
        try {
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String sPid = br.readLine();
                String sPubKey = br.readLine();
                pid = Integer.valueOf(sPid);
                srvPubKey = sPubKey;
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean getSN() {
        File file = new File(Environment.getExternalStorageDirectory() + "/sn.txt");
        try {
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String sSn = br.readLine();
                if (sSn.length() == 16) {
                    sn = sSn;
                    String sLicense = br.readLine();
                    if (sLicense.length() > 0) {
                        licence = sLicense;
                        br.close();
                        return true;
                    }
                }
                br.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
