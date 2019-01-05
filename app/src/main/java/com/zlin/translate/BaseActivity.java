package com.zlin.translate;

import android.app.Activity;

import com.umeng.analytics.MobclickAgent;

/**
 * Created by zhanglin03 on 2019/1/3.
 */

public class BaseActivity extends Activity {
    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
