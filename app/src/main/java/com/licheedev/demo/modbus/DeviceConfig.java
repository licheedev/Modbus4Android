package com.licheedev.demo.modbus;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.serialport.SerialPortFinder;
import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.licheedev.demo.App;
import com.licheedev.demo.R;
import com.licheedev.demo.base.PrefUtil;
import com.serotonin.modbus4j.ModbusConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * @ Created by John on 2018/3/29.
 */

public class DeviceConfig {

    public static class Config {

        String devicePath;
        int baudrate = 0;
        boolean enableCrc = true;
    }

    private static final String CONFIG = "serial_config";

    private static DeviceConfig sInstance = new DeviceConfig();

    private Gson mGson;
    private Config mConfig;
    // 设备路径列表
    private String[] mDevicePaths;
    // 波特率列表
    private int[] mBaudrates;
    // 波特率字符串列表
    private String[] mBaudrateStrs;

    private DeviceConfig() {
        mGson = new GsonBuilder().serializeNulls().create();

        Resources resources = App.getInstance().getResources();
        mBaudrates = resources.getIntArray(R.array.baudrates);
        mBaudrateStrs = new String[mBaudrates.length];
        for (int i = 0; i < mBaudrateStrs.length; i++) {
            mBaudrateStrs[i] = String.valueOf(mBaudrates[i]);
        }

        SerialPortFinder serialPortFinder = new SerialPortFinder();
        // 设备
        String[] allDevicesPath = serialPortFinder.getAllDevicesPath();
        ArrayList<String> ttysXPahts = new ArrayList<>();

        if (allDevicesPath.length > 0) {
            String pattern = "^/dev/tty(S|(USB))\\d+$";
            for (String s : allDevicesPath) {
                if (Pattern.matches(pattern, s)) {
                    ttysXPahts.add(s);
                }
            }
        }

        if (ttysXPahts.size() > 0) {
            Collections.sort(ttysXPahts);
            mDevicePaths = ttysXPahts.toArray(new String[0]);
        } else {
            mDevicePaths = new String[] { "null" };
        }

        loadConfigFromFile();
        if (mConfig == null) {
            mConfig = new Config();
            mConfig.devicePath = mDevicePaths[0];
            mConfig.baudrate = 19200;
        }
    }

    public static DeviceConfig get() {
        return sInstance;
    }

    /**
     * 保存配置到文件中，默认异步保存
     */
    private void saveConfigToFile() {
        saveConfigToFile(false);
    }

    /**
     * 保存配置到文件中
     *
     * @param sync 是否同步保存
     */
    private void saveConfigToFile(boolean sync) {
        String json = mGson.toJson(mConfig);
        SharedPreferences.Editor editor = PrefUtil.getDefault().putString(CONFIG, json);
        if (sync) {
            editor.commit();
        } else {
            editor.apply();
        }
    }

    /**
     * 加载配置
     */
    private void loadConfigFromFile() {

        String json = PrefUtil.getDefault().getString(CONFIG, "");
        try {
            Config config = mGson.fromJson(json, Config.class);
            if (config != null) {
                mConfig = config;
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取设备列表
     *
     * @return
     */
    public String[] getDevicePaths() {
        return mDevicePaths;
    }

    /**
     * 获取波特率列表
     *
     * @return
     */
    public int[] getBaudrates() {
        return mBaudrates;
    }

    /**
     * 获取波特率字符串列表
     *
     * @return
     */
    public String[] getBaudrateStrs() {
        return mBaudrateStrs;
    }

    /**
     * 保存设备
     *
     * @param devicePath
     * @param baudrate
     */
    public void updateSerialConfig(String devicePath, int baudrate) {
        mConfig.devicePath = devicePath;
        mConfig.baudrate = baudrate;
        saveConfigToFile();
    }

    /**
     * 保存设备
     *
     * @param devicePath
     */
    public void updateSerialConfig(String devicePath) {
        mConfig.devicePath = devicePath;
        saveConfigToFile();
    }

    /**
     * 获取设备路径
     *
     * @return
     */
    public String getDevice() {
        return mConfig.devicePath;
    }

    /**
     * 获取波特率
     *
     * @return
     */
    public int getBaudrate() {
        return mConfig.baudrate;
    }

    /**
     * 找到设备路径索引
     *
     * @param devicePath
     * @return
     */
    public int findDeviceIndex(String devicePath) {

        for (int i = 0; i < mDevicePaths.length; i++) {
            if (StringUtils.equals(mDevicePaths[i], devicePath)) {
                return i;
            }
        }

        return 0;
    }

    /**
     * 找到波特率索引
     *
     * @param baudrate
     * @return
     */
    public int findBaudrateIndex(int baudrate) {
        for (int i = 0; i < mBaudrates.length; i++) {
            if (mBaudrates[i] == baudrate) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 是否已经配置过设备
     *
     * @return
     */
    public boolean isDeviceConfiged() {
        return !TextUtils.isEmpty(mConfig.devicePath) && mConfig.baudrate != 0;
    }

    /**
     * 是否启用CRC校验
     *
     * @return
     */
    public boolean isCrcEnable() {
        return mConfig.enableCrc;
    }

    /**
     * 设置 启用/禁用 CRC校验
     *
     * @param enable
     */
    public void setEnableCrc(boolean enable) {
        mConfig.enableCrc = enable;
        ModbusConfig.setEnableRtuCrc(enable);
        saveConfigToFile();
    }
}

