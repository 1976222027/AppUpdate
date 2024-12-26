package com.mhy.appupdate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.sisong.HPatch;
import com.mhy.appupdate.constant.Constants;
import com.mhy.appupdate.http.HttpManager;
import com.mhy.appupdate.http.IHttpManager;
import com.mhy.appupdate.listener.UpdateCallback;
import com.mhy.appupdate.notify.INotification;
import com.mhy.appupdate.notify.NotificationImpl;
import com.mhy.appupdate.service.DownloadService;
import com.mhy.appupdate.util.LogUtils;
import com.mhy.appupdate.util.PermissionUtils;

import java.util.Map;

public class AppUpdater {
    /**
     * {@link #mContext}不强制要求是{@link Activity}，但能传{@link Activity}尽量传。AppUpdater本应该只专注于App更新，尽量不涉及动态权限相关的处理。如果mContext传的是{@link Activity}，则默认会校验一次动态权限。
     */
    private Context mContext;
    /**
     * AppUpdater的配置信息
     */
    private UpdateConfig mConfig;
    /**
     * 更新回调
     */
    private UpdateCallback mCallback;
    /**
     * http管理接口，可自定义实现。如：使用okHttp
     */
    private IHttpManager mHttpManager;
    /**
     * 通知栏：如果当前的通知栏的布局不满足你的需求，可通过参考 {@link NotificationImpl} 去自定义实现一个 {@link INotification}
     */
    private INotification mNotification;

    /**
     * ServiceConnection
     */
    private ServiceConnection mServiceConnection;

    /**
     * 构造
     *
     * @param context {@link Context}
     * @param config  {@link UpdateConfig}
     */
    public AppUpdater(@NonNull Context context, @NonNull UpdateConfig config) {
        HPatch.getInstance().initSo(context);
        this.mContext = context;
        this.mConfig = config;
    }

    /**
     * 设置下载更新进度回调
     *
     * @param callback 更新回调
     * @return
     */
    public AppUpdater setUpdateCallback(@Nullable UpdateCallback callback) {
        this.mCallback = callback;
        return this;
    }

    /**
     * 设置一个 {@link IHttpManager}
     *
     * @param httpManager AppUpdater内置提供{@link HttpManager} 实现。
     *                    如果不设置，将默认使用{@link HttpManager},你也可以自己去实现一个
     *                    {@link IHttpManager}。
     * @return
     */
    public AppUpdater setHttpManager(@Nullable IHttpManager httpManager) {
        this.mHttpManager = httpManager;
        return this;
    }

    /**
     * 设置一个 {@link INotification}
     *
     * @param notification 如果当前的通知栏的布局不满足你的需求，可通过参考 {@link NotificationImpl} 去自定义实现一个 {@link INotification}
     * @return
     */
    public AppUpdater setNotification(@Nullable INotification notification) {
        this.mNotification = notification;
        return this;
    }

    /**
     * 开始下载
     */
    public void start() {
        if (mConfig != null && (!TextUtils.isEmpty(mConfig.getApkUrl()) || !TextUtils.isEmpty(mConfig.getPatchUrl()))) {
            // 如果mContext是Activity,并且配置了下载路径，则默认会校验一次动态权限。
            if (mContext instanceof Activity && !TextUtils.isEmpty(mConfig.getSavePath())) {
                PermissionUtils.verifyReadAndWritePermissions((Activity) mContext, Constants.RE_CODE_STORAGE_PERMISSION);
            }

            if (mConfig.isShowNotification() && !PermissionUtils.isNotificationEnabled(mContext)) {
                LogUtils.w("Notification permission is not enabled.");
            }
            // 启动下载服务
            startDownloadService();
        } else {
            throw new IllegalArgumentException("Url must not be empty.");
        }
    }

    /**
     * 启动下载服务
     */
    private void startDownloadService() {
        Intent intent = new Intent(mContext, DownloadService.class);
        if (mCallback != null || mHttpManager != null || mNotification != null) {
            // 通过 bindService 的方式启动下载服务
            mServiceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    DownloadService.DownloadBinder binder = ((DownloadService.DownloadBinder) service);
                    if (mNotification != null) {
                        binder.start(mConfig, mHttpManager, mCallback, mNotification);
                    } else {
                        binder.start(mConfig, mHttpManager, mCallback);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };

            mContext.getApplicationContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // 通过 startService 的方式启动下载服务
            intent.putExtra(Constants.KEY_UPDATE_CONFIG, mConfig);
            mContext.startService(intent);
        }
    }

    /**
     * 取消下载
     */
    public void stop() {
        stopDownloadService();
    }

    /**
     * 停止下载服务
     */
    private void stopDownloadService() {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(Constants.KEY_STOP_DOWNLOAD_SERVICE, true);
        mContext.startService(intent);
    }

    /**
     * AppUpdater建造者, 内部构造UpdateConfig
     */
    public static class Builder {
        /**
         * {@link Context}
         */
        private Context mContext;
        /**
         * AppUpdater的配置信息
         */
        private UpdateConfig mConfig;

        /**
         * 构造
         *
         * @param context {@link Context}
         */
        public Builder(@NonNull Context context) {
            mContext = context;
            mConfig = new UpdateConfig();
        }

        /**
         * 设置APK下载地址
         *
         * @param url 下载地址
         * @return
         */
        public Builder setApkUrl(@NonNull String url) {
            mConfig.setApkUrl(url);
            return this;
        }

        /**
         * apk文件大小（单位：字节）
         */
        public Builder setApkSize(long size) {
            mConfig.setApkSize(size);
            return this;
        }

        /**
         * 设置APK文件的MD5，用于优先取缓存时通过MD5校验文件APK是否一致。
         * 缓存校验目前支持两种方式，一种是通过versionCode校验，即{@link #setVersionCode(long)}；一种是文件MD5校验，即{@link #setApkMD5(String)}。推荐使用MD5校验方式
         * 如果两种方式都设置了，则只校验MD5
         *
         * @param md5 为null表示不处理，如果设置了MD5，则缓存APK的MD5相同时，只下载一次，优先取本地缓存
         */
        public Builder setApkMD5(String md5) {
            mConfig.setApkMD5(md5);
            return this;
        }

        /**
         * 增量补丁下载地址
         *
         * @param patchUrl
         */
        public Builder setPatchUrl(String patchUrl) {
            mConfig.setPatchUrl(patchUrl);
            return this;
        }

        public Builder setPatchMD5(String md5) {
            mConfig.setPatchMD5(md5);
            return this;
        }

        public Builder setPatchSize(long size) {
            mConfig.setPatchSize(size);
            return this;
        }

        /**
         * 设置保存的路径，（建议使用默认，不做设置）
         *
         * @param path 下载保存的文件路径
         * @return
         * @deprecated 因为适配Android Q的分区存储，所以此方法已弃用，不建议再使用
         */
        @Deprecated
        public Builder setSavePath(String path) {
            mConfig.setSavePath(path);
            return this;
        }

        /**
         * 设置保存的文件名
         *
         * @param filename 下载的保存的apk文件名（默认优先取url文件名）
         */
        public Builder setSaveFilename(String filename) {
            mConfig.setSaveFilename(filename);
            return this;
        }

        /**
         * 设置是否显示通知栏
         *
         * @param isShowNotification 是否显示通知栏（默认true）
         */
        public Builder setShowNotification(boolean isShowNotification) {
            mConfig.setShowNotification(isShowNotification);
            return this;
        }

        /**
         * 设置通知ID
         *
         * @param notificationId 通知ID
         */
        public Builder setNotificationId(int notificationId) {
            mConfig.setNotificationId(notificationId);
            return this;
        }

        /**
         * 设置通知通道ID
         *
         * @param channelId 通知通道ID（默认兼容O）
         */
        public Builder setChannelId(String channelId) {
            mConfig.setChannelId(channelId);
            return this;
        }

        /**
         * 设置通知通道名称
         *
         * @param channelName 通知通道名称（默认兼容O）
         */
        public Builder setChannelName(String channelName) {
            mConfig.setChannelName(channelName);
            return this;
        }

        /**
         * 设置通知图标
         *
         * @param icon 通知栏图标（默认取App的icon）
         */
        public Builder setNotificationIcon(@DrawableRes int icon) {
            mConfig.setNotificationIcon(icon);
            return this;
        }

        /**
         * 设置通知是否震动提示
         *
         * @param vibrate 是否震动提示，为true时使用通知默认震动，Android O(8.0)以上设置，只有初次创建channel时有效，后续修改属性无效，想要重新有效需修改channelId或卸载App重装。
         */
        public Builder setVibrate(boolean vibrate) {
            mConfig.setVibrate(vibrate);
            return this;
        }

        /**
         * 设置通知是否铃声提示
         *
         * @param sound 是否铃声提示，为true时使用通知默认铃声，Android O(8.0)以上设置，只有初次创建channel时有效，后续修改属性无效，想要重新有效需修改channelId或卸载App重装。
         */
        public Builder setSound(boolean sound) {
            mConfig.setSound(sound);
            return this;
        }


        /**
         * 设置下载完成后知否自动触发安装APK
         *
         * @param isInstallApk 下载完成后是否自动调用安装APK（默认true）
         */
        public Builder setInstallApk(boolean isInstallApk) {
            mConfig.setInstallApk(isInstallApk);
            return this;
        }

        /**
         * 设置FileProvider的authority
         *
         * @param authority FileProvider的authority（默认兼容N，默认值{@link Context#getPackageName() + ".AppUpdaterFileProvider"}）
         */
        public Builder setAuthority(String authority) {
            mConfig.setAuthority(authority);
            return this;
        }

        /**
         * 设置下载时，通知栏是否显示下载百分比
         *
         * @param showPercentage 下载时通知栏是否显示百分比
         */
        public Builder setShowPercentage(boolean showPercentage) {
            mConfig.setShowPercentage(showPercentage);
            return this;
        }

        /**
         * 设置下载失败时，是否支持点击通知栏重新下载。与之相关联的方法{@link #setReDownloads(int)}
         *
         * @param reDownload 下载失败时是否支持点击通知栏重新下载，默认true
         */
        public Builder setReDownload(boolean reDownload) {
            mConfig.setReDownload(reDownload);
            return this;
        }

        /**
         * 设置下载失败时，最多重新下载次数。与之相关联的方法{@link #setReDownload(boolean)}
         *
         * @param reDownloads 下载失败时是否支持点击通知栏重新下载，默认最多重新下载3次
         */
        public Builder setReDownloads(int reDownloads) {
            mConfig.setReDownloads(reDownloads);
            return this;
        }

        /**
         * 设置要下载APK的versionCode，用于优先取缓存时通过versionCode校验APK文件是否一致。
         * 缓存校验目前支持两种方式，一种是通过versionCode校验，即{@link #setVersionCode(long)}；一种是文件MD5校验，即{@link #setApkMD5(String)}。推荐使用MD5校验方式
         * 如果两种方式都设置了，则只校验MD5
         *
         * @param versionCode 为null表示不处理，默认不存在则下载，存在则重新下载。不为null时，表示会优先校验本地是否存在已下载版本号为versionCode的APK。
         *                    如果存在则不会重新下载(AppUpdater会自动校验packageName一致性)，直接取本地APK，反之重新下载。
         */
        public Builder setVersionCode(long versionCode) {
            mConfig.setVersionCode(versionCode);
            return this;
        }

        public Builder setVersionName(String versionName) {
            mConfig.setVersionName(versionName);
            return this;
        }

        /**
         * 请求头添加参数
         */
        public Builder addHeader(String key, String value) {
            mConfig.addHeader(key, value);
            return this;
        }

        /**
         * 请求头添加参数
         *
         * @param headers 请求头参数
         */
        public Builder addHeader(Map<String, String> headers) {
            mConfig.addHeader(headers);
            return this;
        }

        /**
         * 设置是否自动删除取消下载的文件
         *
         * @param deleteCancelFile 是否删除取消下载的文件（默认为true）
         */
        public Builder setDeleteCancelFile(boolean deleteCancelFile) {
            mConfig.setDeleteCancelFile(deleteCancelFile);
            return this;
        }

        /**
         * 是否支持通过删除通知栏来取消下载（默认为：false）
         *
         * @param supportCancelDownload
         * @return
         */
        public Builder setSupportNotifyCancelDownload(boolean supportCancelDownload) {
            mConfig.setSupportNotifyCancelDownload(supportCancelDownload);
            return this;
        }

        /**
         * 构建 AppUpdater
         *
         * @return {@link AppUpdater}
         */
        public AppUpdater build() {
            if (mContext == null) {
                throw new NullPointerException("Context must not be null.");
            }
            return new AppUpdater(mContext, mConfig);
        }
    }
}