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
package com.tencent.aiaudio.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.tencent.xiaowei.info.XWBinderRemark;
import com.tencent.xiaowei.sdk.XWDeviceBaseManager;
import com.tencent.xiaowei.info.XWBinderInfo;
import com.tencent.xiaowei.util.QLog;

import java.util.ArrayList;

public class BinderActivity extends ContactActivity {
    static final String TAG = "BinderActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        XWDeviceBaseManager.registerBinderRemarkChangeListener(new XWDeviceBaseManager.IGetBinderRemarkListCallback(){
            @Override
            public void onResult(XWBinderRemark[] binderRemarks) {
                ArrayList<XWBinderInfo> arrayList = XWDeviceBaseManager.getBinderList();
                for (XWBinderRemark remark: binderRemarks) {
                    for (XWBinderInfo binderInfo: arrayList) {
                        if (binderInfo.tinyID == remark.tinyid) {
                            binderInfo.remark = remark.remark;
                        }
                    }
                }

                if (arrayList.isEmpty()) {
                    return;
                }

                mAdapter.clear();
                XWBinderInfo[] binderArray = new XWBinderInfo[arrayList.size()];
                binderArray = arrayList.toArray(binderArray);
                mAdapter.addAll(binderArray);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void updateData() {
        // 这里XWSDKJNI.getInstance().getDeviceBinderList可以同步取到设备的绑定者，其中可以拿到QQ体系的备注（昵称）
        // getBinderRemarkList是去异步的取绑定者的小微体系的"呼叫备注名"
        XWDeviceBaseManager.getBinderRemarkList(new XWDeviceBaseManager.IGetBinderRemarkListCallback() {
            @Override
            public void onResult(XWBinderRemark[] binderRemarks) {
                ArrayList<XWBinderInfo> arrayList = XWDeviceBaseManager.getBinderList();
                for (XWBinderRemark remark: binderRemarks) {
                    for (XWBinderInfo binderInfo: arrayList) {
                        if (binderInfo.tinyID == remark.tinyid) {
                            binderInfo.remark = remark.remark;
                        }
                    }
                }

                if (arrayList.isEmpty()) {
                    return;
                }

                mAdapter.clear();
                XWBinderInfo[] binderArray = new XWBinderInfo[arrayList.size()];
                binderArray = arrayList.toArray(binderArray);
                mAdapter.addAll(binderArray);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

}
