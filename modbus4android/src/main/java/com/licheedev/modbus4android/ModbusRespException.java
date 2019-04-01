package com.licheedev.modbus4android;

import com.serotonin.modbus4j.msg.ModbusResponse;

/**
 * Modbus响应异常
 */
public class ModbusRespException extends Exception {

    private byte exceptionCode;

    public ModbusRespException(ModbusResponse response) {
        super(response.getExceptionMessage() + " ,code=" + response.getExceptionCode());
        exceptionCode = response.getExceptionCode();
    }

    public byte getExceptionCode() {
        return exceptionCode;
    }
}
