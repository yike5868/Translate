package com.zlin.translate.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.zlin.translate.BaseActivity;
import com.zlin.translate.Constant;
import com.zlin.translate.MyWindowManager;
import com.zlin.translate.R;
import com.zlin.translate.TranslateApp;
import com.zlin.translate.model.SetModel;
import com.zlin.translate.views.ColorPickerView;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by zhanglin03 on 2019/1/7.
 */

public class SetActivity extends BaseActivity implements View.OnClickListener {
    ColorPickerView cpv_text;
    AppCompatSeekBar acsb_text;
    ColorPickerView cpv_background;
    AppCompatSeekBar acsb_background;
    Spinner         spinner_service;
    Button btn_yuan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        initView();
        setListeners();
        setSave();

    }
    public void initView(){
        cpv_text = findViewById(R.id.cpv_text);
        acsb_text = findViewById(R.id.acsb_text);
        cpv_background = findViewById(R.id.cpv_background);
        acsb_background = findViewById(R.id.acsb_background);
        spinner_service = findViewById(R.id.spinner_service);
        btn_yuan = findViewById(R.id.btn_yuan);
        btn_yuan.setOnClickListener(this);

      /*
         * 动态添显示下来菜单的选项，可以动态添加元素
         */
        ArrayList<String> list = new ArrayList<String>();
        list.add("百度");
        list.add("Google");
        /*
         * 第二个参数是显示的布局
         * 第三个参数是在布局显示的位置id
         * 第四个参数是将要显示的数据
         */
        ArrayAdapter adapter2 = new ArrayAdapter(this, R.layout.spinner_item, R.id.textview,list);
        spinner_service.setAdapter(adapter2);
        spinner_service.setOnItemSelectedListener(new spinner2Listener());

    }

    class spinner2Listener implements android.widget.AdapterView.OnItemSelectedListener{


        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
           TranslateApp.getInstance().getSetModel().setTranslateType(position);
           saveSet();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            System.out.println("nothingSelect");
        }
    }

    public void setListeners(){

        cpv_text.setOnColorPickerChangeListener(new ColorPickerView.OnColorPickerChangeListener() {
            @Override
            public void onColorChanged(ColorPickerView picker, int color) {
                MyWindowManager.setTextColor(color);
                TranslateApp.getInstance().getSetModel().setTextColor(color);
                saveSet();
            }

            @Override
            public void onStartTrackingTouch(ColorPickerView picker) {
                // TODO

            }

            @Override
            public void onStopTrackingTouch(ColorPickerView picker) {
                // TODO

            }
        });
        cpv_background.setOnColorPickerChangeListener(new ColorPickerView.OnColorPickerChangeListener() {
            @Override
            public void onColorChanged(ColorPickerView picker, int color) {
                MyWindowManager.setBackgroundColor(color);
                TranslateApp.getInstance().getSetModel().setBackgroundAlpha(color);
            }

            @Override
            public void onStartTrackingTouch(ColorPickerView picker) {
                // TODO

            }

            @Override
            public void onStopTrackingTouch(ColorPickerView picker) {
                // TODO

            }
        });
        acsb_text.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TranslateApp.getInstance().getSetModel().setTextAlpha(progress);
                saveSet();
                float value = progress/(float)100;
                MyWindowManager.setTextAlpha(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        acsb_background.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TranslateApp.getInstance().getSetModel().setTextAlpha(progress);
                saveSet();
                float value = progress/(float)100;
                MyWindowManager.setBackgroundAlpha(value);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_yuan:
                TranslateApp.getInstance().getSetModel().setTextAlpha(80);
                TranslateApp.getInstance().getSetModel().setBackgroundAlpha(50);
                TranslateApp.getInstance().getSetModel().setTextColor(Color.WHITE);
                TranslateApp.getInstance().getSetModel().setBackgroundColor(Color.BLACK);
                TranslateApp.getInstance().getSetModel().setTranslateType(Constant.TYEP_BAIDU);
                setSave();
                saveSet();
                break;
        }
    }

    private void saveSet(){
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String obj2 = gson.toJson(TranslateApp.getInstance().getSetModel());
        editor.putString(Constant.SET_SAVE_DATE,obj2);
        editor.commit();
    }


    private void setSave(){
        acsb_text.setProgress(TranslateApp.getInstance().getSetModel().getTextAlpha());
        acsb_background.setProgress(TranslateApp.getInstance().getSetModel().getBackgroundAlpha());
        cpv_text.setIndicatorColor(TranslateApp.getInstance().getSetModel().getTextColor());
        cpv_background.setIndicatorColor(TranslateApp.getInstance().getSetModel().getBackgroundColor());
        spinner_service.setSelection(TranslateApp.getInstance().getSetModel().getTranslateType());
    }
}
