package com.licheedev.demo;

import android.app.Application;
import com.licheedev.adaptscreen.AdaptScreenEx;
import com.licheedev.demo.base.PrefUtil;
import com.serotonin.modbus4j.ModbusConfig;

public class App extends Application {

    static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        // 屏幕适配
        AdaptScreenEx.init(this);
        PrefUtil.init(this);

        configModbus();
    }

    public static App getInstance() {
        return sInstance;
    }

    /**
     * 配置Modbus,可选
     */
    private void configModbus() {
        // 启用rtu的crc校验（默认就启用）
        ModbusConfig.setEnableRtuCrc(true);
        // 打印数据log（默认全禁用）
        // System.out: MessagingControl.send: 01030000000305cb
        // System.out: MessagingConnection.read: 010306000100020000bd75
        ModbusConfig.setEnableDataLog(true, true);
    }
}
