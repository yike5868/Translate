package com.zlin.translate;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
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
import android.widget.TextView;
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
import com.umeng.analytics.MobclickAgent;
import com.zlin.tools.Url;
import com.zlin.tools.baidu.TransApi;
import com.zlin.tools.baidu.TranslateBaiduDTO;
import com.zlin.translate.model.VersionDTO;
import com.zlin.translate.netUtils.BaseCallBack;
import com.zlin.translate.netUtils.BaseOkHttpClient;


import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import me.wangyuwei.flipshare.FlipShareView;
import okhttp3.Call;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    Button btn_show, btn_hide,btn_share;
    TextView tv_hit;
    Intent permissintent;
    boolean showFloatWindow = true;
    boolean canTouch = true;

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
        FileUtils.copyToSD(getApplicationContext(), Constant.LANGUAGE_PATH, Constant.DEFAULT_LANGUAGE_NAME);
    }


    public void getTitleHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            UserConfig.titleHeight = getResources().getDimensionPixelSize(resourceId);
        }
    }

    public void initView() {
        btn_hide = (Button) findViewById(R.id.btn_hide);
        btn_show = (Button) findViewById(R.id.btn_show);
        btn_share = findViewById(R.id.btn_share);
        tv_hit = findViewById(R.id.tv_hit);
        btn_show.setOnClickListener(this);
        btn_hide.setOnClickListener(this);
        btn_share.setOnClickListener(this);
    }

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantHandler.MSG_SCREEN_SHOT:
                    startScreenShot();
//                    addThread(new ShotScreenThread());
                    break;
                case ConstantHandler.MSG_TRANSLATE:
                    if (msg.obj instanceof File) {
                        File file = (File) msg.obj;
                        recGeneral(file);
//                        addThread(new OcrThread(file));
                    } else {
                        Bitmap bitmap = (Bitmap) msg.obj;
                        addThread(new OcrThread(bitmap));
                    }
                    break;
                case ConstantHandler.MSG_TEXT:
                    String text = msg.obj.toString();
                    MyWindowManager.setTranslateText(text);
                    Log.e("ocr 识别的文字", text);
                    if (Utils.isEmpty(text)) {
                        Toast.makeText(MainActivity.this, "识别失败!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    TranslateThread translateThread = new TranslateThread(text);
                    translateThread.start();
                    break;
                case ConstantHandler.MSG_OCR_ERROR:
                    Toast.makeText(MainActivity.this, "识别失败!", Toast.LENGTH_SHORT).show();
                    break;
                case ConstantHandler.MSG_OCR_TEXT:
                    if (msg.obj == null) {
                        Toast.makeText(MainActivity.this, "识别失败！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    TranslateBaiduDTO translateBaiduDTO = (TranslateBaiduDTO) msg.obj;
                    if (translateBaiduDTO.getError_msg() != null) {
                        Toast.makeText(MainActivity.this, translateBaiduDTO.getError_msg(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String baiduStr = "";
                    for (TranslateBaiduDTO.TransResultBean bean : translateBaiduDTO.getTrans_result()) {
                        baiduStr += bean.getSrc() + "\n";
                        baiduStr += bean.getDst() + "\n";
                    }
                    MyWindowManager.setTranslateText(baiduStr);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public String ocr(Bitmap bitmap) {
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(Constant.DATAPATH, "eng");//传入训练文件目录和训练文件
        tessBaseAPI.setImage(bitmap);
        String text = tessBaseAPI.getUTF8Text();
        tessBaseAPI.end();
        return text;

    }

    public String ocr(File file) {
        TessBaseAPI tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(Constant.DATAPATH, "eng");//传入训练文件目录和训练文件
        tessBaseAPI.setImage(file);
        String text = tessBaseAPI.getUTF8Text();
        tessBaseAPI.end();
        return text;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_show:
                showDis();
//                postSubmit();
                break;
            case R.id.btn_hide:
                showSuspensionView();
                break;
            case R.id.btn_draw:
                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_share:
                shareApp();
                break;

        }
    }

    /**
     * 请求到权限后在这里复制识别库
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(Constant.TAG, "onRequestPermissionsResult: " + grantResults[0]);
        switch (requestCode) {
            case Constant.PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(Constant.TAG, "onRequestPermissionsResult: copy");
                    FileUtils.copyToSD(getApplicationContext(), Constant.LANGUAGE_PATH, Constant.DEFAULT_LANGUAGE_NAME);
                }
                break;
            default:
                break;
        }
    }

    /**
     * ocr线程
     */
    class OcrThread implements Runnable {
        File file;
        Bitmap bitmap;

        public OcrThread(File file) {
            this.file = file;
        }

        public OcrThread(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            String text;
            if (file != null) {
                text = ocr(file);
                recGeneral(file);
            } else
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

        TranslateThread(String text) {
            this.text = text;
        }

        @Override
        public void run() {
            if (text == null) {
                myHandler.sendEmptyMessage(ConstantHandler.MSG_OCR_ERROR);
                return;
            }
            TransApi api = new TransApi();
            String str = api.getTransResult(text, "auto", "zh");
            Log.e("baidusdk", str);
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

    public void addThread(Runnable runnable) {
        ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(runnable);
    }

    MediaProjectionManager mediaProjectionManager;
    int width;
    int height;
    int dpi;

    private void startScreenShot() {
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
        canTouch = true;

    }

    @Override
    public void onResume() {
        super.onResume();
        if (permissintent == null)
            requestCapturePermission();
        getVersion();
        getOrientation();
    }

    private static final int REQUEST_CODE = 1;

    private void requestCapturePermission() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    public void hasFinish(String filePath) {
        canTouch = true;
        File file = new File(filePath);
        Message message = myHandler.obtainMessage();
        message.what = ConstantHandler.MSG_TRANSLATE;
        message.obj = file;
        myHandler.sendMessage(message);
    }


    public void showDis() {
        // 显示悬浮按钮
        MyWindowManager.setOnSuspensionViewClickListlistenerener(new FloatWindowView.OnSuspensionViewClickListener()

        {

            @Override
            public void onClickTranslate() {
                if(!canTouch){
                    Toast.makeText(getApplicationContext(),"请稍等正在努力翻译!",Toast.LENGTH_SHORT).show();
                    return;
                }
                canTouch = false;
                getOrientation();
                if (UserConfig.higth == null) {
                    Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                    startActivity(intent);
                    canTouch = true;
                } else {
                    ScreenShot.setUpMediaProjection(MainActivity.this, permissintent);
                    ScreenShot.getWH(MainActivity.this);
                    ScreenShot.createImageReader();
                    ScreenShot.beginScreenShot(MainActivity.this, permissintent);
                }
            }

            @Override
            public void onClickCut() {
                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
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
        if (!MyWindowManager.isFloatWindowOpAllowed(this)) {//已经开启
//            showNoFloatWindowDialog(HomeActivity.this, "悬浮窗", permissionDesc, "取消", "去设置");
        } else {
            showSuspensionView();
        }
    }

    /**
     * 显示悬浮窗
     */
    private void showSuspensionView() {
        if (showFloatWindow) {
            // 如果当前悬浮窗没有显示，则显示悬浮窗；否则更新悬浮窗；
            if (!MyWindowManager.isWindowShowing()) {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        MyWindowManager.createWindow(MainActivity.this, showFloatWindow);
                        MyWindowManager.setMenu(null, new FlipShareView.OnFlipClickListener() {
                            @Override
                            public void onItemClick(int position) {
//                                Toast.makeText(getApplicationContext(), "adadfasdf", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void dismiss() {

                            }
                        });
                    }
                });
            } else {
                myHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.updateFloatWindow(showFloatWindow);
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
        UserConfig.orientation = mConfiguration.orientation; //获取屏幕方向
    }

    /**
     * 百度ocr 识别
     */
    public void baiduOCR(File file) {

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
        }, getApplicationContext(), "6MFb1zdawaRITvVL6B51pAG9", "sQrhacKroa24bpYjaaGKaY4YpWGKb5rA");
    }

    public void recGeneral(File file) {
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
                Log.e("aaaaaaaaa", sb.toString());
                Message message = myHandler.obtainMessage();
                message.what = ConstantHandler.MSG_TEXT;
                message.obj = sb.toString();
                myHandler.sendMessage(message);
            }

            @Override
            public void onError(OCRError error) {
                Log.e("bbbbbbb", error.getMessage());
            }
        });
    }


    /**
     * 获取版本信息
     */
    private void getVersion() {
        BaseOkHttpClient.newBuilder()
                .post()
                .url(Url.GET_VERSION)
                .build()
                .enqueue(new BaseCallBack<VersionDTO>() {
                    @Override
                    public void onSuccess(VersionDTO o) {
                        if(Utils.getVerCode(MainActivity.this)<o.getData().getVersionCode()){
                            showInputDialog(o.getData().getMessage(),o.getData().getVersionPath());
//                            showDownloadProgressDialog(o.getData().getVersionPath());
                        }
//                        Toast.makeText(MainActivity.this, "成功：" + o.getData().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int code) {
                        //Toast.makeText(MainActivity.this, "错误编码：" + code, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        // Toast.makeText(MainActivity.this, "失败：" + e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showInputDialog(String text,final String path) {
    /*@setView 装入一个EditView
     */
        final TextView textView = new TextView(MainActivity.this);
        textView.setText(text);
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("升级").setView(textView);
        inputDialog.setCancelable(false);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showDownloadProgressDialog(path);
                    }
                }).show();
    }

    private void showDownloadProgressDialog(String path) {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("提示");
        progressDialog.setMessage("正在升级...");
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        new DownloadAPK(progressDialog).execute(path);
    }

    /**
     * 下载APK的异步任务
     */

    private class DownloadAPK extends AsyncTask<String, Integer, String> {
        ProgressDialog progressDialog;
        File file;

        public DownloadAPK(ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
            HttpURLConnection conn;
            BufferedInputStream bis = null;
            FileOutputStream fos = null;

            try {
                url = new URL(params[0]);
                String name = params[0];
                name = name.substring(name.lastIndexOf("/") + 1);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                int fileLength = conn.getContentLength();
                bis = new BufferedInputStream(conn.getInputStream());
                String path = Constant.DOWNLOAD_PATH;
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String fileName =Constant.DOWNLOAD_PATH + name;
                file = new File(fileName);
                if (!file.exists()) {
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    file.createNewFile();
                }
                fos = new FileOutputStream(file);
                byte data[] = new byte[4 * 1024];
                long total = 0;
                int count;
                while ((count = bis.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / fileLength));
                    fos.write(data, 0, count);
                    fos.flush();
                }
                fos.flush();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            openFile(file);
            progressDialog.dismiss();
        }

        private void openFile(File file) {
            if (file != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                MainActivity.this.startActivity(intent);
            }

        }
    }

    public void deleteTemp(){
        File file = new File(Constant.PIC_PATH);
        if(!file.exists()){
            file.mkdirs();
            return;
        }
        File[] files = file.listFiles();
        for (File f:files){
            if(f.exists()){
                f.delete();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTemp();
    }

    public void shareApp(){
        /** * 分享图片 */
        Bitmap bgimg0 = getImageFromAssetsFile("downloadpath.png");
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        share_intent.setType("image/*");  //设置分享内容的类型
        share_intent.putExtra(Intent.EXTRA_STREAM, saveBitmap(bgimg0,"img"));
        //创建分享的Dialog
        share_intent = Intent.createChooser(share_intent, "驿客截屏翻译");
        startActivity(share_intent);
    }
    /** * 将图片存到本地 */
    private static Uri saveBitmap(Bitmap bm, String picName) {
        try {
            String dir=Constant.DATAPATH+File.separator+picName+".jpg";
            File f = new File(dir);
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Uri uri = Uri.fromFile(f);
            return uri;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();    }
        return null;
    }

    /** * 从Assets中读取图片 */
    private Bitmap getImageFromAssetsFile(String fileName){
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is=am.open(fileName);
            image=BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();    }
        return image;
    }
}
