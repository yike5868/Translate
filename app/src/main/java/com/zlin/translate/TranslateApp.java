package com.zlin.translate;

import android.app.Application;

import com.zlin.tools.CrashHandler;

/**
 * Created by zhanglin03 on 2018/12/28.
 */

public class TranslateApp extends Application {
    private static TranslateApp mInstance = null;

    public static TranslateApp getInstance() {
        return mInstance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        CrashHandler.getInstance().init(this);
    }
}
