package com.mhy.appupdate.http;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

/**
 * IHttpManager 默认提供 {@link HttpManager} 实现。
 *
 * 
 */
public interface IHttpManager {

    /**
     * 下载
     *
     * @param url
     * @param saveFilePath
     * @param requestProperty
     * @param callback
     */
    void download(String url, String saveFilePath, @Nullable Map<String, String> requestProperty, DownloadCallback callback);

    /**
     * 取消下载
     */
    void cancel();

    interface DownloadCallback extends Serializable {
        /**
         * 开始
         *
         * @param url
         */
        void onStart(String url);

        /**
         * 加载进度…
         *
         * @param progress
         * @param total
         */
        void onProgress(long progress, long total);

        /**
         * 完成
         *
         * @param file
         */
        void onFinish(File file);

        /**
         * 错误
         *
         * @param e
         */
        void onError(Exception e);

        /**
         * 取消
         */
        void onCancel();
    }
}