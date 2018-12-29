package com.zlin.translate;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zhanglin03 on 2018/11/28.
 */

public class DrawRectangleView extends View {
    //  声明Paint对象
    private Paint mPaint = null;
    private int StrokeWidth = 5;
    private Rect rect = new Rect(0,0,0,0);//手动绘制矩形
    public DrawRectangleView(Context context){
        super(context);
        //构建对象
        mPaint = new Paint();
        mPaint.setColor(Color.RED);

        //开启线程
        // new Thread(this).start();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //设置无锯齿
        mPaint.setAntiAlias(true);
        canvas.drawARGB(50,255,227,0);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(StrokeWidth);
//        mPaint.setColor(Color.GREEN);
        mPaint.setAlpha(100);
        // 绘制绿色实心矩形
//        canvas.drawRect(100, 200, 400, 200 + 400, mPaint);
        mPaint.setColor(Color.RED);
        canvas.drawRect(rect,mPaint);
    }
    Rect old;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                rect.right+=StrokeWidth;
                rect.bottom+=StrokeWidth;
                invalidate(rect);
                rect.left = x;
                rect.top = y;
                rect.right =rect.left;
                rect.bottom = rect.top;
            case MotionEvent.ACTION_MOVE:
                old =
                        new Rect(rect.left,rect.top,rect.right+StrokeWidth,rect.bottom+StrokeWidth);
                rect.right = x;
                rect.bottom = y;
                old.union(x,y);
                invalidate(old);
                break;
            case MotionEvent.ACTION_UP:
                Log.e("xxxxxx",rect.left+"");
                Log.e("yyyyyy",rect.top+"");
                Log.e("width",rect.width()+"");
                Log.e("height",rect.height()+"");
                UserConfig.x = rect.left;
                if(UserConfig.orientation== Configuration.ORIENTATION_PORTRAIT )
                    UserConfig.y = rect.top-UserConfig.titleHeight;
                else
                    UserConfig.y = rect.top;
                UserConfig.width = rect.width();
                UserConfig.higth =rect.height();
                break;
            default:
                break;
        }
        return true;//处理了触摸信息，消息不再传递
    }



}