package com.licheedev.demo.modbus;

/**
 * Created by John on 2018/8/15.
 */
public interface Protocol {

    int SLAVE_ID = 0x01;

    String MODBUS_DEVICE = "/dev/ttyS0";

    int BAUDRATE = 9600;
    /**
     * modbus请求超时时间
     */
    int REQUEST_TIMEOUT = 1000;
}
