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
    private final int mDataBits;
    private final int mParity;
    private final int mStopBits;

    private BufferedInputStream mInputStream;
    private BufferedOutputStream mOutputStream;
    private SerialPort mSerialPort;

    public AndroidSerialPortWrapper(String device, int baudRate, int dataBits, int parity,
        int stopBits) {
        mDevice = device;
        mBaudRate = baudRate;
        mDataBits = dataBits;
        mParity = parity;
        mStopBits = stopBits;
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

        mSerialPort = SerialPort //
            .newBuilder(mDevice, mBaudRate)
            .parity(mParity)
            .dataBits(mDataBits)
            .stopBits(mStopBits)
            .build();
        
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
    public int getDataBits() {
        return mDataBits;
    }

    @Override
    public int getStopBits() {
        return mStopBits;
    }

    @Override
    public int getParity() {
        return mParity;
    }
}
