package com.licheedev.modbus4android;

import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.exception.ModbusInitException;
import io.reactivex.Observable;

/**
 * Modbus工作接口，定义了Modbus初始化相关的方法
 */
interface IModbusWorker {

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

    ///**
    // * 给RxJava用的，Modbus工作线程调度器
    // *
    // * @return
    // */
    //Scheduler getModbusScheduler();
    //
    ///**
    // * Modbus工作线程的Handler
    // *
    // * @return
    // */
    //Handler getModbusHandler();

    /**
     * [RX]初始化modbus
     *
     * @param param
     * @return
     */
    Observable<ModbusMaster> rxInit(final ModbusParam param);

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
