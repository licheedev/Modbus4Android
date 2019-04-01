package com.licheedev.modbus4android.param;

import com.licheedev.modbus4android.AndroidSerialPortWrapper;
import com.licheedev.modbus4android.ModbusParam;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.serial.SerialPortWrapper;

/**
 * 串口参数
 */
public class SerialParam implements ModbusParam<SerialParam> {

    /**
     * 串口设备
     */
    private String serialDevice;
    /**
     * 串口波特率
     */
    private int baudRate;
    /**
     * 超时
     */
    private int timeout = DEFAULT_TIMEOUT;
    /**
     * 重试
     */
    private int retries = 2;

    private SerialParam() {
    }

    public static SerialParam create(String serialDevice, int baudRate) {
        SerialParam param = new SerialParam();
        param.serialDevice = serialDevice;
        param.baudRate = baudRate;
        return param;
    }

    public int getTimeout() {
        return timeout;
    }

    public SerialParam setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getRetries() {
        return retries;
    }

    @Override
    public SerialParam setRetries(int retries) {
        this.retries = retries;
        return this;
    }

    @Override
    public ModbusMaster createModbusMaster() {

        ModbusFactory modbusFactory = new ModbusFactory();

        SerialPortWrapper wrapper = new AndroidSerialPortWrapper(getSerialDevice(), getBaudRate());
        ModbusMaster master = modbusFactory.createRtuMaster(wrapper);
        master.setRetries(getRetries());
        master.setTimeout(getTimeout());

        return master;
    }

    public String getSerialDevice() {
        return serialDevice;
    }

    public SerialParam setSerialDevice(String serialDevice) {
        this.serialDevice = serialDevice;
        return this;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public SerialParam setBaudRate(int baudRate) {
        this.baudRate = baudRate;
        return this;
    }
}
