package com.fireflyest.netcontrol.net;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.fireflyest.netcontrol.net.callback.ConnectStateCallback;
import com.fireflyest.netcontrol.net.callback.OnReceiverCallback;
import com.fireflyest.netcontrol.net.callback.OnWriteCallback;

import java.util.List;

public interface BtController {

    void init(BluetoothManager bluetoothManager);

    void connect(Context context, final String address);

    void setStateCallback(ConnectStateCallback connectStateCallback);

    void writeBuffer(String address, String str, OnWriteCallback writeCallback);

    void closeConnect(String address);

    BluetoothGatt getGatt(String address);

    void registerReceiveListener(String requestKey, OnReceiverCallback onReceiverCallback);

    void unregisterReceiveListener(String requestKey);

    void enableNotify(String address);

    void setEnableNotify(boolean enable);

    void setCharacteristic(String address, String service, String characteristic);

    void readCharacteristic(String address);

    void setEnableHex(boolean enable);

}
