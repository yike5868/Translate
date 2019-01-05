package com.zlin.translate;

import android.app.Application;

import com.umeng.commonsdk.UMConfigure;
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
        UMConfigure.init(this, "5c2db7acb465f5d63e000079", "Ten", UMConfigure.DEVICE_TYPE_PHONE, null);
    }
}
