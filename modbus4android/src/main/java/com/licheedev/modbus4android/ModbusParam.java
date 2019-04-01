package com.licheedev.modbus4android;

import com.serotonin.modbus4j.ModbusMaster;

/**
 * modbus初始化参数
 *
 * @param <T>
 */
public interface ModbusParam<T extends ModbusParam> {

    /**
     * 默认超时（毫秒）
     */
    int DEFAULT_TIMEOUT = 500;
    /**
     * 默认重试次数
     */
    int DEFAULT_RETRIES = 2;

    /**
     * 超时
     *
     * @return
     */
    int getTimeout();

    /**
     * 超时(毫秒)
     *
     * @param timeout
     * @return
     */
    T setTimeout(int timeout);

    /**
     * 重试次数
     *
     * @return
     */
    int getRetries();

    /**
     * 重试次数
     *
     * @param retries
     * @return
     */
    T setRetries(int retries);

    /**
     * 创建ModbusMaster
     *
     * @return
     */
    ModbusMaster createModbusMaster();
}
