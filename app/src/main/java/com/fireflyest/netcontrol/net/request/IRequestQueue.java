package com.fireflyest.netcontrol.net.request;

/**
 * 描述:请求队列
 */
public interface IRequestQueue<T> {

    void set(String key, T t);

    T get(String key);
}
