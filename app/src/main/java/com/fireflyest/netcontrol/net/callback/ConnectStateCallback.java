package com.fireflyest.netcontrol.net.callback;

public interface ConnectStateCallback {

    /**
     * 连接成功
     * @param address 地址
     */
    void connectSucceed(String address);

    /**
     * 连接丢失
     * @param address 地址
     */
    void connectLost(String address);

}
