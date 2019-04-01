package com.licheedev.modbus4android.param;

import com.licheedev.modbus4android.ModbusParam;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.ip.IpParameters;

/**
 * TCP参数
 */
public class TcpParam implements ModbusParam<TcpParam> {

    private final IpParameters mParameters;
    /**
     * 超时
     */
    private int timeout = 500;
    /**
     * 重试
     */
    private int retries = 2;
    private boolean keepAlive;

    private TcpParam(String host, int port) {
        mParameters = new IpParameters();
        mParameters.setHost(host);
        mParameters.setPort(port);
    }

    public static TcpParam create(String host, int port) {
        TcpParam param = new TcpParam(host, port);
        return param;
    }

    public String getHost() {
        return mParameters.getHost();
    }

    public TcpParam setHost(String host) {
        mParameters.setHost(host);
        return this;
    }

    public int getPort() {
        return mParameters.getPort();
    }

    public TcpParam setPort(int port) {
        mParameters.setPort(port);
        return this;
    }

    public boolean isEncapsulated() {
        return mParameters.isEncapsulated();
    }

    public TcpParam setEncapsulated(boolean encapsulated) {
        mParameters.setEncapsulated(encapsulated);
        return this;
    }

    public int getTimeout() {
        return timeout;
    }

    public TcpParam setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getRetries() {
        return retries;
    }

    public TcpParam setRetries(int retries) {
        this.retries = retries;
        return this;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public TcpParam setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    @Override
    public ModbusMaster createModbusMaster() {
        ModbusFactory modbusFactory = new ModbusFactory();
        ModbusMaster master = modbusFactory.createTcpMaster(mParameters, isKeepAlive());
        master.setRetries(getRetries());
        master.setTimeout(getTimeout());
        return master;
    }
}
