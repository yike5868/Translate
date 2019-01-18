package com.zlin.translate.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zlin.translate.BaseActivity;
import com.zlin.translate.Constant;
import com.zlin.translate.R;
import com.zlin.translate.utils.CameraUtil;
import com.zlin.translate.utils.ImageUtils;
import com.zlin.translate.utils.ToastUtil;

import java.io.File;

import top.zibin.luban.OnCompressListener;

/**
 * Created by zhanglin03 on 2019/1/16.
 */

public class TakeCarmeraActivity  extends BaseActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private int isChong=0;// 是否重新拍摄 1是新拍  2 是重新拍摄  0是第一次
    private Camera mCamera;
    private SurfaceView surfaceView;
    private SurfaceHolder mHolder;
    private int mCameraId = 0;
    private Context context;
    //屏幕宽高
    private int screenWidth;
    private int screenHeight;
    private LinearLayout home_custom_top_relative;
    private ImageView camera_delay_time;
    private View homeCustom_cover_top_view;
    private View homeCustom_cover_bottom_view;
    private View home_camera_cover_top_view;
    private View home_camera_cover_bottom_view;
    private ImageView flash_light;
    private TextView camera_delay_time_text;
    private ImageView camera_square;
    public final static int DETAIL_RESULT_UPLOD = 5;
    private int index;
    //底部高度 主要是计算切换正方形时的动画高度
    private int menuPopviewHeight;
    //动画高度
    private int animHeight;
    //闪光灯模式 0:自动 1: 开启 2: 关闭
    private int light_num = 2;
    //延迟时间
    private int delay_time;
    private boolean is_camera_delay;
    private ImageView camera_frontback;
    private ImageView camera_close;
    private Button action_button;
    int isONe=1;
    boolean limit = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_take_carmera);
        context = this;
        getIntents();
        initView();
        initData();



    }

    private void getIntents() {
        limit = getIntent().getBooleanExtra("limit",true);
    }

    private void initView() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams params = surfaceView.getLayoutParams();
                int measuredWidth = surfaceView.getMeasuredWidth();
                params.height = measuredWidth * 4 / 3;
                surfaceView.setLayoutParams(params);
            }
        });
        action_button = (Button) findViewById(R.id.action_button);

        action_button.setOnClickListener(this);
        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        //关闭相机界面按钮
        camera_close = (ImageView) findViewById(R.id.camera_close);
        camera_close.setOnClickListener(this);

        //top 的view
        home_custom_top_relative = (LinearLayout) findViewById(R.id.home_custom_top_relative);
        home_custom_top_relative.setAlpha(0.5f);

        //前后摄像头切换
        camera_frontback = (ImageView) findViewById(R.id.camera_frontback);
        camera_frontback.setOnClickListener(this);

        //延迟拍照时间
        camera_delay_time = (ImageView) findViewById(R.id.camera_delay_time);
        camera_delay_time.setOnClickListener(this);

        //正方形切换
        camera_square = (ImageView) findViewById(R.id.camera_square);
        camera_square.setOnClickListener(this);

        //切换正方形时候的动画
        homeCustom_cover_top_view = findViewById(R.id.homeCustom_cover_top_view);
        homeCustom_cover_bottom_view = findViewById(R.id.homeCustom_cover_bottom_view);

        homeCustom_cover_top_view.setAlpha(0.5f);
        homeCustom_cover_bottom_view.setAlpha(0.5f);

        //拍照时动画
        home_camera_cover_top_view = findViewById(R.id.home_camera_cover_top_view);
        home_camera_cover_bottom_view = findViewById(R.id.home_camera_cover_bottom_view);
        home_camera_cover_top_view.setAlpha(1);
        home_camera_cover_bottom_view.setAlpha(1);

        flash_light = (ImageView) findViewById(R.id.flash_light);
        flash_light.setOnClickListener(this);

        camera_delay_time_text = (TextView) findViewById(R.id.camera_delay_time_text);

    }

    private void initData() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        menuPopviewHeight = screenHeight - screenWidth * 4 / 3;
        animHeight = (screenHeight - screenWidth - menuPopviewHeight - ImageUtils.dp2px(context, 44)) / 2;
//        getLocalPhoto(clueId);


        //这里相机取景框我这是为宽高比3:4 所以限制底部控件的高度是剩余部分
//        RelativeLayout.LayoutParams bottomParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, menuPopviewHeight);
//        bottomParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        homecamera_bottom_relative.setLayoutParams(bottomParam);

//        screenUtils.setView(point);

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int what = msg.what;
            switch (what) {
                case 0:
                    if (delay_time > 0) {
                        camera_delay_time_text.setText("" + delay_time);
                    }

                    try {
                        if (delay_time == 0) {
                            captrue();
                            is_camera_delay = false;
                            camera_delay_time_text.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        return;
                    }

                    break;

                case 2:
                    is_camera_delay = false;
                    break;

            }
        }
    };


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.action_button) {
            switch (light_num) {
                case 0:
                    //自动
                    CameraUtil.getInstance().turnLightAuto(mCamera);
                    break;
                case 1:
                    CameraUtil.getInstance().turnLightOn(mCamera);
                    break;
                case 2:
                    //关闭
                    CameraUtil.getInstance().turnLightOff(mCamera);
                    break;
            }
            captrue();
        } else if (i == R.id.camera_square) {
            if (index == 0) {
                camera_square_0();
            } else if (index == 1) {
                camera_square_1();
            }
            //前后置摄像头拍照
        } else if (i == R.id.camera_frontback) {
            switchCamera();


            //退出相机界面 释放资源
        } else if (i == R.id.camera_close) {
            if (is_camera_delay) {
                Toast.makeText(TakeCarmeraActivity.this, "正在拍照请稍后...", Toast.LENGTH_SHORT).show();
                return;
            }
            else{
                TakeCarmeraActivity.this.finish();
            }
            //闪光灯
        } else if (i == R.id.flash_light) {

            if (mCameraId == 1) {
                //前置
                ToastUtil.makeText("请切换为后置摄像头开启闪光灯");
                return;
            }
            if (mCamera == null) {
                ToastUtil.makeText("请检查相机相关权限是否打开！");
                return;
            }
            Camera.Parameters parameters;
            try {
                parameters = mCamera.getParameters();
            }catch(Exception e){
                e.printStackTrace();
                return;
            }
            switch (light_num) {
                case 0:
                    //打开
                    light_num = 1;
                    flash_light.setImageResource(R.mipmap.icon_sgd_dk);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);//开启
                    mCamera.setParameters(parameters);
                    break;
                case 1:
                    //关闭
                    light_num = 2;
                    //关闭
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mCamera.setParameters(parameters);
                    flash_light.setImageResource(R.mipmap.icon_sgd_gb);
                    break;
                case 2:
                    //自动
                    light_num = 0;
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    mCamera.setParameters(parameters);
                    flash_light.setImageResource(R.mipmap.icon_shanguangdeng);
                    break;
            }


            //延迟拍照时间
        } else if (i == R.id.camera_delay_time) {
            switch (delay_time) {
                case 0:
                    delay_time = 0;
                    break;

                case 3:
                    delay_time = 0;
                    break;

                case 5:
                    delay_time = 0;
                    break;

                case 10:
                    delay_time = 0;
                    break;
            }
        }
    }

    public void switchCamera() {
        if (mCamera == null) {
            ToastUtil.makeText("请检查相机相关权限是否打开！");
            return;
        }
        releaseCamera();
        mCameraId = (mCameraId + 1) % mCamera.getNumberOfCameras();
        mCamera = getCamera(mCameraId);
        if (mHolder != null) {
            startPreview(mCamera, mHolder);
        }
    }

    /**
     * 正方形拍摄
     */
    public void camera_square_0() {
//        camera_square.setImageResource(R.drawable.btn_camera_size1_n);

        //属性动画
        ValueAnimator anim = ValueAnimator.ofInt(0, animHeight);
        anim.setDuration(100);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = Integer.parseInt(animation.getAnimatedValue().toString());
                RelativeLayout.LayoutParams Params = new RelativeLayout.LayoutParams(screenWidth, currentValue);
                Params.setMargins(0, ImageUtils.dp2px(context, 44), 0, 0);
                homeCustom_cover_top_view.setLayoutParams(Params);

                RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(screenWidth, currentValue);
                bottomParams.setMargins(0, screenHeight - menuPopviewHeight - currentValue, 0, 0);
                homeCustom_cover_bottom_view.setLayoutParams(bottomParams);
            }

        });
        anim.start();

        homeCustom_cover_top_view.bringToFront();
        home_custom_top_relative.bringToFront();
        homeCustom_cover_bottom_view.bringToFront();
        index++;
    }

    /**
     * 长方形方形拍摄
     */
    public void camera_square_1() {
//        camera_square.setImageResource(R.drawable.btn_camera_size2_n);

        ValueAnimator anim = ValueAnimator.ofInt(animHeight, 0);
        anim.setDuration(100);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = Integer.parseInt(animation.getAnimatedValue().toString());
                RelativeLayout.LayoutParams Params = new RelativeLayout.LayoutParams(screenWidth, currentValue);
                Params.setMargins(0, ImageUtils.dp2px(context, 44), 0, 0);
                homeCustom_cover_top_view.setLayoutParams(Params);

                RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(screenWidth, currentValue);
                bottomParams.setMargins(0, screenHeight - menuPopviewHeight - currentValue, 0, 0);
                homeCustom_cover_bottom_view.setLayoutParams(bottomParams);
            }
        });
        anim.start();
        index = 0;
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isONe!=1){
            if (mCamera == null) {

                mCamera = getCamera(mCameraId);

                if (mHolder != null) {
                    new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            try{
                                Thread.sleep(50);
                            }catch (Exception e){

                            }
                            startPreview(mCamera, mHolder);
                        }
                    }.start();

                }
            }
        }



    }



    @Override
    public void onPause() {
        super.onPause();
        try {
            releaseCamera();
        }catch (Exception e){
            e.printStackTrace();
            mCamera = null;
        }
//        Log.e("TGA","releaseCamera");
    }

    /**
     * 获取Camera实例
     *
     * @return
     */
    private Camera getCamera(int id) {
        //魅族无权限解决点击闪光灯退出问题
//        PackageManager pm = getPackageManager();
//        boolean permission = (PackageManager.PERMISSION_GRANTED ==
//                pm.checkPermission("android.permission.CAMERA", "com.yixincapital.oa"));
//        if (permission) {
//        }else {
//            return null;
//        }


        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {

        }
        return camera;
    }

    /**
     * 预览相机
     */
    private void startPreview(Camera camera, SurfaceHolder holder) {
        if (camera == null) {

        } else {
            try{

// setParameters 是针对魅族MX5。MX5通过Camera.open()拿到的Camera对象不为null

                Camera.Parameters mParameters = mCamera.getParameters();

                mCamera.setParameters(mParameters);
                mCamera.stopPreview();
                isONe++;
                holder.removeCallback(this);
                camera.setPreviewDisplay(holder);
                setupCamera(camera);

                //亲测的一个方法 基本覆盖所有手机 将预览矫正
                CameraUtil.getInstance().setCameraDisplayOrientation(this, mCameraId, camera);
//            camera.setDisplayOrientation(90);
                camera.startPreview();
            }catch(Exception e) {

                ToastUtil.makeText("请检查相机相关权限是否打开！");

            }

        }


    }

    /**
     * 保存图片
     */
    private void captrue() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                //将data 转换为位图 或者你也可以直接保存为文件使用 FileOutputStream
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap saveBitmap = CameraUtil.getInstance().setTakePicktrueOrientation(mCameraId, bitmap);

                File file = new File(Constant.PHOTO_PATH);
                if(!file.exists()){
                    file.mkdirs();
                }
                //然后获取图片全路径
                String img_path = Constant.PHOTO_PATH +
                        File.separator + System.currentTimeMillis() + ".jpg";

                //需要chuan
                ImageUtils.saveJPGE_After(context, saveBitmap, img_path, 100);
                ImageUtils.luBanSave(context, new File(img_path), new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
//                photoPath=img_path;

//                image_path = img_path;
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }

                if (!saveBitmap.isRecycled()) {
                    saveBitmap.recycle();
                }
            }
        });
    }

    /**
     * 设置
     */
    private void setupCamera(Camera camera) {
        if (camera == null) {
            return;
        }

        try {
            Camera.Parameters parameter = camera.getParameters();

            Camera.Size s = CameraUtil.getInstance().getCameraSizeThree(parameter.getSupportedPictureSizes(), parameter.getSupportedPictureSizes().get(0), CameraUtil.GET_PICTURE_SIZE, 1);
            Camera.Size s1 = CameraUtil.getInstance().getCameraSizeThree(parameter.getSupportedPreviewSizes(), parameter.getSupportedPreviewSizes().get(0), CameraUtil.GET_PREVIEW_SIZE, 1);

            parameter.setPreviewSize(s1.width, s1.height);
            parameter.setPictureSize(s.width, s.height);
            if (parameter.getSupportedFocusModes().contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameter.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            camera.setParameters(parameter);
        } catch (Exception e) {
            System.out.println("" + e.getMessage());
        }
    }

    /**
     * 释放相机资源
     */
    private void releaseCamera() {
//        screenUtils.stop();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                //加载缩略图
                String pathXin = data.getStringExtra("path");

                break;
            case DETAIL_RESULT_UPLOD:
                //重新拍摄
                String isSave = data.getStringExtra("path");

                break;

        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

    }
    @Override
    public void onBackPressed() {
        if (is_camera_delay) {
            Toast.makeText(TakeCarmeraActivity.this, "正在拍照请稍后...", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (isChong==2){
//            isChong=1;
//            basic_rl.setVisibility(View.GONE);
//            search.setVisibility(View.VISIBLE);
//        }else {
//
//        }
        TakeCarmeraActivity.this.finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);


        }
        if (mCamera == null) {
            ToastUtil.makeText("请检查相机相关权限是否打开！");

        }else {
            try{

// setParameters 是针对魅族MX5。MX5通过Camera.open()拿到的Camera对象不为null

                Camera.Parameters mParameters = mCamera.getParameters();

                mCamera.setParameters(mParameters);
                startPreview(mCamera, holder);
            }catch(Exception e) {

                ToastUtil.makeText("请检查相机相关权限是否打开！");
                mCamera = null;
            }

        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);

        }
        if (mCamera == null) {
            ToastUtil.makeText("请检查相机相关权限是否打开！");

        }else {
            try{

// setParameters 是针对魅族MX5。MX5通过Camera.open()拿到的Camera对象不为null

                Camera.Parameters mParameters = mCamera.getParameters();

                mCamera.setParameters(mParameters);
                mCamera.stopPreview();
                startPreview(mCamera, holder);
            }catch(Exception e) {

                ToastUtil.makeText("请检查相机相关权限是否打开！");

            }

        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCamera();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
}

