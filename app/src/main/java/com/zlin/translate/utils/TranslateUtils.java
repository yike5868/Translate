package com.zlin.translate.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zlin.tools.Url;
import com.zlin.tools.baidu.TransApi;
import com.zlin.tools.baidu.TranslateBaiduDTO;
import com.zlin.translate.Constant;
import com.zlin.translate.ConstantHandler;
import com.zlin.translate.MainActivity;
import com.zlin.translate.TranslateApp;
import com.zlin.translate.model.GoogleTranslate;
import com.zlin.translate.netUtils.BaseCallBack;
import com.zlin.translate.netUtils.BaseOkHttpClient;
import com.zlin.translate.permission.FloatWindowManager;

import java.io.IOException;

import okhttp3.Call;

/**
 * Created by zhanglin03 on 2019/1/18.
 */

public class TranslateUtils {


    private TranslateUtils(){

    }
    private static class TranslateUtilsHolder{
        private final static TranslateUtils instance=new TranslateUtils();
    }
    public static TranslateUtils getInstance(){
        return TranslateUtilsHolder.instance;
    }


    public void translate(String text, Handler handler, Context context){
        if(TextUtils.isEmpty(text)){
            ToastUtil.makeText("识别失败！");
            return;
        }
        if (TranslateApp.getInstance().getSetModel().getTranslateType() == Constant.TYEP_BAIDU) {
            baiduTranslate(text,handler);
        } else {
            gooleTranslate(text,handler);
        }
    }


    private void baiduTranslate(String text,Handler myHandler) {

        TransApi api = new TransApi();
        String str = api.getTransResult(text, "auto", "zh");
        Log.e("baidusdk", str);
        TranslateBaiduDTO translateBaiduDTO = new Gson().fromJson(str, TranslateBaiduDTO.class);

        if (translateBaiduDTO.getError_msg() != null) {
            ToastUtil.makeText(translateBaiduDTO.getError_msg());
            return;
        }
        String baiduStr = "";
        for (TranslateBaiduDTO.TransResultBean bean : translateBaiduDTO.getTrans_result()) {
            baiduStr += bean.getSrc() + "\n";
            baiduStr += bean.getDst() + "\n";
        }
        Message message = myHandler.obtainMessage();
        message.what = ConstantHandler.MSG_OCR_TEXT;
        message.obj = baiduStr;
        myHandler.sendMessage(message);
    }

    private void gooleTranslate(String text,final Handler myHandler) {

        String url = Url.GOOGLE_TRANSLATE_URL + text;
        BaseOkHttpClient.newBuilder()
                .post()
                .url(url)
                .build()
                .enqueue(new BaseCallBack<GoogleTranslate>() {
                    @Override
                    public void onSuccess(GoogleTranslate o) {
                        StringBuffer values = new StringBuffer();
                        for (GoogleTranslate.SentencesBean sb : o.getSentences()) {
                            values.append(sb.getOrig() + ("\n"));
                            values.append(sb.getTrans() + ("\n"));
                        }
                        Log.e("google",values.toString());

                        Message message = myHandler.obtainMessage();
                        message.what = ConstantHandler.MSG_OCR_TEXT;
                        message.obj = values;
                        myHandler.sendMessage(message);

                    }

                    @Override
                    public void onError(int code) {
                        ToastUtil.makeText("错误编码：" + code);
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        ToastUtil.makeText("失败：" + e.toString());
                    }
                });
    }
}
