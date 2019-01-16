package com.zlin.translate.utils;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zlin.translate.R;
import com.zlin.translate.TranslateApp;

public class ToastUtil {
    private static Toast mToastInstance;
    public static void showToastShortView(String toastText) {
        if (mToastInstance == null) {
            mToastInstance = Toast.makeText(TranslateApp.sContext, "",
                    Toast.LENGTH_SHORT);
            mToastInstance.setView(makeView());
        }
        ((TextView) mToastInstance.getView().findViewById(R.id.message))
                .setText(toastText);
        mToastInstance.setGravity(Gravity.CENTER, 0, 0);
        mToastInstance.show();
    }

    public static void showToastLongView(String toastText) {
        if (mToastInstance == null) {
            mToastInstance = Toast.makeText(TranslateApp.sContext, "",
                    Toast.LENGTH_LONG);
            mToastInstance.setView(makeView());
        }
        ((TextView) mToastInstance.getView().findViewById(R.id.message))
                .setText(toastText);
        mToastInstance.setGravity(Gravity.CENTER, 0, 0);
        mToastInstance.show();
    }

    public static View makeView() {
        LayoutInflater inflate = (LayoutInflater) TranslateApp.sContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.toast_content_view, null);
        return v;
    }
    public static void makeText(String msg) {
//        Toast.makeText(OAApp.getInstance(), msg, Toast.LENGTH_SHORT).show();
        final Toast toast =  Toast.makeText(TranslateApp.getInstance(), msg, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM,0,0);
        toast.show();
        //延长土司时间
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        },5000);
    }
}
