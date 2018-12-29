package com.zlin.translate;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.Image.Plane;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

/**
 * Created by zhanglin03 on 2018/11/28.
 */


public class SaveTask extends AsyncTask<Image, Void, Bitmap> {
    private Activity activity;
    public SaveTask(Activity activity){
        this.activity = activity;
    }
    String filePath;
    @Override
    protected Bitmap doInBackground(Image... args) {
        if (null == args || 1 > args.length || null == args[0]) {
            return null;
        }

        Image image = args[0];

        int width;
        int height;
        try {
            width = image.getWidth();
            height = image.getHeight();
        } catch (IllegalStateException e) {
            return null;
        }

        final Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        // 每个像素的间距
        int pixelStride = planes[0].getPixelStride();
        // 总的间距
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                Bitmap.Config.ARGB_4444);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);

        if(UserConfig.width+UserConfig.x > width){
            UserConfig.width = width-UserConfig.x-1;
        }
        if(UserConfig.higth+UserConfig.y > height){
            UserConfig.higth -= 1;
        }
        Log.e("width height",width+"   "+height);
        Log.e("x y width heigth" , UserConfig.x+"  y:"+UserConfig.y+"  width:"+UserConfig.width+"   height"+ UserConfig.higth);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap,//原图
                UserConfig.x,//图片裁剪横坐标开始位置
                UserConfig.y,//图片裁剪纵坐标开始位置
                UserConfig.width,//要裁剪的宽度
                UserConfig.higth);//要裁剪的高度

        image.close();

//        File fileImage = saveImage(bitmap);
        File fileImage1 = saveImage(bitmap1);

        if (null != fileImage1) {
            filePath = fileImage1.getAbsolutePath();
            return null;
        }
        return null;
    }

    private File saveImage(Bitmap bitmap){
        File fileImage = null;
        if (null != bitmap) {
            FileOutputStream fos = null;
            try {
                fileImage = new File(createFile());
                if (!fileImage.exists()) {
                    fileImage.createNewFile();
                    fos = new FileOutputStream(fileImage);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                }
            } catch (IOException e) {
                fileImage = null;
                e.printStackTrace();
            } finally {
                if (null != fos) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                    }
                }
                if (null != bitmap && !bitmap.isRecycled()) {
                    bitmap.recycle();
                    bitmap = null;
                }
            }
        }
        return  fileImage;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (ScreenShot.surface.isValid()) {
            ScreenShot.surface.release();
        }
        if(activity instanceof MainActivity){
            ((MainActivity) activity).hasFinish(filePath);
        }
    }

    // 输出目录
    private String createFile() {
        String outDir = Constant.PIC_PATH;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String date = simpleDateFormat.format(new Date());
        return outDir+ File.separator + date + ".png";
    }

}