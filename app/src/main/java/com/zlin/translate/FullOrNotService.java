package com.zlin.translate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by zhanglin03 on 2018/12/27.
 */

public class FullOrNotService extends Service {
    private View mView;
    private static final String TAG = "FullOrNotService";

    public FullOrNotService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mView = new Button(this);
        mView.setBackgroundColor(Color.parseColor("#ff0dd1"));
        Log.e("service","has start");
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        wmParams.gravity = Gravity.TOP | Gravity.LEFT;
        wmParams.x = 0;
        wmParams.y = 0;
        wmParams.width = 100;
        wmParams.height = 100;
        try {
            windowManager.addView(mView, wmParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Log.e("thread", "has start");
                    int[] top = new int[2];
                    mView.getLocationOnScreen(top);
                    if (top[1] == 0) {
                        Log.d(TAG, "Y " + top[1] + "current is full screen");
                    } else {
                        Log.d(TAG, "Y " + top[1] + "current is not full screen");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        ).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}