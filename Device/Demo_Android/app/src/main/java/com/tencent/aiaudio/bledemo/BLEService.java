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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.tencent.aiaudio.utils.BLEUtil;
import com.tencent.aiaudio.utils.UIUtils;
import com.tencent.xiaowei.util.QLog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class BLEService extends Service {
    private static final String TAG = "BLEService";
    private static BLEService mBLEService;
    private BroadcastReceiver mReceiver;

    private HashMap<String, BLEDeviceInfo> mMap = new HashMap<>();
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<OnBLEEventListener> mOnBLEEventListener = new ArrayList<>();
    private BLEDeviceInfo mOperatingDevice;
    private boolean isDiscovering;

    public static BLEService getInstance() {
        return mBLEService;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBLEService = this;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        QLog.e(TAG, "init BLEService");

        initBondDevices();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);  //发现新设备
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);  //绑定状态改变
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);  //开始扫描
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);  //结束扫描
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);  //连接状态改变
        filter.addAction(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);  //连接状态改变
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);  //蓝牙开关状态改变
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);

        mReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.e(TAG, action);
                switch (action) {
                    case BluetoothDevice.ACTION_FOUND: {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device == null) {
                            return;
                        }
                        if (TextUtils.isEmpty(device.getName())) {
                            return;
                        }
                        // 必须支持音频播放的才继续
                        BluetoothClass bc = device.getBluetoothClass();
                        if (bc == null) {
                            return;
                        }
                        int major = bc.getMajorDeviceClass();
                        if ((major & BluetoothClass.Device.Major.AUDIO_VIDEO) != BluetoothClass.Device.Major.AUDIO_VIDEO) {
                            return;
                        }
                        BLEDeviceInfo item = new BLEDeviceInfo();
                        item.address = device.getAddress();
                        item.name = device.getName();
                        item.state = device.getBondState();
                        item.major = major;
                        if (!mMap.containsKey(device.getAddress())) {
                            mMap.put(device.getAddress(), item);

                            for (OnBLEEventListener listener : mOnBLEEventListener) {
                                listener.onDiscovery(item);
                            }
                        }

                    }
                    break;

                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        //开始扫描
                        isDiscovering = true;
                        for (OnBLEEventListener listener : mOnBLEEventListener) {
                            listener.onDiscoveryStart();
                        }
                        for (BLEDeviceInfo item : mMap.values()) {
                            for (OnBLEEventListener listener : mOnBLEEventListener) {
                                listener.onDiscovery(item);
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        //结束扫描
                        if (isDiscovering) {
                            isDiscovering = false;
                            for (OnBLEEventListener listener : mOnBLEEventListener) {
                                listener.onDiscoveryStop(new ArrayList(mMap.values()));
                            }
                        }
                        break;
                    case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
                        //连接状态改变

                        break;
                    case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED: {
                        int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        switch (state) {
                            case BluetoothProfile.STATE_DISCONNECTED:
                                if (mOperatingDevice != null) {
                                    if (mMap.containsKey(device.getAddress())) {
                                        mMap.get(device.getAddress()).state = BLEDeviceInfo.STATE_BOND_BONDED;

                                        for (OnBLEEventListener listener : mOnBLEEventListener) {
                                            listener.onConnected(false, mMap.get(device.getAddress()));
                                        }

                                        mOperatingDevice = null;
                                    }
                                }
                                break;
                            case BluetoothProfile.STATE_CONNECTING:
                                if (mMap.containsKey(device.getAddress())) {
                                    mMap.get(device.getAddress()).state = BLEDeviceInfo.STATE_CONNECTING;
                                    for (OnBLEEventListener listener : mOnBLEEventListener) {
                                        listener.onConnecting(mMap.get(device.getAddress()));
                                    }
                                }
                                break;
                            case BluetoothProfile.STATE_CONNECTED:
                                for (BLEDeviceInfo item : mMap.values()) {
                                    if (item.state == BLEDeviceInfo.STATE_CONNECTED) {
                                        item.state = BLEDeviceInfo.STATE_BOND_BONDED;
                                        break;
                                    }
                                }

                                if (mMap.containsKey(device.getAddress())) {
                                    mMap.get(device.getAddress()).state = BLEDeviceInfo.STATE_CONNECTED;
                                    for (OnBLEEventListener listener : mOnBLEEventListener) {
                                        listener.onConnected(true, mMap.get(device.getAddress()));
                                    }
                                    mOperatingDevice = null;
                                }


                                break;
                            case BluetoothProfile.STATE_DISCONNECTING:
                                break;
                        }
                    }
                    break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        //蓝牙开关状态改变
                        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                        Log.e(TAG, "state " + state);
                        switch (state) {
                            case BluetoothAdapter.STATE_OFF:
                                // 发送关闭的通知
                                for (OnBLEEventListener listener : mOnBLEEventListener) {
                                    listener.onClose();
                                }
                                break;
                            case BluetoothAdapter.STATE_ON:
                                // 发送打开的通知
                                for (OnBLEEventListener listener : mOnBLEEventListener) {
                                    listener.onOpen();
                                }
                                break;
                        }
                        break;
                    case BluetoothDevice.ACTION_PAIRING_REQUEST:
                        // 提示app需要去设备上操作
//                        pin如何输入？
                        BluetoothDevice mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                        try {
//                            //(三星)4.3版本测试手机还是会弹出用户交互页面(闪一下)，如果不注释掉下面这句页面不会取消但可以配对成功。(中兴，魅族4(Flyme 6))5.1版本手机两中情况下都正常
                        try {
                            BLEUtil.setPairingConfirmation(mBluetoothDevice, true);
//                            abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                            abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。
//                            //3.调用setPin方法进行配对...
//                            boolean ret = BLEUtil.setPin(mBluetoothDevice, "0000");// 初始配对码一般为0000 1234
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
                        break;
                    case BluetoothDevice.ACTION_BOND_STATE_CHANGED: {
                        //获取发生改变的蓝牙对象
                        BluetoothDevice device = intent
                                .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        //根据不同的状态显示提示
                        if (mMap.containsKey(device.getAddress())) {
                            mMap.get(device.getAddress()).state = device.getBondState();

                        }

                        switch (device.getBondState()) {
                            case BluetoothDevice.BOND_BONDING://正在配对
                                Log.e(TAG, "正在配对......");
                                if (mMap.containsKey(device.getAddress()))
                                    for (OnBLEEventListener listener : mOnBLEEventListener) {
                                        listener.onBonding(mMap.get(device.getAddress()));
                                    }

                                break;
                            case BluetoothDevice.BOND_BONDED://配对结束
                                mOperatingDevice = null;
                                Log.e(TAG, "完成配对");
                                if (mMap.containsKey(device.getAddress()))
                                    for (OnBLEEventListener listener : mOnBLEEventListener) {
                                        listener.onBond(mMap.get(device.getAddress()));
                                    }

                                break;
                            case BluetoothDevice.BOND_NONE://取消配对/未配对
                                if (mOperatingDevice != null) {
                                    mOperatingDevice.state = device.getBondState();
                                    mOperatingDevice = null;
                                }

                                Log.e(TAG, "取消配对");
                                if (mMap.containsKey(device.getAddress()))
                                    for (OnBLEEventListener listener : mOnBLEEventListener) {
                                        listener.onUnBond(mMap.get(device.getAddress()));
                                    }

                            default:
                                break;
                        }
                    }
                    break;
                }
            }
        };

        registerReceiver(mReceiver, filter);

        BLEManager.setOnBLEEventListener(new BLEManager.OnBLEEventListener() {
            @Override
            public int isBLEOpen() {
                return mBluetoothAdapter.isEnabled() ? 0 : 1;
            }

            @Override
            public BLEDeviceInfo onGetCurrentConnectedBLEDevice() {
                for (BLEDeviceInfo item : mMap.values()) {
                    if (item.state == BLEDeviceInfo.STATE_CONNECTED) {
                        return item;
                    }
                }
                return null;
            }

            @Override
            public int openBLE() {
                return BLEService.this.openBLE();

            }

            @Override
            public int closeBLE() {
                return BLEService.this.closeBLE();
            }

            @Override
            public int startDiscovery() {
                return BLEService.this.startDiscovery();
            }

            @Override
            public int bond(String address) {
                try {
                    return createBond(mBluetoothAdapter.getRemoteDevice(address));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return BLEManager.RESULT_EXCEPTION;
            }

            @Override
            public int unBond(String address) {
                try {
                    return removeBond(mBluetoothAdapter.getRemoteDevice(address));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return BLEManager.RESULT_EXCEPTION;
            }

            @Override
            public int connect(String address) {
                try {
                    return BLEService.this.connect(mBluetoothAdapter.getRemoteDevice(address));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return BLEManager.RESULT_EXCEPTION;
            }
        });

        addOnBLEEventListener(new OnBLEEventListener() {
            @Override
            public void onOpen() {
                BLEManager.onBLEOpen();
            }

            @Override
            public void onClose() {
                BLEManager.onBLEClose();
            }

            @Override
            public void onDiscoveryStart() {
                BLEManager.onDiscoveryBLEStart();
            }

            @Override
            public void onDiscovery(BLEDeviceInfo item) {
                BLEManager.onDiscoveryBLE(item);
            }

            @Override
            public void onDiscoveryStop(ArrayList<BLEDeviceInfo> items) {
//                Log.e(TAG, JsonUtil.toJson(items));
//                BLEDeviceInfo[] infos = null;
//                if (items.size() > 0) {
//                    infos = new BLEDeviceInfo[items.size()];
//                }
//                for (int i = 0; i < items.size(); i++) {
//                    infos[i] = items.get(i);
//                }
//                BLEManager.onDiscoveryBLE(infos);
                BLEManager.onDiscoveryBLEStop();
            }

            @Override
            public void onConnecting(BLEDeviceInfo item) {
            }

            @Override
            public void onConnected(boolean success, BLEDeviceInfo item) {
                BLEManager.onBLEDeviceConnected(success, item);
            }

            @Override
            public void onBonding(BLEDeviceInfo item) {

            }

            @Override
            public void onBond(BLEDeviceInfo item) {
                BLEManager.onBLEDeviceBond(item);
            }

            @Override
            public void onUnBond(BLEDeviceInfo item) {
                BLEManager.onBLEDeviceUnBond(item);
            }
        });
    }

    private void initBondDevices() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : devices) {
            BLEDeviceInfo item = new BLEDeviceInfo();
            item.name = device.getName();
            item.address = device.getAddress();
            BluetoothClass bc = device.getBluetoothClass();
            if (bc != null) {
                item.major = bc.getMajorDeviceClass();
            }
            item.state = BLEDeviceInfo.STATE_BOND_BONDED;
            try {
                Method method = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                method.setAccessible(true);
                boolean isConnected = (boolean) method.invoke(device, (Object[]) null);
                if (isConnected) {
                    item.state = BLEDeviceInfo.STATE_CONNECTED;
                }
            } catch (Exception e) {

            }
            mMap.put(item.address, item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);

        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
        mBLEService = null;
    }

    public BLEDeviceInfo getOperatingDevice() {
        return mOperatingDevice;
    }

    public int openBLE() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(enableBtIntent);
            return BLEManager.RESULT_SUCCESS;
        }
        return BLEManager.RESULT_BLE_OPEND;
    }

    public int closeBLE() {
        mOperatingDevice = null;
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            mMap.clear();
            return BLEManager.RESULT_SUCCESS;
        }
        return BLEManager.RESULT_BLE_CLOSED;
    }

    public int startDiscovery() {
        if (!mBluetoothAdapter.isEnabled()) {
            return BLEManager.RESULT_BLE_CLOSED;
        }
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mMap.clear();
        initBondDevices();
        mBluetoothAdapter.startDiscovery();
        return BLEManager.RESULT_SUCCESS;
    }

    public int connect(final BluetoothDevice device) {
        BluetoothClass bc = device.getBluetoothClass();
        if (bc == null) {
            return BLEManager.RESULT_EXCEPTION;
        }
        Log.e(TAG, "device.getBluetoothClass().getMajorDeviceClass() " + bc.getMajorDeviceClass());
        if (!mBluetoothAdapter.isEnabled()) {
            return BLEManager.RESULT_BLE_CLOSED;
        }
        if ((bc.getMajorDeviceClass() & BluetoothClass.Device.Major.AUDIO_VIDEO) != BluetoothClass.Device.Major.AUDIO_VIDEO) {
            UIUtils.showToast("不是音频输出设备");
            return BLEManager.RESULT_BLE_DONT_SUPPORT_AUDIO_VIDEO;
        }

        if (mOperatingDevice != null) {
            // 正在操作
            return BLEManager.RESULT_IS_BUSY;
        }
        mOperatingDevice = mMap.get(device.getAddress());
        mBluetoothAdapter.getProfileProxy(this, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                BluetoothHeadset bluetoothHeadset = (BluetoothHeadset) proxy;
                Class btHeadsetCls = BluetoothHeadset.class;
                try {
                    Method connect = btHeadsetCls.getMethod("connect", BluetoothDevice.class);
                    connect.setAccessible(true);
                    connect.invoke(bluetoothHeadset, device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {

            }
        }, BluetoothProfile.HEADSET);
        return BLEManager.RESULT_SUCCESS;
    }

    public int createBond(BluetoothDevice device) throws Exception {
        if (!mBluetoothAdapter.isEnabled()) {
            return BLEManager.RESULT_BLE_CLOSED;
        }
        if (mOperatingDevice != null) {
            // 正在操作
            return BLEManager.RESULT_IS_BUSY;
        }

        mOperatingDevice = mMap.get(device.getAddress());
        return BLEUtil.createBond(device) ? BLEManager.RESULT_SUCCESS : BLEManager.RESULT_OP_FAILED;
    }

    public int removeBond(BluetoothDevice device) throws Exception {
        if (!mBluetoothAdapter.isEnabled()) {
            return BLEManager.RESULT_BLE_CLOSED;
        }
        if (mOperatingDevice != null) {
            // 正在操作
            return BLEManager.RESULT_IS_BUSY;
        }

        mOperatingDevice = mMap.get(device.getAddress());
        return BLEUtil.removeBond(device) ? BLEManager.RESULT_SUCCESS : BLEManager.RESULT_OP_FAILED;
    }

    public void addOnBLEEventListener(OnBLEEventListener listener) {
        mOnBLEEventListener.add(listener);
    }

    public void removeOnBLEEventListener(OnBLEEventListener listener) {
        mOnBLEEventListener.remove(listener);
    }

    public interface OnBLEEventListener {
        void onOpen();

        void onClose();

        void onDiscoveryStart();

        void onDiscovery(BLEDeviceInfo item);

        void onDiscoveryStop(ArrayList<BLEDeviceInfo> items);

        void onConnecting(BLEDeviceInfo item);

        void onConnected(boolean success, BLEDeviceInfo item);

        void onBonding(BLEDeviceInfo item);

        void onBond(BLEDeviceInfo item);

        void onUnBond(BLEDeviceInfo item);
    }
}
