package com.zlin.translate.utils;


/**
 * Created by yue on 2015/8/13.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Surface;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class CameraUtil {
    //降序
    private CameraDropSizeComparator dropSizeComparator = new CameraDropSizeComparator();
    //升序
    private CameraAscendSizeComparator ascendSizeComparator = new CameraAscendSizeComparator();
    private static CameraUtil myCamPara = null;
    /**
     * 获取相机拍照尺寸
     */
    public static final int GET_PICTURE_SIZE = 0;
    /**
     * 获取相机预览尺寸
     */
    public static final int GET_PREVIEW_SIZE = 1;
    /**
     * 获取录像的像素
     */
    public static final int GET_VIDEO_SIZE = -1;


    private CameraUtil() {

    }

    public static CameraUtil getInstance() {
        if (myCamPara == null) {
            myCamPara = new CameraUtil();
            return myCamPara;
        } else {
            return myCamPara;
        }
    }

    public int getRecorderRotation(int cameraId) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        return info.orientation;
    }

    /**
     * 获取所有支持的返回视频尺寸
     *
     * @param list
     * @param minHeight
     * @return
     */
    public Size getPropVideoSize(List<Size> list, int minHeight) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.height >= minHeight)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    /**
     * 保证预览方向正确
     *
     * @param activity
     * @param cameraId
     * @param camera
     */
    public void setCameraDisplayOrientation(Activity activity,
                                            int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    public Bitmap setTakePicktrueOrientation(int id, Bitmap bitmap) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        if (id == 1) {
            return bitmap;
        } else {
            Camera.getCameraInfo(id, info);
            try {
                bitmap = rotaingImageView(id, info.orientation, bitmap);
            } catch (Exception e) {
                System.out.println("e - > " + e.getMessage());
            }
        }

        return bitmap;
    }

    /***
     * 初始化输出图偏分辨率，遍历Camera可选的输出分辨率，按照从大到小的顺序将宽高相乘，取所有宽为1xxx的分辨率进行匹配，设置给PictureSize
     */
    public static int[] initPictureSize(List<Size> sizeList,int screenWidth,int screenHeight) {
        int pictureWidth = 0;
        int pictureHeight = 0;
        if (sizeList.size() > 0) {
            if (sizeList.get(0).width >sizeList.get(sizeList.size() - 1).width)//如果是从大到小，则反序
                Collections.reverse(sizeList);
            for (Size size : sizeList) {
//                LogUtil.e(TAG, "PictureSize-->" + size.width + "x" + size.height);
                if(size.height / 1000 == 0){
                    continue;
                }else if (size.height / 1000 == 1) {//拿到所有宽为1xxx的分辨率
                    pictureWidth = size.width;
                    pictureHeight = size.height;
                    if(size.width>1900 && size.width<2000 && size.height>1000 &&size.height<1100){//2、查找跟1920*1080最接近的分辨率
                        break;
                    }
                    if(size.width==screenHeight && size.height==screenWidth){//1、先查找跟屏幕分辨率一致的输出分辨率
                        break;
                    }
                    if(Math.abs(pictureWidth*1.0f/pictureHeight-screenHeight*1.0f/screenWidth)<0.1){//3、查找跟屏幕分辨率宽高比更接近的分辨率
                        break;
                    }
                }else if(size.height / 1000==2){
                    break;
                }
            }

            if (pictureWidth == 0 || pictureHeight == 0) {
                for (int i=0;i<sizeList.size();i++) {
                    Size size = sizeList.get(i);
                    if(size.height>1000 && size.height<2000 && size.width!=size.height){//去height 1000~2000中间最小的一个
                        pictureWidth = size.width;
                        pictureHeight = size.height;
                        break;
                    }
                }
            }
        } else {
            pictureWidth = screenWidth;
            pictureHeight = screenHeight;
        }
        return new int[]{pictureWidth,pictureHeight};
    }

    /**
     * @return
     */
    public Size getCameraSizeThree(List<Size> supportSizes, Size defaultCameraSize, int cameraType, int sn) {
        try {
            //比例接近的像素集合
            List<Size> nearSizes = new ArrayList<>();
            //比例相等的像素集合
            List<Size> equalSizes = new ArrayList<>();
            //控件的宽高比
            int proportion = 1080 * 1000 / 1440;
            int p = 5000;
            Iterator<Size> iterator = supportSizes.iterator();
            while (iterator.hasNext()) {
                Size size = iterator.next();
                //移除不满足比例的尺寸
                if (cameraType == GET_PICTURE_SIZE) {
                    if ((size.width * size.height > 4000000)) {
                        continue;
                    }
                } else if (cameraType == GET_VIDEO_SIZE) {
                    if ((size.width * size.height > 4915200)) {
                        continue;
                    }
                } else if (cameraType == GET_PREVIEW_SIZE) {

                }
                //获取像素的比例
                int w = size.height * 1000 / size.width;
                if (Math.abs(w - proportion) != 0) {
                    if (Math.abs(w - proportion) < Math.abs(p - proportion)) {
                        //若有更合适的，则清除之前的。重新添加
                        nearSizes.clear();
                        nearSizes.add(size);
                        p = w;
                    } else if (Math.abs(w - proportion) == Math.abs(p - proportion)) {
                        //若比例与之前相等，则直接添加.
                        nearSizes.add(size);
                    }
                } else {
                    //若比例相减等于0，则与控件比例相同。
                    equalSizes.add(size);
                }
            }
            if (equalSizes.size() == 0) {
                //若无相等比例，则从相近比例中选出
                if (nearSizes.size() == 1) {
                    //若相近比例只有一个，则直接选出
                    defaultCameraSize = nearSizes.get(0);
                } else if (nearSizes.size() > 1) {
                    //若相近比例不止一个，则筛选出像素最高的
                    Size nearSize = null;
                    for (Size entry : nearSizes) {
                        if (nearSize == null) {
                            nearSize = entry;
                        } else {
                            //上次循环的最适合比例
                            int n = nearSize.width * nearSize.height;
                            //这次循环的像素
                            int d = entry.width * entry.height;
                            //若本次循环大于上次循环，则标记本次循环。
                            if (n < d) {
                                nearSize = entry;
                            }
                        }
                    }
                    defaultCameraSize = nearSize;
                }
            } else if (equalSizes.size() == 1) {
                //若相等比例只有一个，则直接选则
                defaultCameraSize = equalSizes.get(0);
            } else if (equalSizes.size() > 1) {
                //若相等比例不止一个,则筛选出相等比例的最高像素
                Size bestSize = null;
                for (Size entry : equalSizes) {
                    if (bestSize == null) {
                        bestSize = entry;
                    } else                                                                                            {
                        //上次循环的最适合比例
                        int n = bestSize.width * bestSize.height;
                        //这次循环的像素
                        int d = entry.width * entry.height;
                        //若本次循环大于上次循环，则标记本次循环。
                        if (n < d) {
                            bestSize = entry;
                        }
                    }
                }
                defaultCameraSize = bestSize;
            }
            return defaultCameraSize;//若无最适合，则返回默认像素
        } catch (Exception e) {
            e.printStackTrace();
            return defaultCameraSize;
        }
    }

    /**
     * 把相机拍照返回照片转正
     *
     * @param angle 旋转角度
     * @return bitmap 图片
     */
    public Bitmap rotaingImageView(int id, int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //加入翻转 把相机拍照返回照片转正
        if (id == 1) {
            matrix.postScale(-1, 1);
        }
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static void saveImageToGallery(Bitmap bmp,String photoName,String clueId,int quality) {
        // 首先保存图片
        File appDir = new File(Environment.getExternalStorageDirectory(), clueId);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = photoName + ".jpg";
        File file = new File(appDir, fileName);
        System.out.println("camera Url = " + file.getPath());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
//        try {
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    file.getAbsolutePath(), fileName, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        // 最后通知图库更新
//        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }

    /**
     * 保存图片为JPEG
     *
     * @param bitmap
     * @param path
     */
    public static void saveJPGE_After(Context context, Bitmap bitmap, String path, int photoId) {
        File file = new File(path);
        makeDir(file);
        try {
            FileOutputStream out = new FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                out.flush();
                out.close();
            }
            updateResources(context, file.getPath());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void makeDir(File file) {
        File tempPath = new File(file.getParent());
        if (!tempPath.exists()) {
            tempPath.mkdirs();
        }
    }

    public static void updateResources(Context context, String path) {
        MediaScannerConnection.scanFile(context, new String[]{path}, null, null);
    }

    /**
     * 获取所有支持的预览尺寸
     *
     * @param list
     * @param minWidth
     * @return
     */
    public Size getPropPreviewSize(List<Size> list, int minWidth) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.width >= minWidth)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    /**
     * 获取所有支持的返回图片尺寸
     *
     * @param list
     * @param
     * @param minWidth
     * @return
     */
    public Size getPropPictureSize(List<Size> list, int minWidth) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.width >= minWidth)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    /**
     * 获取所有支持的返回视频尺寸
     *
     * @param list
     * @param minHeight
     * @return
     */
    public Size getPropSizeForHeight(List<Size> list, int minHeight) {
        Collections.sort(list, new CameraAscendSizeComparatorForHeight());

        int i = 0;
        for (Size s : list) {
            if ((s.height >= minHeight)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    /***
     * 计算最优的相机预览(取景)分辨率，因为是全屏取景，需要计算一个跟屏幕分辨率相同或者宽高比最接近的分辨率
     */
    public static int[] initPreviewSize(List<Size> sizeList, int screenWidth, int screenHeight) {
        int previewWidth = 0;
        int previewHeight = 0;
        if (sizeList.size() > 0) {
            if (sizeList.get(0).width < sizeList.get(sizeList.size() - 1).width) {//从小到大，则反序
                Collections.reverse(sizeList);
            }
            List<Size> tempList = new ArrayList<>();
            for (Size size : sizeList) {
                if (size.width / 1000 == 1) {
                    tempList.add(size);
                    previewWidth = size.width;
                    previewHeight = size.height;
                    if (Math.abs(previewWidth - screenHeight) < 30 && Math.abs(previewHeight - screenWidth) < 30) {//1、先查找跟屏幕分辨率差距最小的预览分辨率
                        break;
                    }
                    if (Math.abs(previewWidth * 1.0f / previewHeight - screenHeight * 1.0f / screenWidth) < 0.1) {//2、如果1中没有筛选到合适的，则查找跟屏幕分辨率高宽比最接近的分辨率
                        break;
                    }
                } else if (size.width / 1000 == 0) {
                    break;
                }
            }
            if (previewWidth == previewHeight && previewHeight != 0) {//如果上一次循环最终的宽高相等，则循环tempList，取第一个宽高不相等的值
                for (Size size : tempList) {
                    if (size.width != size.height) {
                        previewWidth = size.width;
                        previewHeight = size.height;
                        break;
                    }
                }
            }
            if (previewWidth == 0 || previewHeight == 0) {//如果没有符合条件的，则取可选宽高最大值
                for (int i = sizeList.size() - 1; i >= 0; i--) {//这一步是为了防止出现没有1000-1999区间的分辨率，那就拿2000及以上最小的分辨率，华为畅玩某机型上出现过
                    Size size = sizeList.get(i);
                    if (size.width > 1000 && size.width != size.height) {
                        previewWidth = size.width;
                        previewHeight = size.height;
                        break;
                    }
                }
            }
        } else {
            previewWidth = screenWidth;
            previewHeight = screenHeight;
        }
        return new int[]{previewWidth, previewHeight};
    }

    /***
     * 已经对Camera指定分辨率输出，所以不需要对分辨率进行调整，只需要处理照片质量(精度)即可, 在mCamera.takePicture回调中调用
     *
     * @param
     * @param photoId 当前图片的id
     * @return 位图
     */
    public static void process(String path,String photoName, int photoId,String clueId, int fileSizeKb) {
        System.out.println("CameraSize - > " + fileSizeKb);
        Bitmap bmp = BitmapFactory.decodeFile(path);//filePath
        if (photoId == 240 || photoId == 241 || photoId == 282) {//手续照片
            if (fileSizeKb > 1500) {//不大于1.5M，否则按90%精度进行压缩
                saveImageToGallery(bmp,photoName,clueId,90);
            }
        } else if (photoId == 248 || photoId == 248 || photoId == 281 || photoId == 287) {//VIN码，车辆铭牌 前、后风挡标签
            if (fileSizeKb > 800) {//不大于0.8M
                saveImageToGallery(bmp,photoName,clueId,80);
            }
        } else {//其他必拍照片和附加照片
            if (fileSizeKb > 500) {//不大于0.5M
                saveImageToGallery(bmp,photoName,clueId,75);
            }
        }
//        if (!bmp.isRecycled())
//            bmp.recycle();
//        return bmp;
    }
    /**
     * 读取图片属性：旋转的角度
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return degree;
        }
        return degree;
    }

    /**
     * 旋转图片，使图片保持正确的方向。
     * @param bitmap 原始图片
     * @param degrees 原始图片的角度
     * @return Bitmap 旋转后的图片
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0 || null == bitmap) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (null != bitmap) {
            bitmap.recycle();
        }
        return bmp;
    }

    /**
     * 根据指定目录删除文件
     */
    public static void deletePhoto(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        File file = new File(url);
        if (file.exists()) {
            file.delete();
        }
    }


    /**
     * 保存图片
     *
     * @param src      源图片
     * @param filePath 要保存到的文件
     * @param format   格式
     * @param quality  压缩质量
     * @return {@code true}: 成功<br>{@code false}: 失败
     */
    public static boolean saveBitmap2File(Bitmap src, String filePath, Bitmap.CompressFormat format, int quality) {
        if (src == null || TextUtils.isEmpty(filePath))
            return false;
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(filePath, false));
            return src.compress(format, quality, bos);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //升序 按照高度
    public class CameraAscendSizeComparatorForHeight implements Comparator<Size> {
        public int compare(Size lhs, Size rhs) {
            if (lhs.height == rhs.height) {
                return 0;
            } else if (lhs.height > rhs.height) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    public boolean equalRate(Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.03) {
            return true;
        } else {
            return false;
        }
    }

    //降序
    public class CameraDropSizeComparator implements Comparator<Size> {
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width < rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }

    //升序
    public class CameraAscendSizeComparator implements Comparator<Size> {
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }

    /**
     * 打印支持的previewSizes
     *
     * @param params
     */
    public void printSupportPreviewSize(Camera.Parameters params) {
        List<Size> previewSizes = params.getSupportedPreviewSizes();
        for (int i = 0; i < previewSizes.size(); i++) {
            Size size = previewSizes.get(i);
        }

    }

    /**
     * 打印支持的pictureSizes
     *
     * @param params
     */
    public void printSupportPictureSize(Camera.Parameters params) {
        List<Size> pictureSizes = params.getSupportedPictureSizes();
        for (int i = 0; i < pictureSizes.size(); i++) {
            Size size = pictureSizes.get(i);
        }
    }

    /**
     * 打印支持的聚焦模式
     *
     * @param params
     */
    public void printSupportFocusMode(Camera.Parameters params) {
        List<String> focusModes = params.getSupportedFocusModes();
        for (String mode : focusModes) {
        }
    }

    /**
     * 打开闪关灯
     *
     * @param mCamera
     */
    public void turnLightOn(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (flashModes == null) {
            // Use the screen as a flashlight (next best thing)
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            } else {
            }
        }
    }


    /**
     * 自动模式闪光灯
     *
     * @param mCamera
     */
    public void turnLightAuto(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (flashModes == null) {
            // Use the screen as a flashlight (next best thing)
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            } else {
            }
        }
    }


    /**
     * 关闭闪光灯
     *
     * @param mCamera
     */
    public void turnLightOff(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        // Check if camera flash exists
        if (flashModes == null) {
            return;
        }
        if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            // Turn off the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            } else {
            }
        }
    }
}
