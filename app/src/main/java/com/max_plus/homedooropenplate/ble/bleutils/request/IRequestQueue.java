package com.max_plus.homedooropenplate.ble.bleutils.request;

/**
 * 类名: IRequestQueue
 * 描述: NULL
 */
public interface IRequestQueue<T> {
    void set(String key, T t);

    T get(String key);
}
