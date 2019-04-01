package com.licheedev.demo.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * SharedPreferences工具类，需要调用{@link PrefUtil#init(Context)} 进行初始化
 */
public class PrefUtil {

    private static PrefUtil sDefault;

    private SharedPreferences mPreferences;

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context) {
        sDefault = new PrefUtil(
            PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()));
    }

    /**
     * 获取默认的PrefUtil实例
     *
     * @return
     */
    public static PrefUtil getDefault() {
        checkInit();
        return sDefault;
    }

    /**
     * 新建PrefUtil实例
     *
     * @param context
     * @param name
     * @return
     */
    public static PrefUtil newInstance(Context context, String name) {
        return new PrefUtil(context, name);
    }

    private PrefUtil(SharedPreferences preferences) {
        mPreferences = preferences;
    }

    private PrefUtil(Context context, String name) {
        mPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     * 获得SharedPreferences.Editor对象
     *
     * @return
     */
    public SharedPreferences.Editor edit() {
        return mPreferences.edit();
    }

    /**
     * 填充int并返回SharedPreferences.Editor对象，
     * 最后需要调用{@link SharedPreferences.Editor#apply()}才能保存数据
     *
     * @param key
     * @param value
     * @return
     */
    public SharedPreferences.Editor putInt(String key, int value) {
        return edit().putInt(key, value);
    }

    /**
     * 直接保存int
     *
     * @param key
     * @param value
     */
    public void saveInt(String key, int value) {
        putInt(key, value).apply();
    }

    /**
     * 获取int数据
     *
     * @param key
     * @param defValue
     * @return
     */
    public int getInt(String key, int defValue) {
        return mPreferences.getInt(key, defValue);
    }

    /**
     * 填充float并返回SharedPreferences.Editor对象，
     * 最后需要调用{@link SharedPreferences.Editor#apply()}才能保存数据
     *
     * @param key
     * @param value
     * @return
     */
    public SharedPreferences.Editor putFloat(String key, float value) {
        return edit().putFloat(key, value);
    }

    /**
     * 直接保存float
     *
     * @param key
     * @param value
     */
    public void saveFloat(String key, float value) {
        putFloat(key, value).apply();
    }

    /**
     * 获取float数据
     *
     * @param key
     * @param defValue
     * @return
     */
    public float getFloat(String key, float defValue) {
        return mPreferences.getFloat(key, defValue);
    }

    /**
     * 填充boolean并返回SharedPreferences.Editor对象，
     * 最后需要调用{@link SharedPreferences.Editor#apply()}才能保存数据
     *
     * @param key
     * @param value
     * @return
     */
    public SharedPreferences.Editor putBoolean(String key, boolean value) {
        return edit().putBoolean(key, value);
    }

    /**
     * 直接保存boolean
     *
     * @param key
     * @param value
     */
    public void saveBoolean(String key, boolean value) {
        putBoolean(key, value).apply();
    }

    /**
     * 获取boolean数据
     *
     * @param key
     * @param defValue
     * @return
     */
    public boolean getBoolean(String key, boolean defValue) {
        return mPreferences.getBoolean(key, defValue);
    }

    /**
     * 填充long并返回SharedPreferences.Editor对象，
     * 最后需要调用{@link SharedPreferences.Editor#apply()}才能保存数据
     *
     * @param key
     * @param value
     * @return
     */
    public SharedPreferences.Editor putLong(String key, long value) {
        return edit().putLong(key, value);
    }

    /**
     * 直接保存long
     *
     * @param key
     * @param value
     */
    public void saveLong(String key, long value) {
        putLong(key, value).apply();
    }

    /**
     * 获取long数据
     *
     * @param key
     * @param defValue
     * @return
     */
    public long getLong(String key, long defValue) {
        return mPreferences.getLong(key, defValue);
    }

    /**
     * 填充String并返回SharedPreferences.Editor对象，
     * 最后需要调用{@link SharedPreferences.Editor#apply()}才能保存数据
     *
     * @param key
     * @param value
     * @return
     */
    public SharedPreferences.Editor putString(String key, String value) {
        return edit().putString(key, value);
    }

    /**
     * 直接保存String
     *
     * @param key
     * @param value
     */
    public void saveString(String key, String value) {
        putString(key, value).apply();
    }

    /**
     * 获取String数据
     *
     * @param key
     * @param defValue
     * @return
     */
    public String getString(String key, String defValue) {
        return mPreferences.getString(key, defValue);
    }

    /**
     * 获取绑定的SharedPreferences
     *
     * @return
     */
    public SharedPreferences getSp() {
        return mPreferences;
    }

    /**
     * 检查是否有初始化过Content
     */
    private static void checkInit() {
        if (sDefault == null) {
            throw new NullPointerException("默认PrefUtil为null，请先调用PrefUtil.init(context)进行初始化");
        }
    }
}
