package com.zlin.translate.utils;

import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhanglin03 on 2019/1/21.
 */

public class TextUtils {

    public static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }

    public static String replaceBlank(String src) {
        String dest = "";
        if (src != null) {
            Pattern pattern = Pattern.compile("\n");
            Matcher matcher = pattern.matcher(src);
            dest = matcher.replaceAll("");
        }
        return dest;
    }

    public static String addBlank(String src) {
        String dest = src.replaceAll(",",",\n");
        dest = dest.replaceAll("\\.","\\.\n");
        return dest;
    }
}
