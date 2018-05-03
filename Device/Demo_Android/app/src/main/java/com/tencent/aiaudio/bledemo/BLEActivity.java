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
package com.tencent.aiaudio.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.tencent.aiaudio.activity.base.BaseActivity;
import com.tencent.aiaudio.adapter.CommonListAdapter;
import com.tencent.aiaudio.demo.R;
import com.tencent.aiaudio.utils.UIUtils;
import com.tencent.xiaowei.util.QLog;

import java.util.ArrayList;

public class BLEActivity extends BaseActivity {

    private String TAG = "BLEActivity";
    private CommonListAdapter<BLEDeviceInfo> mAdapter;
    private ListView mListView;
    private BluetoothAdapter mBluetoothAdapter;
    private BLEService.OnBLEEventListener mListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            UIUtils.showToast("设备不支持蓝牙");
            // 发送 设备无蓝牙的通知
            finish();
            return;
        }

        final Button button = (Button) findViewById(R.id.btn_ble_open);
        if (mBluetoothAdapter.isEnabled()) {
            button.setText("关闭");
            if (BLEService.getInstance() != null) {
                BLEDeviceInfo item = BLEService.getInstance().getOperatingDevice();
                QLog.e(TAG, "正在操作 " + item);
            }
        } else {
            button.setText("打开");
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    if (BLEService.getInstance() != null) {
                        BLEService.getInstance().closeBLE();
                    }
                } else {
                    if (BLEService.getInstance() != null) {
                        BLEService.getInstance().openBLE();
                    }
                }
            }
        });

        final Button btnDis = (Button) findViewById(R.id.btn_ble_dis);
        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBluetoothAdapter.isEnabled()) {
                    if (BLEService.getInstance() != null) {
                        BLEService.getInstance().startDiscovery();
                    }
                }
            }
        });


        mListView = (ListView) findViewById(R.id.lv);
        mAdapter = new CommonListAdapter<BLEDeviceInfo>() {
            @Override
            protected View initListCell(int position, View convertView, ViewGroup parent) {
                Log.e(TAG, mAdapter.getItem(position).toString());
                Holder holder;
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.item_ble, parent, false);
                    holder = new Holder();
                    holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                    holder.tvAddress = (TextView) convertView.findViewById(R.id.tv_address);
                    holder.tvState = (TextView) convertView.findViewById(R.id.tv_state);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.tvName.setText(mAdapter.getItem(position).name);
                holder.tvAddress.setText(mAdapter.getItem(position).address);
                switch (mAdapter.getItem(position).state) {
                    case BLEDeviceInfo.STATE_BOND_BONDED:
                        holder.tvState.setText("已配对");
                        break;
                    case BLEDeviceInfo.STATE_BOND_NONE:
                        holder.tvState.setText("");
                        break;
                    case BLEDeviceInfo.STATE_CONNECTED:
                        holder.tvState.setText("已连接");
                        break;
                    case BLEDeviceInfo.STATE_BOND_BONDING:
                        holder.tvState.setText("正在配对");
                        break;
                    case BLEDeviceInfo.STATE_CONNECTING:
                        holder.tvState.setText("正在连接");
                        break;
                }

                return convertView;
            }

            class Holder {
                TextView tvName;
                TextView tvAddress;
                TextView tvState;
            }
        };
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String address = mAdapter.getItem(position).address;
                switch (mAdapter.getItem(position).state) {
                    case BLEDeviceInfo.STATE_BOND_BONDED:
                        try {
                            if (BLEService.getInstance() != null) {
                                BLEService.getInstance().connect(mBluetoothAdapter.getRemoteDevice(address));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case BLEDeviceInfo.STATE_BOND_NONE:
                        try {
                            if (BLEService.getInstance() != null) {
                                BLEService.getInstance().createBond(mBluetoothAdapter.getRemoteDevice(address));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case BLEDeviceInfo.STATE_CONNECTED:
                        try {
                            if (BLEService.getInstance() != null) {
                                BLEService.getInstance().removeBond(mBluetoothAdapter.getRemoteDevice(address));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }

            }
        });

        mListener = new BLEService.OnBLEEventListener() {
            @Override
            public void onOpen() {
                UIUtils.showToast("蓝牙已打开");
                button.setText("关闭");
            }

            @Override
            public void onClose() {
                UIUtils.showToast("蓝牙已关闭");
                button.setText("打开");
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDiscoveryStart() {
                QLog.e(TAG, "onDiscoveryStart");
                btnDis.setText("正在扫描");
                mAdapter.clear();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDiscovery(BLEDeviceInfo item) {
                mAdapter.add(item);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDiscoveryStop(ArrayList<BLEDeviceInfo> items) {
                QLog.e(TAG, "onDiscoveryStop");
                btnDis.setText("扫描");

            }

            @Override
            public void onConnecting(BLEDeviceInfo item) {
                QLog.e(TAG, "onConnecting");
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onConnected(boolean success, BLEDeviceInfo item) {
                QLog.e(TAG, "onConnected");
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onBonding(BLEDeviceInfo item) {
                QLog.e(TAG, "onBonding");
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onBond(BLEDeviceInfo item) {
                QLog.e(TAG, "onBond");
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onUnBond(BLEDeviceInfo item) {
                QLog.e(TAG, "onUnBond");
                mAdapter.notifyDataSetChanged();
            }
        };

        if (BLEService.getInstance() != null) {
            BLEService.getInstance().addOnBLEEventListener(mListener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BLEService.getInstance() != null) {
            BLEService.getInstance().removeOnBLEEventListener(mListener);
        }
    }
}
