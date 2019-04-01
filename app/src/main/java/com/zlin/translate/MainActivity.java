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
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.zlin.tools.Url;
import com.zlin.translate.activity.PermissionsActivity;
import com.zlin.translate.activity.SetActivity;
import com.zlin.translate.activity.TakeCarmeraActivity;
import com.zlin.translate.model.VersionDTO;
import com.zlin.translate.netUtils.BaseCallBack;
import com.zlin.translate.netUtils.BaseOkHttpClient;
import com.zlin.translate.permission.FloatWindowManager;
import com.zlin.translate.utils.OcrUtils;
import com.zlin.translate.utils.PermissionsChecker;
import com.zlin.translate.utils.ToastUtil;
import com.zlin.translate.utils.TranslateUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    Button btn_show, btn_cam, btn_hide, btn_share, btn_menu;
    TextView tv_hit;
    Intent permissintent;
    boolean canTouch = true;
    // 所需的全部权限
    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private PermissionsChecker mPermissionsChecker; // 权限检测器

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, Constant.PERMISSION_REQUEST_CODE);
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, Constant.PERMISSION_REQUEST_CODE);
            }
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
            }
        }
        mPermissionsChecker = new PermissionsChecker(this);

//        getTitleHeight();
        getVersion();
    }

    /**
     * 获取title高度， 修改为点击翻译动态
     */
    public void getTitleHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            UserConfig.titleHeight = getResources().getDimensionPixelSize(resourceId);
        }
    }

    public void initView() {
        btn_hide = findViewById(R.id.btn_hide);
        btn_show = findViewById(R.id.btn_show);
        btn_share = findViewById(R.id.btn_share);
        btn_menu = findViewById(R.id.btn_menu);
        tv_hit = findViewById(R.id.tv_hit);
        btn_cam = findViewById(R.id.btn_cam);
        btn_cam.setOnClickListener(this);
        btn_show.setOnClickListener(this);
        btn_hide.setOnClickListener(this);
        btn_share.setOnClickListener(this);
        btn_menu.setOnClickListener(this);
    }

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantHandler.MSG_SCREEN_SHOT:
                    startScreenShot();
//                    addThread(new ShotScreenThread());
                    break;
                case ConstantHandler.MSG_TRANSLATE:
                    File file = (File) msg.obj;
                    OcrUtils.getInstance().recGeneral(getApplicationContext(), file, myHandler);
                    break;
                case ConstantHandler.MSG_TEXT:
                    String text = msg.obj.toString();
                    Log.e("ocr 识别的文字", text);
                    if (Utils.isEmpty(text)) {
                        Toast.makeText(MainActivity.this, "识别失败!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    TranslateUtils.getInstance().translate(text, myHandler, MainActivity.this);
                    break;
                case ConstantHandler.MSG_OCR_ERROR:
                    Toast.makeText(MainActivity.this, "识别失败!", Toast.LENGTH_SHORT).show();
                    break;
                case ConstantHandler.MSG_OCR_TEXT:
                    if (msg.obj == null) {
                        Toast.makeText(MainActivity.this, "识别失败！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    FloatWindowManager.getInstance().setTranslateText(msg.obj.toString());
                    break;

                case ConstantHandler.MSG_PERMISSION_DENIED:
                    ToastUtil.makeText("权限获取失败，请开启浮窗权限");
                    break;
                case ConstantHandler.MSG_GET_TITLE:
                    int b = getStatusBarHeight();
                    ToastUtil.makeText("getstatus b "+b );
                    removeStatusBarHelperView();
                    clickTranslate();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_show:
                showDis();
//                postSubmit();
                break;
            case R.id.btn_hide:
                break;
            case R.id.btn_draw:
                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_share:
                shareApp();
                break;
            case R.id.btn_menu:
                showDis();
                Intent intent1 = new Intent(MainActivity.this, SetActivity.class);
                startActivity(intent1);
                break;
            case R.id.btn_cam:
                Intent intent2 = new Intent(MainActivity.this, TakeCarmeraActivity.class);
                startActivity(intent2);
                break;

        }
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

        getOrientation();
        // 缺少权限时, 进入权限配置页面
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
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

        FloatWindowManager.getInstance().setOnSuspensionViewClickListlistenerener(new FloatWindowView.OnSuspensionViewClickListener() {

            @Override
            public void onClickTranslate() {
                /**
                 * 首先判断是否是全屏， 状态栏是否显示
                 */
                initStatusBarHelperView();
                WaitThread w = new WaitThread();
                w.start();
            }

            @Override
            public void onClickCut() {

                Intent intent = new Intent(MainActivity.this, DrawActivity.class);
                startActivity(intent);
            }

        });

        FloatWindowManager.getInstance().applyOrShowFloatWindow(MainActivity.this);
        // 显示悬浮按钮

    }

    /**
     * 开始截屏翻译
     */
    public void clickTranslate(){
                if (!canTouch) {
                    Toast.makeText(getApplicationContext(), "请稍等正在努力翻译!", Toast.LENGTH_SHORT).show();
                    return;
                }
                canTouch = false;
                getOrientation();
                if (UserConfig.higth == null || UserConfig.higth == 0 || UserConfig.width == 0) {
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

    class WaitThread extends  Thread{
        @Override
        public void run() {
            super.run();
            try {
                sleep(200);
                myHandler.sendEmptyMessage(ConstantHandler.MSG_GET_TITLE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
                        if (Utils.getVerCode(MainActivity.this) < o.getData().getVersionCode()) {
                            showInputDialog(o);
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

    private void showInputDialog(final VersionDTO o) {
    /*@setView 装入一个EditView
     */
        if (o == null || o.isHasErrors()) {
            return;
        }
        final TextView textView = new TextView(MainActivity.this);
        textView.setText(o.getData().getMessage());
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("升级").setView(textView);
        if (o.getData().isMust()) {
            inputDialog.setCancelable(false);
            inputDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showDownloadProgressDialog(o.getData().getVersionPath());
                        }
                    }).show();
        } else {
            inputDialog.setNegativeButton("取消", null);
            inputDialog.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showDownloadProgressDialog(o.getData().getVersionPath());
                        }
                    }).show();
        }
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

                String fileName = Constant.DOWNLOAD_PATH + name;
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

    public void deleteTemp() {
        File file = new File(Constant.PIC_PATH);
        if (!file.exists()) {
            file.mkdirs();
            return;
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.exists()) {
                f.delete();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteTemp();
    }

    public void shareApp() {
        /** * 分享图片 */
        Bitmap bgimg0 = getImageFromAssetsFile("downloadpath.png");
        Intent share_intent = new Intent();
        share_intent.setAction(Intent.ACTION_SEND);//设置分享行为
        share_intent.setType("image/*");  //设置分享内容的类型
        share_intent.putExtra(Intent.EXTRA_STREAM, saveBitmap(bgimg0, "img"));
        //创建分享的Dialog
        share_intent = Intent.createChooser(share_intent, "驿客截屏翻译");
        startActivity(share_intent);
    }

    /**
     * 将图片存到本地
     */
    private static Uri saveBitmap(Bitmap bm, String picName) {
        try {
            String dir = Constant.DATAPATH + File.separator + picName + ".jpg";
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
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从Assets中读取图片
     */
    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    View mStatusBarHelperView;
    private void initStatusBarHelperView() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mStatusBarHelperView = new View(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        // 不可触摸，不可获得焦点
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        // 放在左上角
        lp.gravity = Gravity.LEFT | Gravity.TOP;
        // 需要在manifest里申明android.permission.SYSTEM_ALERT_WINDOW权限
        lp.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        wm.addView(mStatusBarHelperView, lp);
    }

    //使用完毕移除自定义View
    private void removeStatusBarHelperView() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.removeView(mStatusBarHelperView);
        mStatusBarHelperView = null;
    }

    /**
     *
     * @return true 状态栏隐藏;false状态栏显示
     */
    private int getStatusBarHeight() {
        int[] windowParams = new int[2];
        int[] screenParams = new int[2];
        mStatusBarHelperView.getLocationInWindow(windowParams);
        mStatusBarHelperView.getLocationOnScreen(screenParams);
        // 如果状态栏隐藏，返回0，如果状态栏显示则返回高度
        return screenParams[1] - windowParams[1];
    }

}
