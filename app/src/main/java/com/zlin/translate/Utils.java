package com.zlin.translate;

import android.app.Activity;
import android.content.pm.PackageManager;

/**
 * Created by zhanglin03 on 2018/12/28.
 */

public class Utils {
    public static boolean isEmpty(String str){
        if(str == null || "".equals(str)){
            return true;
        }else
            return false;
    }
    public static int getVerCode(Activity activity) {

        int verCode = -1;
        try {
            verCode = activity.getPackageManager().getPackageInfo(
                    "com.zlin.translate", 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            verCode = -1;
        }
        return verCode;
    }
}
