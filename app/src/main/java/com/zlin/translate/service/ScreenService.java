package com.zlin.translate.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.util.logging.Logger;

public class ScreenService  extends Service {
    private View mCheckFullScreenView = null;
    private View mStatusBarHelperView = null;
    @Override
    public void onCreate() {
        super.onCreate();
        Context ctx = getApplicationContext();
        mCheckFullScreenView = new View(ctx);
        mCheckFullScreenView.setBackgroundColor(Color.RED);
        WindowManager windowManager = (WindowManager)ctx.getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        //创建非模态、不可碰触
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                |WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        //放在左上角
        params.gravity = Gravity.START | Gravity.TOP;
        params.height = 50;
        params.width = 50;
        //设置弹出View类型
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

        windowManager.addView(mCheckFullScreenView, params);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}