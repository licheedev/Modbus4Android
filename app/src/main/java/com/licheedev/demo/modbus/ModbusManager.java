package com.licheedev.demo.modbus;

import com.licheedev.impl.rxjava.RxModbusWorker;

//public class ModbusManager extends ModbusWorker {
public class ModbusManager extends RxModbusWorker {

    private static volatile ModbusManager sInstance;

    public static ModbusManager get() {
        ModbusManager manager = sInstance;
        if (manager == null) {
            synchronized (ModbusManager.class) {
                manager = sInstance;
                if (manager == null) {
                    manager = new ModbusManager();
                    sInstance = manager;
                }
            }
        }
        return manager;
    }

    private ModbusManager() {
    }

    /**
     * 释放整个ModbusManager，单例会被置null
     */
    public synchronized void release() {
        super.release();
        sInstance = null;
    }

    //
    ///**
    // * 初始化modbus
    // *
    // * @param callback
    // */
    //public synchronized void initModbus(ModbusCallback<ModbusMaster> callback) {
    //
    //    if (isModbusOpened()) {
    //        return;
    //    }
    //
    //    // 串口
    //    ModbusParam serialParam = SerialParam.create(Protocol.MODBUS_DEVICE, Protocol.BAUDRATE)
    //        .setTimeout(Protocol.REQUEST_TIMEOUT)
    //        .setRetries(0); // 不重试
    //
    //    // TCP
    //    ModbusParam tcpParam = TcpParam.create("127.0.0.1", 233)
    //        .setEncapsulated(false)
    //        .setKeepAlive(true)
    //        .setTimeout(2000)
    //        .setRetries(0);
    //
    //    // 先把原来的关掉
    //    closeModbusMaster();
    //    init(serialParam, callback);
    //}
}
