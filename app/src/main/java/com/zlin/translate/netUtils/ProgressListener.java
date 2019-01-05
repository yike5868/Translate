package com.zlin.translate.netUtils;

/**
 * Created by zhanglin03 on 2019/1/3.
 */


public interface ProgressListener {
    /**
     * 显示进度
     *
     * @param mProgress
     */
    public void onProgress(int mProgress,long contentSize);

    /**
     * 完成状态
     *
     * @param totalSize
     */
    public void onDone(long totalSize);
}