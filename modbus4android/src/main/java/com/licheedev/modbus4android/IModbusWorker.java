package com.licheedev.modbus4android;

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;

/**
 * Modbus工作接口，定义了Modbus初始化相关的方法
 */
public interface IModbusWorker {

    /**
     * 关掉ModbusMaster
     */
    void closeModbusMaster();

    /**
     * 释放整个ModbusWorker，ModbusScheduler会被释放，ModbusMaster无法再次被打开
     */
    void release();

    /**
     * 获取ModbusMaster对象（可以用来判断是否已经初始化Modbus）
     *
     * @return
     */
    ModbusMaster getModbusMaster();

    /**
     * 是否已经打开Modbus
     *
     * @return
     */
    boolean isModbusOpened();

    /**
     * 初始化modbus
     *
     * @param param
     * @param callback
     */
    void init(final ModbusParam param, final ModbusCallback<ModbusMaster> callback);

    /**
     * 检查工作状态，判断是否正确初始化，或者已经被释放
     *
     * @throws ModbusInitException
     * @throws IllegalStateException
     */
    void checkWorkingState() throws ModbusInitException, IllegalStateException;
}
