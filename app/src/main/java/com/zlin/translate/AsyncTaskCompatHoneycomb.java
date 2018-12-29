package com.zlin.translate;

import android.os.AsyncTask;

class AsyncTaskCompatHoneycomb {

    @SafeVarargs
    static <Params, Progress, Result> void executeParallel(AsyncTask<Params, Progress, Result> task,
                                                           Params... params) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

}