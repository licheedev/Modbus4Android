package com.licheedev.demo;

import android.app.Application;
import com.licheedev.demo.base.AdaptScreenUtils;
import com.licheedev.demo.base.PrefUtil;

public class App extends Application {

    static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        AdaptScreenUtils.init(this);
        PrefUtil.init(this);
    }

    public static App getInstance() {
        return sInstance;
    }
}
