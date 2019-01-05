package com.zlin.translate;

import android.os.Environment;

import java.io.File;

/**
 * Created by zhanglin03 on 2018/11/27.
 */

public class Constant {
    /**
     * TAG
     */
    public static final String TAG = "zlTranslate";
    /**
     * TessBaseAPI初始化用到的第一个参数，是个目录。
     */
    public static final String DATAPATH = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"zlTranslate";

    /**
     * 下载的地址
     */
    public static final String DOWNLOAD_PATH = DATAPATH+File.separator+"downLoad";
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    public static final String tessdata = DATAPATH + File.separator + "tessdata";
    /**
     * 在DATAPATH中新建这个目录，TessBaseAPI初始化要求必须有这个目录。
     */
    public static final String ERROR_PATH = DATAPATH + File.separator + "log";
    /**
     * 图片保存目录
     */
    public static final String PIC_PATH = DATAPATH +File.separator+"screenShot";
    /**
     * TessBaseAPI初始化测第二个参数，就是识别库的名字不要后缀名。
     */
    public static final String DEFAULT_LANGUAGE = "eng";
    /**
     * assets中的文件名
     */
    public static final String DEFAULT_LANGUAGE_NAME = DEFAULT_LANGUAGE + ".traineddata";
    /**
     * 保存到SD卡中的完整文件名
     */
    public static final String LANGUAGE_PATH = tessdata + File.separator + DEFAULT_LANGUAGE_NAME;

    /**
     * 权限请求值
     */
    public static final int PERMISSION_REQUEST_CODE=0;
}
