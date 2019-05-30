package com.licheedev.demo.base;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import java.lang.reflect.Field;

/**
 * 屏幕适配工具
 * <a href="https://github.com/Blankj/AndroidUtilCode/blob/master/utilcode/README-CN.md#adaptscreen-%E7%9B%B8%E5%85%B3---adaptscreenutilsjava---demo">查看原始代码</a>
 * 增加了适配较长/较短边功能
 */
public final class AdaptScreenUtils {

    private static boolean isInitMiui = false;
    private static Field mTmpMetricsField;
    private static Context sApplication;

    /**
     * 初始化
     *
     * @param context
     */
    public static void init(Context context) {
        sApplication = context.getApplicationContext();
    }

    /**
     * 针对屏幕“当前水平方向的尺寸”进行适配，重写{@link Activity#getResources()} 方法，调用此方法后返回
     *
     * @param resources 填入super.getResources()
     * @param designWidth 参考尺寸
     */
    public static Resources adaptWidth(Resources resources, int designWidth) {
        DisplayMetrics dm = getDisplayMetrics(resources);

        int width = dm.widthPixels;
        float newXdpi = dm.xdpi = (width * 72f) / designWidth;
        setAppDmXdpi(newXdpi);
        return resources;
    }

    /**
     * 针对屏幕“较短边”进行适配，重写{@link Activity#getResources()} 方法，调用此方法后返回
     *
     * @param resources 填入super.getResources()
     * @param designShortSize 参考UI图的较短边尺寸
     */
    public static Resources adaptShorter(Resources resources, int designShortSize) {
        DisplayMetrics dm = getDisplayMetrics(resources);

        int width = dm.widthPixels < dm.heightPixels ? dm.widthPixels : dm.heightPixels;
        float newXdpi = dm.xdpi = (width * 72f) / designShortSize;
        setAppDmXdpi(newXdpi);
        return resources;
    }

    /**
     * 针对屏幕“当前水平方向的尺寸”进行适配，重写{@link Activity#getResources()} 方法，调用此方法后返回
     *
     * @param resources 填入super.getResources()
     * @param designHeight 参考尺寸
     */
    public static Resources adaptHeight(Resources resources, int designHeight) {
        DisplayMetrics dm = getDisplayMetrics(resources);
        int height = dm.heightPixels;
        float newXdpi = dm.xdpi = (height * 72f) / designHeight;
        setAppDmXdpi(newXdpi);
        return resources;
    }

    /**
     * 针对屏幕“较长边”进行适配，重写{@link Activity#getResources()} 方法，调用此方法后返回
     *
     * @param resources 填入super.getResources()
     * @param designLongerSize 参考UI图的较长边尺寸
     */
    public static Resources adaptLonger(Resources resources, int designLongerSize) {
        DisplayMetrics dm = getDisplayMetrics(resources);
        int height = dm.heightPixels > dm.widthPixels ? dm.heightPixels : dm.widthPixels;
        float newXdpi = dm.xdpi = (height * 72f) / designLongerSize;
        setAppDmXdpi(newXdpi);
        return resources;
    }

    /**
     * 取消适配
     * 重写{@link Activity#getResources()} 方法，调用此方法后返回
     *
     * @param resources 填入super.getResources()
     */
    public static Resources closeAdapt(Resources resources) {
        DisplayMetrics dm = getDisplayMetrics(resources);
        float newXdpi = dm.xdpi = dm.density * 72;
        setAppDmXdpi(newXdpi);
        return resources;
    }

    /**
     * pt转px
     *
     * @param ptValue pt
     * @return px
     */
    public static int pt2Px(float ptValue) {
        DisplayMetrics metrics = sApplication.getResources().getDisplayMetrics();
        return (int) (ptValue * metrics.xdpi / 72f + 0.5);
    }

    /**
     * px转
     *
     * @param pxValue px
     * @return pt
     */
    public static int px2Pt(float pxValue) {
        DisplayMetrics metrics = sApplication.getResources().getDisplayMetrics();
        return (int) (pxValue * 72 / metrics.xdpi + 0.5);
    }

    private static void setAppDmXdpi(final float xdpi) {
        sApplication.getResources().getDisplayMetrics().xdpi = xdpi;
    }

    private static DisplayMetrics getDisplayMetrics(Resources resources) {
        DisplayMetrics miuiDisplayMetrics = getMiuiTmpMetrics(resources);
        if (miuiDisplayMetrics == null) return resources.getDisplayMetrics();
        return miuiDisplayMetrics;
    }

    private static DisplayMetrics getMiuiTmpMetrics(Resources resources) {
        if (!isInitMiui) {
            DisplayMetrics ret = null;
            String simpleName = resources.getClass().getSimpleName();
            if ("MiuiResources".equals(simpleName) || "XResources".equals(simpleName)) {
                try {
                    //noinspection JavaReflectionMemberAccess
                    mTmpMetricsField = Resources.class.getDeclaredField("mTmpMetrics");
                    mTmpMetricsField.setAccessible(true);
                    ret = (DisplayMetrics) mTmpMetricsField.get(resources);
                } catch (Exception e) {
                    Log.e("AdaptScreenUtils", "no field of mTmpMetrics in resources.");
                }
            }
            isInitMiui = true;
            return ret;
        }
        if (mTmpMetricsField == null) return null;
        try {
            return (DisplayMetrics) mTmpMetricsField.get(resources);
        } catch (Exception e) {
            return null;
        }
    }
}
