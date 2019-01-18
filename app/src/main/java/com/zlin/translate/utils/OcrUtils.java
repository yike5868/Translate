package com.zlin.translate.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.Word;
import com.baidu.ocr.sdk.model.WordSimple;
import com.zlin.translate.ConstantHandler;

import java.io.File;

/**
 * Created by zhanglin03 on 2019/1/18.
 */

public class OcrUtils {

    /**
     * ocr线程
     */
//    class OcrThread implements Runnable {
//        File file;
//        Bitmap bitmap;
//
//        public OcrThread(File file) {
//            this.file = file;
//        }
//
//        public OcrThread(Bitmap bitmap) {
//            this.bitmap = bitmap;
//        }
//
//        @Override
//        public void run() {
//            String text;
//            if (file != null) {
//                text = ocr(file);
//                recGeneral(file);
//            } else
//                text = ocr(bitmap);
//            Message message = myHandler.obtainMessage();
//            message.what = ConstantHandler.MSG_TEXT;
//            message.obj = text;
//            myHandler.sendMessage(message);
//        }
//    }
//
    public static void recGeneral(Context context, File file,final Handler myHandler) {
        GeneralParams param = new GeneralParams();
        param.setDetectDirection(true);
        param.setImageFile(file);
        OCR.getInstance(context).recognizeGeneral(param, new OnResultListener<GeneralResult>() {
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
                Message message = myHandler.obtainMessage();
                message.what = ConstantHandler.MSG_OCR_ERROR;
                myHandler.sendMessage(message);
            }
        });
    }
}
