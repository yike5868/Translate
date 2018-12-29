package com.zlin.translate;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by zhanglin03 on 2018/11/28.
 */

public class DrawActivity extends Activity implements View.OnClickListener {
    RelativeLayout rl_main;
    DrawRectangleView drawRectangleView;
    Button btn_finish,btn_cancel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawRectangleView = new DrawRectangleView(this);
        setContentView(R.layout.activity_draw);
        initView();
    }
    public void initView(){
        rl_main = (RelativeLayout)findViewById(R.id.rl_main);
        rl_main.addView(new DrawRectangleView(DrawActivity.this));
        btn_finish = (Button)findViewById(R.id.btn_finish);
        btn_cancel = (Button)findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);
        btn_finish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_finish:

            case R.id.btn_cancel:
                DrawActivity.this.finish();
                break;
        }
    }
}
