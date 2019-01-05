package com.zlin.translate;

/**
 * Created by zhanglin03 on 2018/12/26.
 */
import java.lang.reflect.Field;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import me.wangyuwei.flipshare.FlipShareView;
import me.wangyuwei.flipshare.ShareItem;

public class FloatWindowView extends LinearLayout implements View.OnTouchListener{
    /**
     * 记录悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录悬浮窗的高度
     */
    public static int viewHeight;

    /**
     * 记录系统状态栏的高度
     */
    private static int statusBarHeight;

    /**
     * 用于更新悬浮窗的位置
     */
    private WindowManager windowManager;

    /**
     * 悬浮窗的参数
     */
    private WindowManager.LayoutParams mParams;

    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;

    /**
     * 记录手指按下时在悬浮窗的View上的横坐标的值
     */
    private float xInView;

    /**
     * 记录手指按下时在悬浮窗的View上的纵坐标的值
     */
    private float yInView;

    /**
     * 用于判断是否可点击
     */
    private boolean canClickable = true;

    /**
     * 按钮高度宽度
     */
    private float btn_02_height ;
    private float btn_02_wight;

    /**
     *
     */

    FlipShareView.OnFlipClickListener flipClickListener;
    List<String> listString;

    public void setCanClickable(boolean clickable){
        canClickable = clickable;
    }

    private OnSuspensionViewClickListener onSuspensionViewClickListener;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    public interface OnSuspensionViewClickListener{
        void onClickTranslate();
        void onClickCut();

    }
    public void setOnSuspensionViewClickListener(OnSuspensionViewClickListener listener){
        onSuspensionViewClickListener = listener;
    }
    TextView btn_01;
    TextView btn_02;
    TextView tv_text;
    TextView  btn_menu;
    public FloatWindowView(Context context) {
        super(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_window_widget, this);
        View view = findViewById(R.id.layout_floatwindow);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
        btn_01 = findViewById(R.id.btn_01);
        btn_02 = findViewById(R.id.btn_02);
        tv_text = findViewById(R.id.tv_text);
        btn_menu = findViewById(R.id.btn_menu);
        btn_02.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // TODO Auto-generated method stub
                btn_02.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                btn_02_height = btn_02.getMeasuredHeight();
                btn_02_wight = btn_02.getMeasuredWidth();
                Log.e("测试 btn_01_height：", btn_01.getMeasuredHeight()+","+btn_01.getMeasuredWidth());
            }
        });
    }

    public void setTranslateText(String text){
        tv_text.setText(text);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        String s = Build.MODEL;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();

                if (s.equalsIgnoreCase("vivo Y27") ) {
                    yDownInScreen = event.getRawY();
                }else{
                    yDownInScreen = event.getRawY() - getStatusBarHeight();
                }

                xInScreen = event.getRawX();
                if (s.equalsIgnoreCase("vivo Y27")) {
                    yInScreen = event.getRawY();
                }else{
                    yInScreen = event.getRawY() - getStatusBarHeight();
                }

                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                if (s.equalsIgnoreCase("vivo Y27")) {
                    yInScreen = event.getRawY();
                }else{
                    yInScreen = event.getRawY() - getStatusBarHeight();
                }
                // 手指移动的时候更新悬浮窗的位置
                updateViewPosition((int) (xInScreen - xInView), (int) (yInScreen - yInView));
                break;
            case MotionEvent.ACTION_UP:
                // 如果手指离开屏幕时xInScreen在xDownInScreen前后10像素的范围内，且yInScreen在yDownInScreen前后10像素的范围内，则视为触发了单击事件。
                if ((xDownInScreen-10) <= xInScreen && xInScreen <= (xDownInScreen+10)
                        && (yDownInScreen-10) <= yInScreen && yInScreen <= (yDownInScreen+10)) {
                    if(yInView<btn_02_height&&xInView<btn_02_wight){
//                        Toast.makeText(getContext(),"doclickcut",Toast.LENGTH_SHORT).show();
                        doClickCut();
                    }else if(yInView>btn_02_height&&xInView<btn_02_wight){
//                        Toast.makeText(getContext(),"domenu",Toast.LENGTH_SHORT).show();
//                        doMenu();
                    }else{
//                        Toast.makeText(getContext(),"doclicktranslate",Toast.LENGTH_SHORT).show();
                        doClickTranslate();
                    }
                } else {
                    int screenWidth = windowManager.getDefaultDisplay().getWidth();
                    int x;
                    if ((xInScreen - xInView) > (screenWidth - viewWidth) / 2) {
                        x = screenWidth;
                    } else {
                        x = 0;
                    }
                    updateViewPosition(x, (int) (yInScreen - yInView));
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 将悬浮窗的参数传入，用于更新悬浮窗的位置。
     *
     * @param params
     *            悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 更新悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition(int x, int y) {
        mParams.x = x;
        mParams.y = y;
        windowManager.updateViewLayout(this, mParams);
    }

    private void doClickTranslate() {
        onSuspensionViewClickListener.onClickTranslate();
//        int[] top = new int[2];
//        btn_01.getLocationOnScreen(top);
//        if (top[1] == 0) {
//            Toast.makeText(getContext(),"full screen",Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(getContext(),"not full screen",Toast.LENGTH_SHORT).show();
//        }
    }

    private void doClickCut() {
        onSuspensionViewClickListener.onClickCut();
        int[] top = new int[2];
        btn_01.getLocationOnScreen(top);
        if (top[1] == 0) {
//           Toast.makeText(getContext(),"full screen",Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(getContext(),"not full screen",Toast.LENGTH_SHORT).show();
        }
    }
    private void doMenu(){
//        FlipShareView share = new FlipShareView.Builder((Activity) getContext(), btn_menu)
//                .addItem(new ShareItem("背景透明度", Color.WHITE, 0xff43549C, null))
//                .addItem(new ShareItem("按钮透明度", Color.WHITE, 0xff4999F0,null))
//                .addItem(new ShareItem("文字透明度", Color.WHITE, 0xffD9392D, null))
//                .addItem(new ShareItem("资助我", Color.WHITE, 0xff57708A))
//                .setBackgroundColor(0x60000000)
//                .create();
//        share.setOnFlipClickListener(flipClickListener);

        new FlipShareView.Builder((Activity) getContext(), btn_01)
                .addItem(new ShareItem("Facebook", Color.WHITE, 0xff43549C))
                .addItem(new ShareItem("Wangyuwei", Color.WHITE, 0xff4999F0))
                .addItem(new ShareItem("Wangyuweiwangyuwei", Color.WHITE, 0xffD9392D))
                .addItem(new ShareItem("纯文字也可以", Color.WHITE, 0xff57708A))
                .setAnimType(FlipShareView.TYPE_HORIZONTAL)
                .create();
    }
    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    void setonMenu(List<String> listString,FlipShareView.OnFlipClickListener flipClickListener){
        this.listString = listString;
        this.flipClickListener = flipClickListener;
    }
}
