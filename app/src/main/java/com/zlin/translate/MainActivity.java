package com.zlin.translate;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.Word;
import com.baidu.ocr.sdk.model.WordSimple;
import com.google.gson.Gson;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.zlin.tools.baidu.TransApi;
import com.zlin.tools.baidu.TranslateBaiduDTO;


import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_show,btn_hide;
    Intent permissintent;
    boolean showFloatWindow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.PERMISSION_REQUEST_CODE);
            }
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
            }
        }
        initAccessTokenWithAkSk();
        getTitleHeight();
        FileUtils.copyToSD(getApplicationContext(),Constant.LANGUAGE_PATH,Constant.DEFAULT_LANGUAGE_NAME);
    }


    public void getTitleHeight(){
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            UserConfig.titleHeight = getResources().getDimensionPixelSize(resourceId);
        }
    }

    public void initView(){
        btn_hide = (Button)findViewById(R.id.btn_hide);
        btn_show = (Button)findViewById(R.id.btn_show);
        btn_show.setOnClickListener(this);
        btn_hide.setOnClickListener(this);
    }

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantHandler.MSG_SCREEN_SHOT:
                    startScreenShot();
//                    addThread(new ShotScreenThread());
                    break;
                case ConstantHandler.MSG_TRANSLATE:
                    if(msg.obj instanceof File) {
                        File file = (File) msg.obj;
                        recGeneral(file);
//                        addThread(new OcrThread(file));
                    }else{
                        Bitmap bitmap = (Bitmap)msg.obj;
                        addThread(new OcrThread(bitmap));
                    }
                    break;
                case ConstantHandler.MSG_TEXT:
                    String text = msg.obj.toString();
                    MyWindowManager.setTranslateText(text);
                    Log.e("ocr 识别的文字",text);
                    if(Utils.isEmpty(text)){
                        Toast.makeText(MainActivity.this,"ocr识别失败!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    TranslateThread translateThread = new TranslateThread(text);
                    translateThread.start();
                    break;
                case ConstantHandler.MSG_OCR_ERROR:
                    Toast.makeText(MainActivity.this,"ocr识别失败!",Toast.LENGTH_SHORT).show();
                    break;
                case ConstantHandler.MSG_OCR_TEXT:
                    if(msg.obj==null){
                        Toast.makeText(MainActivity.this,"识别失败！",Toast.LENGTH_SHORT).show();
                        return ;
                    }
                   TranslateBaiduDTO translateBaiduDTO =(TranslateBaiduDTO)msg.obj;
                    if(translateBaiduDTO.getError_msg()!=null){
                        Toast.makeText(MainActivity.this,translateBaiduDTO.getError_msg(),Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String baiduStr = "";
                    for (TranslateBaiduDTO.TransResultBean bean :translateBaiduDTO.getTrans_result()){
                       baiduStr += bean.getSrc()+"\n";
                       baiduStr += bean.getDst()+"\n";
                    }
                    MyWindowManager.setTranslateText(baiduStr);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public String ocr(Bitmap bitmap){
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(Constant.DATAPATH, "eng");//传入训练文件目录和训练文件
        tessBaseAPI.setImage(bitmap);
        String text = tessBaseAPI.getUTF8Text();
        tessBaseAPI.end();
        return text;

    }
    public String ocr(File file){
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(Constant.DATAPATH, "eng");//传入训练文件目录和训练文件
        tessBaseAPI.setImage(file);
        String text = tessBaseAPI.getUTF8Text();
        tessBaseAPI.end();
        return text;

    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_show:
                showDis();
                break;
            case R.id.btn_hide:
                break;
            case R.id.btn_draw:
                Intent intent = new Intent(MainActivity.this,DrawActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 请求到权限后在这里复制识别库
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(Constant.TAG, "onRequestPermissionsResult: "+grantResults[0]);
        switch (requestCode){
            case Constant.PERMISSION_REQUEST_CODE:
                if (grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Log.i(Constant.TAG, "onRequestPermissionsResult: copy");
                    FileUtils.copyToSD(getApplicationContext(),Constant.LANGUAGE_PATH,Constant.DEFAULT_LANGUAGE_NAME);
                }
                break;
            default:
                break;
        }
    }
    /**
     * ocr线程
     */
    class OcrThread implements Runnable{
        File file;
        Bitmap bitmap;
        public OcrThread(File file){
            this.file = file;
        }
        public OcrThread(Bitmap bitmap){
            this.bitmap = bitmap;
        }
        @Override
        public void run() {
            String text ;
            if(file!=null) {
                text = ocr(file);
                recGeneral(file);
            }
            else
                text = ocr(bitmap);
           Message message = myHandler.obtainMessage();
           message.what = ConstantHandler.MSG_TEXT;
           message.obj = text;
           myHandler.sendMessage(message);
        }
    }
    /**
     * 翻译线程
     */
    class TranslateThread extends Thread {
        String text;
        TranslateThread(String text){
            this.text = text;
        }
        @Override
        public void run() {
            if(text == null){
                myHandler.sendEmptyMessage(ConstantHandler.MSG_OCR_ERROR);
                return;
            }
            TransApi api = new TransApi();
            String str =  api.getTransResult(text, "auto", "zh");
            Log.e("baidusdk",str);
            TranslateBaiduDTO translateBaiduDTO = new Gson().fromJson(str, TranslateBaiduDTO.class);
            Message message = myHandler.obtainMessage();
            message.what = ConstantHandler.MSG_OCR_TEXT;
            message.obj = translateBaiduDTO;
            myHandler.sendMessage(message);
        }
    }
    /**
     * 线程池管理线程
     */

    public void addThread(Runnable runnable){
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(runnable);
    }

    MediaProjectionManager mediaProjectionManager;
    int width;
    int height;
    int dpi;
    private void startScreenShot(){
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            width = displayMetrics.widthPixels;
            height = displayMetrics.heightPixels;
            dpi = displayMetrics.densityDpi;
        }

        mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        if (mediaProjectionManager != null) {
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), 123);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE:
                if (null != data) {
                    permissintent = data;
                }
                break;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(permissintent == null)
         requestCapturePermission();
    }

    private static final int REQUEST_CODE = 1;

    private void requestCapturePermission() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    public void hasFinish(String filePath){
        File file = new File(filePath);
        Message message = myHandler.obtainMessage();
        message.what = ConstantHandler.MSG_TRANSLATE;
        message.obj = file;
        myHandler.sendMessage(message);
    }


    public void showDis() {
        // 显示悬浮按钮
        MyWindowManager.setOnSuspensionViewClickListlistenerener(new FloatWindowView.OnSuspensionViewClickListener() {

            @Override
            public void onClickTranslate() {
                getOrientation();
                if(permissintent == null) {

                }else {
                    ScreenShot.setUpMediaProjection(MainActivity.this, permissintent);
                    ScreenShot.getWH(MainActivity.this);
                    ScreenShot.createImageReader();
                    ScreenShot.beginScreenShot(MainActivity.this, permissintent);
                }
            }
            @Override
            public void onClickCut() {
                Intent intent = new Intent(MainActivity.this,DrawActivity.class);
                startActivity(intent);
            }
        });
        // 开启悬浮框前先请求权限
        if ("Xiaomi".equals(Build.MANUFACTURER) || "Meizu".equals(Build.MANUFACTURER)) {// 小米手机
            requestPermission();
        } else {
            showSuspensionView();
        }
    }

    /**
     * 请求用户给予悬浮窗的权限
     */
    public void requestPermission() {
        if(!MyWindowManager.isFloatWindowOpAllowed(this)){//已经开启
//            showNoFloatWindowDialog(HomeActivity.this, "悬浮窗", permissionDesc, "取消", "去设置");
        } else{
            showSuspensionView();
        }
    }
    /**
     * 显示悬浮窗
     *
     */
    private void showSuspensionView() {
        if (showFloatWindow) {
            // 如果当前悬浮窗没有显示，则显示悬浮窗；否则更新悬浮窗；
            if (!MyWindowManager.isWindowShowing()) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        MyWindowManager.createWindow(MainActivity.this, showFloatWindow);
                    }
                });
            } else {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.updateFloatWindow( showFloatWindow);
                    }
                });
            }
        } else {
            myHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (MyWindowManager.isWindowShowing())
                        MyWindowManager.removeWindow(getApplicationContext(), showFloatWindow);
                }
            });
        }
    }

    /**
     * @return 获取手机方向
     */
    public void getOrientation() {
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        UserConfig.orientation  = mConfiguration.orientation; //获取屏幕方向
    }

    /**
     * 百度ocr 识别
     */
    public void baiduOCR(File file){

    }
    /**
     * 用明文ak，sk初始化
     */
    private void initAccessTokenWithAkSk() {
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                //hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                //alertText("AK，SK方式获取token失败", error.getMessage());
            }
        }, getApplicationContext(),  "6MFb1zdawaRITvVL6B51pAG9", "sQrhacKroa24bpYjaaGKaY4YpWGKb5rA");
    }
    public void recGeneral( File file) {
        GeneralParams param = new GeneralParams();
        param.setDetectDirection(true);
        param.setImageFile(file);
        OCR.getInstance(getApplicationContext()).recognizeGeneral(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult result) {
                StringBuilder sb = new StringBuilder();
                for (WordSimple wordSimple : result.getWordList()) {
                    Word word = (Word) wordSimple;
                    sb.append(word.getWords());
                    sb.append("\n");
                }
                Log.e("aaaaaaaaa",sb.toString());
                Message message = myHandler.obtainMessage();
                message.what = ConstantHandler.MSG_TEXT;
                message.obj = sb.toString();
                myHandler.sendMessage(message);
            }

            @Override
            public void onError(OCRError error) {
                Log.e("bbbbbbb",error.getMessage());
            }
        });
    }

}
