package com.zlin.translate;


import android.os.AsyncTask;
/**
 * Created by zhanglin03 on 2018/11/28.
 */
public final class AsyncTaskCompat {

    @SafeVarargs
    public static <Params, Progress, Result> AsyncTask<Params, Progress, Result> executeParallel(
            AsyncTask<Params, Progress, Result> task, Params... params) {
        if (task == null) {
            throw new IllegalArgumentException("task can not be null");
        }

        AsyncTaskCompatHoneycomb.executeParallel(task, params);
        return task;
    }

    private AsyncTaskCompat() {
    }

}