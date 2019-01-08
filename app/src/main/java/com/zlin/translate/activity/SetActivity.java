package com.zlin.translate.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.zlin.translate.BaseActivity;
import com.zlin.translate.Constant;
import com.zlin.translate.MyWindowManager;
import com.zlin.translate.R;
import com.zlin.translate.model.SetModel;
import com.zlin.translate.views.ColorPickerView;

import org.json.JSONObject;

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
    SetModel setModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        initView();
        setListeners();
        getSet();
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
    }

    public void setListeners(){

        cpv_text.setOnColorPickerChangeListener(new ColorPickerView.OnColorPickerChangeListener() {
            @Override
            public void onColorChanged(ColorPickerView picker, int color) {
                MyWindowManager.setTextColor(color);
                setModel.setTextColor(color);
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
                setModel.setBackgroundAlpha(color);
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
                setModel.setTextAlpha(progress);
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
                setModel.setTextAlpha(progress);
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
                MyWindowManager.setBackgroundColor(Color.BLACK);
                MyWindowManager.setTextColor(Color.WHITE);
                float dd = (float) 0.5;
                MyWindowManager.setTextAlpha(dd);
                MyWindowManager.setBackgroundAlpha(dd);
                break;
        }
    }

    private void saveSet(){
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String obj2 = gson.toJson(setModel);
        editor.putString(Constant.SET_SAVE_DATE,obj2);
        editor.commit();
    }

    private void getSet(){
        SharedPreferences sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        String date = sharedPreferences.getString(Constant.SET_SAVE_DATE, "");
        if("".equals(date)){
            setModel = new SetModel();
            setModel.setBackgroundAlpha(80);
            setModel.setBackgroundColor(Color.BLACK);
            setModel.setTextAlpha(80);
            setModel.setTextColor(Color.WHITE);
        }else{
            setModel = new Gson().fromJson(date, SetModel.class);
        }
    }

    private void setSave(){
        acsb_text.setProgress(setModel.getTextAlpha());
        acsb_background.setProgress(setModel.getBackgroundAlpha());
        cpv_text.setIndicatorColor(setModel.getTextColor());
        cpv_background.setIndicatorColor(setModel.getBackgroundColor());
    }
}
