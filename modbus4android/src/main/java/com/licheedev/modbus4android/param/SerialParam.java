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
    /**
     * 数据位
     */
    private int dataBits = 8;
    /**
     * 校验位
     */
    private int parity = 0;
    /**
     * 停止位
     */
    private int stopBits = 1;

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

        SerialPortWrapper wrapper =
            new AndroidSerialPortWrapper(getSerialDevice(), getBaudRate(), getDataBits(),
                getParity(), getStopBits());

        ModbusMaster master = modbusFactory.createRtuMaster(wrapper);
        master.setRetries(getRetries());
        master.setTimeout(getTimeout());

        return master;
    }

    /**
     * 获取串口设备地址
     *
     * @return
     */
    public String getSerialDevice() {
        return serialDevice;
    }

    /**
     * 设置串口设备地址
     *
     * @param serialDevice
     * @return
     */
    public SerialParam setSerialDevice(String serialDevice) {
        this.serialDevice = serialDevice;
        return this;
    }

    /**
     * 获取串口波特率
     *
     * @return
     */
    public int getBaudRate() {
        return baudRate;
    }

    /**
     * 设置串口波特率
     *
     * @param baudRate
     * @return
     */
    public SerialParam setBaudRate(int baudRate) {
        this.baudRate = baudRate;
        return this;
    }

    /**
     * 设置串口数据位
     *
     * @param dataBits 默认8,可选值为5~8
     * @return
     */
    public SerialParam setDataBits(int dataBits) {
        this.dataBits = dataBits;
        return this;
    }

    /**
     * 设置串口校验位
     *
     * @param parity 0:无校验位(NONE，默认)；1:奇校验位(ODD);2:偶校验位(EVEN)
     * @return
     */
    public SerialParam setParity(int parity) {
        this.parity = parity;
        return this;
    }

    /**
     * 设置串口停止位
     *
     * @param stopBits 默认1；1:1位停止位；2:2位停止位
     * @return
     */
    public SerialParam setStopBits(int stopBits) {
        this.stopBits = stopBits;
        return this;
    }

    /**
     * 获取串口数据位
     *
     * @return
     */
    public int getDataBits() {
        return dataBits;
    }

    /**
     * 获取串口校验位
     *
     * @return
     */
    public int getParity() {
        return parity;
    }

    /**
     * 获取串口停止位
     *
     * @return
     */
    public int getStopBits() {
        return stopBits;
    }
}
