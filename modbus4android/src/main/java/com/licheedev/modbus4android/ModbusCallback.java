package com.licheedev.modbus4android;

import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;

/**
 * modbus回到
 *
 * @param <T>
 */
public interface ModbusCallback<T> {

    /**
     * 成功
     *
     * @param t
     */
    void onSuccess(T t);

    /**
     * 失败
     *
     * @param tr
     * @see ModbusInitException 初始化失败或者没有初始化时的异常
     * @see ModbusTransportException modbus请求失败的异常
     * @see ModbusRespException modbus有响应，但是包含错误
     */
    void onFailure(Throwable tr);

    /**
     * 无论失败还是成功
     */
    void onFinally();
}
