package com.zlin.translate;

/**
 * Created by zhanglin03 on 2018/12/26.
 */
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

public class MyWindowManager {

    /**
     * 悬浮窗View的实例
     */
    private static FloatWindowView floatWindow;
    /**
     * 悬浮窗View的参数
     */
    private static WindowManager.LayoutParams windowParams;
    /**
     * 用于控制在屏幕上添加或移除悬浮窗
     */
    private static WindowManager mWindowManager;
    /**
     * 用于获取手机可用内存
     */
    private static ActivityManager mActivityManager;

    private static FloatWindowView.OnSuspensionViewClickListener myListener;

    /**
     * 这里是给调用者设置的一个点击监听的代理
     * @param listener
     */
    public static void setOnSuspensionViewClickListlistenerener(FloatWindowView.OnSuspensionViewClickListener listener){
        myListener = listener;
    }

    /**
     * 创建一个悬浮窗。初始位置为屏幕的右部中间位置。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    public static void createWindow(Context context, boolean canClickable) {
        WindowManager windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (floatWindow == null) {
            floatWindow = new FloatWindowView(context);
            floatWindow.setOnSuspensionViewClickListener(myListener);
            floatWindow.setCanClickable(canClickable);
            if (windowParams == null) {
                windowParams = new LayoutParams();
//                windowParams.type = LayoutParams.TYPE_PHONE;
                if("Xiaomi".equals(Build.MANUFACTURER) || Build.VERSION.SDK_INT < 21){
                    windowParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                            | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
                }else{
                    windowParams.type = LayoutParams.TYPE_TOAST;
                }
                windowParams.format = PixelFormat.RGBA_8888;
                windowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | LayoutParams.FLAG_NOT_FOCUSABLE;
                windowParams.gravity = Gravity.LEFT | Gravity.TOP;
                windowParams.width = FloatWindowView.viewWidth;
                windowParams.height = FloatWindowView.viewHeight;
                windowParams.x = screenWidth;
                windowParams.y = screenHeight / 2;
            }
            floatWindow.setParams(windowParams);
            windowManager.addView(floatWindow, windowParams);
        }
    }

    /**
     * 将悬浮窗从屏幕上移除。
     *
     * @param context
     *            必须为应用程序的Context.
     */
    public static void removeWindow(Context context, boolean canClickable) {
        if (floatWindow != null) {
            floatWindow.setCanClickable(canClickable);
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(floatWindow);
            floatWindow = null;
        }
    }
    /**
     * 设置ocr 及翻译文字
     */
    public static void setTranslateText(String text){
        if(floatWindow != null){
            floatWindow.setTranslateText(text);
        }
    }


    /**
     * 更新悬浮窗的显示图片
     * @param canClickable
     */
    public static void updateFloatWindow( boolean canClickable) {
        if (floatWindow != null) {
            floatWindow.setCanClickable(canClickable);
//            ImageView imgFloatwindow = (ImageView) floatWindow.findViewById(R.id.img_floatwindow);
//            Glide.with(context).load(imgUrl).error(null).centerCrop().into(imgFloatwindow);
        }
    }

    /**
     * 判断悬浮窗是否显示
     *
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
    public static boolean isWindowShowing() {
        return floatWindow != null;
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context
     *            必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。
     *
     * @param context
     *            可传入应用程序上下文。
     * @return ActivityManager的实例
     */
    private static ActivityManager getActivityManager(Context context) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }

    /**
     * 判断悬浮窗权限
     *
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isFloatWindowOpAllowed(Context context) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 19) {
            return checkOp(context, 24);  // AppOpsManager.OP_SYSTEM_ALERT_WINDOW
        } else {
            if ((context.getApplicationInfo().flags & 1 << 27) == 1 << 27) {
                return true;
            } else {
                return false;
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean checkOp(Context context, int op) {
        final int version = Build.VERSION.SDK_INT;

        if (version >= 19) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {

                Class<?> spClazz = Class.forName(manager.getClass().getName());
                Method method = manager.getClass().getDeclaredMethod("checkOp", int.class, int.class, String.class);
                int property = (Integer) method.invoke(manager, op,
                        Binder.getCallingUid(), context.getPackageName());

                if (AppOpsManager.MODE_ALLOWED == property) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
        }
        return false;
    }
}

