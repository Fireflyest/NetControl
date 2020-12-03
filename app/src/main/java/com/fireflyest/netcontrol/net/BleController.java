package com.fireflyest.netcontrol.net;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fireflyest.netcontrol.net.callback.ConnectStateCallback;
import com.fireflyest.netcontrol.net.callback.OnReceiverCallback;
import com.fireflyest.netcontrol.net.callback.OnWriteCallback;
import com.fireflyest.netcontrol.net.request.ReceiverRequestQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BleController implements BtController {

    //输入日志标记
    public static final String LOG_TAG = "BLE_Controller";

    //接收数据uuid
    private static final UUID BLUETOOTH_NOTIFY_D = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bluetoothAdapter;

    private Map<String, BluetoothGattCharacteristic> characteristicMap = new HashMap<>();
    private Map<String, BluetoothGatt> gattMap = new HashMap<>();

    private Handler handler = new Handler(Looper.getMainLooper());

    //蓝牙连接回调
    private BleGattCallback bleGattCallback;
    //连接状态回调
    private ConnectStateCallback connectStateCallback;
    //写入回调
    private OnWriteCallback writeCallback;

    //读操作请求队列
    private ReceiverRequestQueue receiverRequestQueue = new ReceiverRequestQueue();

    private boolean enableNotify;

    //默认连接超时时间:6s
    private static final int CONNECTION_TIME_OUT = 6000;

    private List<String> disconnectList = new ArrayList<>();

    private static BleController bleController = new BleController();

    public static BleController getInstance() {return bleController; }

    private BleController(){
    }

    @Override
    public void init(BluetoothManager bluetoothManager) {
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.bleGattCallback = new BleGattCallback();
        this.enableNotify = true;
    }

    @Override
    public void connect(Context context, String address) {
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        if (remoteDevice == null) {
            Log.e(LOG_TAG, "未找到该蓝牙设备 -> " + address);
            return;
        }

        this.disconnectList.remove(address);

        remoteDevice.connectGatt(context, false, bleGattCallback);
        Log.e(LOG_TAG, "正在连接蓝牙设备 -> " + address);

        this.delayConnectResponse(address);

    }
    @Override
    public void setStateCallback(ConnectStateCallback connectStateCallback){
        this.connectStateCallback = connectStateCallback;
    }

    @Override
    public void writeBuffer(String address, byte[] buffer, OnWriteCallback writeCallback) {
        this.writeCallback = writeCallback;

        if (!bluetoothAdapter.isEnabled()) {
            writeCallback.onFailed(OnWriteCallback.FAILED_BLUETOOTH_DISABLE);
            Log.e(LOG_TAG, "FAILED_BLUETOOTH_DISABLE");
            return;
        }

        BluetoothGattCharacteristic characteristic = characteristicMap.get(address);
        if (characteristic == null) {
            writeCallback.onFailed(OnWriteCallback.FAILED_INVALID_CHARACTER);
            Log.e(LOG_TAG, "FAILED_INVALID_CHARACTER");
            return;
        }

        characteristic.setValue(buffer);

        BluetoothGatt gatt = gattMap.get(address);
        if (gatt != null) {
            boolean success = gatt.writeCharacteristic(characteristic);
            if(!success) writeCallback.onFailed(OnWriteCallback.FAILED_OPERATION);
            Log.e(LOG_TAG, "发送结果 -> " + (success ? "成功":"失败"));
        }

    }

    @Override
    public void closeConnect(String address) {
        BluetoothGatt gatt = gattMap.get(address);
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
            connectStateCallback.connectLost(address);
        }
        characteristicMap.remove(address);
        this.disconnectList.add(address);
    }


    @Override
    public BluetoothGatt getGatt(String address) {
        return gattMap.get(address);
    }

    @Override
    public void registerReceiveListener(String requestKey, OnReceiverCallback onReceiverCallback) {
        receiverRequestQueue.set(requestKey, onReceiverCallback);
    }

    @Override
    public void unregisterReceiveListener(String requestKey) {
        receiverRequestQueue.removeRequest(requestKey);
    }

    @Override
    public void enableNotify(String address) {
        BluetoothGatt gatt = gattMap.get(address);
        if (gatt == null) {
            Log.e(LOG_TAG, "蓝牙未连接 -> " + address);
            return;
        }

        BluetoothGattCharacteristic gattCharacteristic = characteristicMap.get(address);
        if (gattCharacteristic == null) {
            Log.e(LOG_TAG, "蓝牙未配置 -> " + address);
            return;
        }

        if((gattCharacteristic.getProperties() & 0x10) == 0)return;
        if (gatt.setCharacteristicNotification(gattCharacteristic, true)){
            BluetoothGattDescriptor clientConfig = gattCharacteristic.getDescriptor(BLUETOOTH_NOTIFY_D);
            if (clientConfig == null) return;
            clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(clientConfig);
        }

    }

    @Override
    public void setEnableNotify(boolean enable){
        this.enableNotify = enable;
    }

    @Override
    public void setCharacteristic(String address, String service, String characteristic) {
        BluetoothGatt gatt = gattMap.get(address);
        if (gatt == null) {
            Log.e(LOG_TAG, "蓝牙未连接 -> " + address);
            return;
        }
        BluetoothGattService gattService = gatt.getService(UUID.fromString(service));
        if (gattService == null) {
            Log.e(LOG_TAG, "未找到服务 -> " + service);
            return;
        }

        BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(UUID.fromString(characteristic));
        characteristicMap.put(address, gattCharacteristic);

    }

    @Override
    public void readCharacteristic(String address){
        BluetoothGatt gatt = gattMap.get(address);
        if (gatt == null) {
            Log.e(LOG_TAG, "蓝牙未连接 -> " + address);
            return;
        }

        BluetoothGattCharacteristic gattCharacteristic = characteristicMap.get(address);
        if (gattCharacteristic == null) {
            Log.e(LOG_TAG, "蓝牙未配置 -> " + address);
            return;
        }

        gatt.readCharacteristic(gattCharacteristic);

    }

    /*##################################################################################3*/

    /**
     * 蓝牙GATT连接及操作事件回调
     */
    private class BleGattCallback extends BluetoothGattCallback {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String address = gatt.getDevice().getAddress();
            if (newState == BluetoothProfile.STATE_CONNECTED) { //连接成功
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {   //断开连接
                connectStateCallback.connectLost(address);
                characteristicMap.remove(address);
                if (!disconnectList.contains(address)) {
                    reConnect();
                }
            }
        }

        //服务被发现了
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            String address = gatt.getDevice().getAddress();
            connectStateCallback.connectSucceed(address);
            gattMap.put(address, gatt);
            if(enableNotify){
                for(BluetoothGattService service : gatt.getServices()){
                    for(BluetoothGattCharacteristic  characteristic: service.getCharacteristics()){
                        if((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0){
                            if (gatt.setCharacteristicNotification(characteristic, true)){
                                BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(BLUETOOTH_NOTIFY_D);
                                if (clientConfig == null) continue;
                                clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                gatt.writeDescriptor(clientConfig);
                            }
                        }
                    }
                }
            }
        }

        //收到数据
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            Log.e(LOG_TAG, "接收数据: "+ Arrays.toString(characteristic.getValue()));
            final OnReceiverCallback callback = receiverRequestQueue.get("mainActivity");
            if(callback == null)return;
            final byte[] data = characteristic.getValue();
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onReceive(data);
                }
            });
        }

        //描述符被写了
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        }

        //描述符被读了
        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.e(LOG_TAG, "读取数据: "+ Arrays.toString(characteristic.getValue()));
            final OnReceiverCallback callback = receiverRequestQueue.get("mainActivity");
            if(callback == null)return;
            final byte[] data = characteristic.getValue();
            if(data == null)return;
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    callback.onReceive(data);
                }
            });
        }

        //发送数据结果
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (null != writeCallback) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onSuccess();
                        }
                    });
                    Log.e(LOG_TAG, "Send data success!");
                } else {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            writeCallback.onFailed(OnWriteCallback.FAILED_OPERATION);
                        }
                    });
                    Log.e(LOG_TAG, "Send data failed!");
                }
            }
        }
    }

    private void runOnMainThread(Runnable runnable) {
        if (isMainThread()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }

    private boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    private void reConnect(){

    }

    /**
     * 如果连接connectionTimeOut时间后还没有响应,手动关掉连接.
     */
    private void delayConnectResponse(final String address) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!gattMap.containsKey(address)) connectStateCallback.connectLost(address);
            }
        }, CONNECTION_TIME_OUT);
    }

}
