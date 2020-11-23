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

    void writeBuffer(String address, byte[] buf, OnWriteCallback writeCallback);

    void closeConnect(String address);

    boolean isConnected(String address);

    List<BluetoothDevice> getDeviceList();

    BluetoothGatt getGatt(String address);

    void registerReceiveListener(String requestKey, OnReceiverCallback onReceiverCallback);

    void unregisterReceiveListener(String requestKey);

}
