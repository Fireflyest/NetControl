package com.fireflyest.netcontrol.net;

import android.bluetooth.BluetoothManager;
import android.content.Context;

public class BtManager {

    public static final int BLE = 0;
    public static final int NORMAL = 1;

    public static int connectType;

    private BtManager() {
    }

    public static void init(int type, Context context) {
        switch (type) {
            case BLE:
                BleController.getInstance().init((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE));
                break;
            default:
                break;
        }
        connectType = type;
    }

    public static BtController getBtController() {
//        switch (connectType) {
//            case BLE:
//                return BleController.getInstance();
//            case NORMAL:
//            default:
//                return null;
//        }
        return BleController.getInstance();

    }

}
