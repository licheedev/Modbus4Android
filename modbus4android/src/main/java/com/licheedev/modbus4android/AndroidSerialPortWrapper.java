package com.licheedev.modbus4android;

import android.serialport.SerialPort;
import com.serotonin.modbus4j.serial.SerialPortWrapper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * modbus的Android串口实现
 */
public class AndroidSerialPortWrapper implements SerialPortWrapper {

    private final String mDevice;
    private final int mBaudRate;
    private BufferedInputStream mInputStream;
    private BufferedOutputStream mOutputStream;
    private SerialPort mSerialPort;

    public AndroidSerialPortWrapper(String device, int baudRate) {
        mDevice = device;
        mBaudRate = baudRate;
    }

    @Override
    public void close() throws Exception {

        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mInputStream = null;
            }
        }

        if (mOutputStream != null) {

            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mOutputStream = null;
            }
        }

        if (mSerialPort != null) {
            try {
                mSerialPort.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void open() throws Exception {

        mSerialPort = new SerialPort(mDevice, mBaudRate);
        mInputStream = new BufferedInputStream(mSerialPort.getInputStream());
        mOutputStream = new BufferedOutputStream(mSerialPort.getOutputStream());
    }

    @Override
    public InputStream getInputStream() {
        return mInputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    @Override
    public int getBaudRate() {
        return mBaudRate;
    }

    @Override
    public int getFlowControlIn() {
        return 0;
    }

    @Override
    public int getFlowControlOut() {
        return 0;
    }

    @Override
    public int getDataBits() {
        return 8; // 数据位 8
    }

    @Override
    public int getStopBits() {
        return 1; // 停止位 1
    }

    @Override
    public int getParity() {
        return 0;
    }
}
