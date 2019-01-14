package com.zlin.translate;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import com.google.gson.Gson;
import com.umeng.commonsdk.UMConfigure;
import com.zlin.tools.CrashHandler;
import com.zlin.translate.model.SetModel;

/**
 * Created by zhanglin03 on 2018/12/28.
 */

public class TranslateApp extends Application {
    private static TranslateApp mInstance = null;

    private static SetModel setModel = null;

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

    public SetModel getSetModel() {
        if(setModel == null){
            SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            String date = sharedPreferences.getString(Constant.SET_SAVE_DATE, "");
            if("".equals(date)){
                setModel = new SetModel();
                setModel.setBackgroundAlpha(80);
                setModel.setBackgroundColor(Color.BLACK);
                setModel.setTextAlpha(80);
                setModel.setTextColor(Color.WHITE);
                setModel.setTranslateType(Constant.TYEP_BAIDU);
            }else{
                setModel = new Gson().fromJson(date, SetModel.class);
            }
        }
        return setModel;
    }

    public static void setSetModel(SetModel setModel) {
        TranslateApp.setModel = setModel;
    }
}
