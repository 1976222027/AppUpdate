package com.mhy.appupdate.service;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;

import com.github.sisong.HPatch;
import com.mhy.appupdate.constant.UpdateConstants;
import com.mhy.appupdate.listener.UpdateCallback;
import com.mhy.appupdate.util.AppUtils;
import com.mhy.appupdate.util.LogUtils;

import java.io.File;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * auth : littonishir
 * date : 2018/5/8
 * gith : https://github.com/littonishir
 * APP更新管理器
 * 建议放在服务里面启动
 */

public class SystemDownload {
    private DownloadManager downloadManager;
    private Context mContext;
    private long mTaskId = 0;
    private static volatile SystemDownload instance;
    private boolean isPatch = false;
    private boolean autoInstall = true;
    private String newVerName = "";
    /**
     * 更新回调
     */
    private UpdateCallback mCallback;
    /**
     * 最后更新进度，用来降频刷新
     */
    private int lastProgress;

    private Handler mHandler = null;
    private Timer timer = null;
    private String notifyTitle = "";
    private String notifyDescription = "";


    private SystemDownload(Context context) {
        this.mContext = context;
        HPatch.getInstance().initSo(context);
    }

    public static SystemDownload getInstance(Context context) {
        if (instance == null) {
            synchronized (SystemDownload.class) {
                if (instance == null) {
                    instance = new SystemDownload(context);
                }
            }
        }
        return instance;
    }

    //  <!-- 配置 点击通知 和 下载完成 两个 action -->
//  <action android:name="android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED"/>
//  <action android:name="android.intent.action.DOWNLOAD_COMPLETE"/>
    //广播接收者，接收下载状态
    private BroadcastReceiver receiver;

    /**
     * 这只是否自动安装
     */
    public SystemDownload setAutoInstall(boolean autoInstall) {
        this.autoInstall = autoInstall;
        return this;
    }

    /**
     * 通知栏 下载标题
     */
    public SystemDownload setNotifyTitle(String notifyTitle) {
        this.notifyTitle = notifyTitle;
        return this;
    }

    /**
     * 通知栏 下载描述
     */
    public SystemDownload setNotifyDescription(String description) {
        this.notifyDescription = description;
        return this;
    }

    public SystemDownload setUpdateCallback(@Nullable UpdateCallback callback) {
        this.mCallback = callback;
        return this;
    }

    public SystemDownload setShowLog(boolean showDebug) {
        LogUtils.setShowLog(showDebug);
        return this;
    }

    /**
     * 下载补丁
     */
    public void downloadPatch(String patchUrl, long newVersionCode, String newVersionName, String md5Patch, boolean showNotification, boolean needProgress) {
        newVerName = newVersionName;
        isPatch = true;
        download(patchUrl, newVersionCode, "app_" + newVersionName + "_apk.patch", md5Patch, showNotification, needProgress);
    }

    /**
     * 下载apk
     */
    public void downloadAPK(String apkUrl, long newVersionCode, String newVersionName, String md5Apk, boolean showNotification, boolean needProgress) {
        newVerName = newVersionName;
        isPatch = false;
        download(apkUrl, newVersionCode, "app_" + newVersionName + ".apk", md5Apk, showNotification, needProgress);
    }

    //使用系统下载器下载
    private void download(String versionUrl, long versionCode, String fileName, String md5, boolean showNotification, boolean needProgress) {
        boolean isDownloading = mTaskId != 0;
        if (mHandler != null) {
            Message msg = new Message();
            msg.what = 0;
            msg.obj = isDownloading;
            mHandler.sendMessage(msg);
        }
        if (isDownloading) {
            LogUtils.i("已经在下载中,请勿重复下载。");
            return;
        }
        String path = AppUtils.getUpdateCacheFilesDir(mContext);
        //删除旧的更新文件，和保存位置对应
        File oldFile = new File(path, fileName);
        boolean isExistApk = false;
        if (!TextUtils.isEmpty(md5)) {//验md5
            isExistApk = AppUtils.verifyFileMD5(oldFile, md5);
        } else if (versionCode > 0) {
            isExistApk = AppUtils.apkExists(mContext, versionCode, oldFile);
        }
        if (isExistApk) {//存在同版本的 直接安装
            lookAPK(0, oldFile.getAbsolutePath());
            return;
        }
        //删除旧的更新文件
        AppUtils.clearUpdateApkCache(mContext, oldFile.getAbsolutePath());

        lastProgress = 0;
        //将下载请求加入下载队列
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //注册广播接收者，监听下载状态
        registerReceiver(needProgress);

        try {
            //创建下载任务
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(versionUrl));
            //设置文件类型，可以在下载结束后自动打开该文件
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(versionUrl));
            request.setMimeType(mimeString);//加入任务队列
            /*
             * 设置在通知栏是否显示下载通知(下载进度), 有 3 个值可选:
             * VISIBILITY_VISIBLE:                   下载过程中可见, 下载完后自动消失 (默认)
             * VISIBILITY_VISIBLE_NOTIFY_COMPLETED:  下载过程中和下载完成后均可见
             * VISIBILITY_HIDDEN:                    始终不显示通知
             * VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION 只在下载完成后显示通知
             */
            if (showNotification) {
                // 设置通知栏的标题，如果不设置，默认使用文件名
                if (!TextUtils.isEmpty(notifyTitle)) {
                    request.setTitle(notifyTitle);
                }
                // 设置通知栏的描述
                if (!TextUtils.isEmpty(notifyDescription)) {
                    request.setDescription(notifyDescription);
                }
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            } else {
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            }

        /*if (mTaskId != 0) {// 取消上一次下载任务  mTaskId存起来+,断点续传，暂停恢复
            downloadManager.remove(mTaskId);
            // 记录已下载的字节数()
            long downloadedBytes = getDownloadedBytes(mTaskId);
            // 从本地获取已下载的字节数及其他信息
            // long downloadedBytes = getSavedDownloadedBytes();
            if (downloadedBytes > 0) {
                // 如果想要实现下载的暂停功能，可以使用DownloadManager提供的remove()方法取消下载任务。然后，
                // 可以记录已下载的字节数和文件URL等信息，以便稍后重新启动下载时可以通过设置HTTP字头的Range字段恢复下载进度。
                // 需要注意的是，为了支持断点续传，服务器必须能够处理并正确响应Range字段，返回恰当范围的数据。
                // 同时，如果服务器不支持断点续传或不满足范围请求，它可能会忽略Range字段并返回完整的文件。
                // 因此，断点续传的成功与否也取决于服务器的支持。
                // 根据已下载的字节数设置请求头Range字段，以实现断点续传功能
                request.addRequestHeader("Range", "bytes=" + downloadedBytes + "-");
            }
        }*/

            // 是否允许漫游时下载
            request.setAllowedOverRoaming(false);//漫游网络是否可以下载
            // 允许在流量下下载
            request.setAllowedOverMetered(true);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            // 是否允许该记录在下载管理界面可见，Q只有公共目录才可见
            request.setVisibleInDownloadsUi(false);
            // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
            //request.allowScanningByMediaScanner(); // Q私有目录不扫描
            // 1.sdcard的目录下的download文件夹，下载sd卡需要权限, Q+不需要，M~P需要？。。
            // request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            // AppUtils.clearUpdateApkCache(mContext, new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName));
            // 2.必须是外部存储路径的文件 URI，并且调用应用程序必须具有 WRITE_EXTERNAL_STORAGE 权限。
            // request.setDestinationUri(Uri);
            // 3.下载到 Android/data/packagename/files/apk/不需要权限
            request.setDestinationInExternalFilesDir(mContext, UpdateConstants.DEFAULT_DIR, fileName);
            File dirFile = new File(path);
            if (!dirFile.exists()) {//目录不存在，创建
                dirFile.mkdirs();
            }
            //加入下载列后会给该任务返回一个long型的id, 通过该id可以取消任务，重启任务等等
            mTaskId = downloadManager.enqueue(request);
            //开始下载
            if (mHandler != null) {
                Message msg1 = new Message();
                msg1.what = 1;
                msg1.obj = versionUrl;
                mHandler.sendMessage(msg1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {//子线程
                    checkDownloadStatus();
                }
            }, 1000, 1000);
        }
    }

    /**
     * 下载监听
     *
     * @param needProgress 需要进度
     */
    private void registerReceiver(boolean needProgress) {
        if (needProgress) {
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    switch (msg.what) {
                        case 0:
                            if (mCallback != null) {
                                boolean isDownloading = msg.obj != null && (boolean) msg.obj;
                                mCallback.onDownloading(isDownloading);
                            }
                            break;
                        case 1:
                            if (mCallback != null) {
                                String versionUrl = msg.obj == null ? "" : (String) msg.obj;
                                mCallback.onStart(versionUrl);
                            }
                            break;
                        case 2:
                            if (mCallback != null) {
                                Bundle bundle = msg.getData();
                                if (bundle != null) {
                                    int bytesDownloaded = bundle.getInt("progress");
                                    int bytesTotal = bundle.getInt("total");
                                    boolean isChanged = bundle.getBoolean("isChanged");
                                    if (bytesTotal > 0) {
                                        mCallback.onProgress(bytesDownloaded, bytesTotal, isChanged);
                                    }
                                }
                            }
                            break;
                        case 3:
                            if (mCallback != null) {
                                mCallback.onFinish((File) msg.obj);
                            }
                            break;
                        case 4:
                            if (mCallback != null) {
                                mCallback.onError((Exception) msg.obj);
                            }
                            break;
                        case 5:
                        default:
                            if (mCallback != null) {
                                mCallback.onCancel();
                            }
                            break;
                    }
                }
            };
            // 启动定时器，定时检查下载进度
            startTimer();
        } else { //只关心下载完成
            registerCompleteReceiver();
        }
    }

    /**
     * 下载完成监听
     */
    private void registerCompleteReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
//            if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
//              // 用户点击了通知栏，如果下载完了，点击安装
                //checkDownloadStatus();
//            } else
                if (TextUtils.equals(action, DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    //判断是否下载完成的广播
                    long completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (completedDownloadId == mTaskId) {// 更新通知栏中的相关信息，例如显示下载完成的提示
                        //下载完成
                        Uri downloadFileUri = downloadManager.getUriForDownloadedFile(completedDownloadId);
                        if (downloadFileUri != null) {
                            File f = new File(AppUtils.getRealFilePathFromUri(mContext, downloadFileUri));
                            //打开文件进行安装/补丁合并安装
                            lookAPK(mTaskId, f.getAbsolutePath()); //TODO 这两处Handler是空，所以用callback
                        } else {
                            handlerError("下载失败");
                        }
                    }
                }
            }
        };
        // 注册广播接收者
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);//通知点击
        //注册广播接收，监听下载状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {//需要导出权限，系统发送的广播
            mContext.registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED);
        } else {
            mContext.registerReceiver(receiver, intentFilter);
        }
    }

    //检查下载状态
    private void checkDownloadStatus() {
        if (mTaskId == 0) {
            return;
        }
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//筛选下载任务，传入任务ID,可变参数 多个
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            if (index < 0) {
                cursor.close();
                return;
            }
            int status = cursor.getInt(index);
            switch (status) {
                case DownloadManager.STATUS_PAUSED:
                    //下载暂停
                    LogUtils.d("下载暂停");
                    break;
                case DownloadManager.STATUS_PENDING:
                    //下载延迟
                    LogUtils.d("下载延迟");
                    break;
                case DownloadManager.STATUS_RUNNING:
                    //正在下载
                    LogUtils.d("正在下载");
                    int progressIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    int bytesDownloaded = cursor.getInt(progressIndex);
                    int totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
                    int bytesTotal = cursor.getInt(totalIndex);
                    boolean isChanged = false;
                    int progressPercentage = 0;
                    if (bytesTotal > 0) {
                        // 计算下载的百分比
                        progressPercentage = Math.round(bytesDownloaded * 1.0f / bytesTotal * 100.0f);
                        LogUtils.d("下载进度:" + progressPercentage);
                        // 百分比改变了才更新
                        if (progressPercentage != lastProgress) {
                            isChanged = true;
                            lastProgress = progressPercentage;

                            if (mHandler != null) {
                                Message msg2 = new Message();
                                msg2.what = 2;
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("isChanged", isChanged);
                                bundle.putInt("progress", bytesDownloaded);
                                bundle.putInt("total", bytesTotal);
                                msg2.setData(bundle);
                                mHandler.sendMessage(msg2);
                            }
                        }
                        LogUtils.i(String.format(Locale.getDefault(), "%d%%\t| %d/%d", progressPercentage, bytesDownloaded, bytesTotal));
                    } else {
                        LogUtils.e(String.format(Locale.getDefault(), "%d/%d", bytesDownloaded, bytesTotal));
                    }
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    if (mHandler != null) {
                        Message msg2 = new Message();
                        msg2.what = 2;
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("isChanged", true);
                        bundle.putInt("progress", 100);
                        bundle.putInt("total", 100);
                        msg2.setData(bundle);
                        mHandler.sendMessage(msg2);
                    }
                    LogUtils.i("检查下载完成");
                    //int id = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_ID));
                    // 获取下载好的 apk 路径
                    String localFilename = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                        localFilename = cursor.getString(columnIndex);
                    } else {
                        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                        localFilename = cursor.getString(columnIndex);
                    }
                    //打开文件进行安装/补丁合并安装
                    lookAPK(mTaskId, localFilename);
                    break;
                case DownloadManager.STATUS_FAILED:
                    //下载失败
                    LogUtils.e("检查下载失败");
                    handlerError("检查下载失败");
                    // 一次下载失败，取消下载广播
                    unregisterReceiver();
                    break;
            }
        }
        cursor.close();
    }

    //下载到本地后执行安装根据任务的id进行安装
    private void lookAPK(long taskId, String downloadFile) {
        if (isPatch) {//合并补丁
            String path = AppUtils.getUpdateCacheFilesDir(mContext);
            File dirFile = new File(path);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            // 合成apk文件
            File newApk = new File(path, "app_" + newVerName + ".apk");

            String downloadPath = Uri.parse(downloadFile).getPath();
            File file = new File(downloadPath);
            if (!file.exists()) {//如果不存在,再通过下载uri获取真实文件路径
                // 得到下载文件
                if (downloadManager != null && taskId != 0) {
                    Uri downloadFileUri = downloadManager.getUriForDownloadedFile(taskId);
                    downloadPath = AppUtils.getRealFilePathFromUri(mContext, downloadFileUri);
                } else {
                    handlerError("下载失败，补丁文件不存在");
                    return;
                }
            }
            HPatch.getInstance().patchApk(AppUtils.getApkPath(mContext),
                    downloadPath, newApk.getAbsolutePath(), new HPatch.PatchCallback() {
                        @Override
                        public void onPatchResult(boolean success) {
                            if (success) {//合并完成
                                LogUtils.d("补丁合并成功");
                                if (autoInstall) {
                                    installApk(newApk);
                                }
                                handlerFinish(newApk);
                            } else {
                                handlerError("补丁合并失败");
                            }
                        }
                    });
        } else {//apk
        /*if (Build.VERSION.SDK_INT >= 26) { //不需要 不需要 不需要
            boolean b = mContext.getPackageManager().canRequestPackageInstalls();
            if (!b) {
                Uri packageURI = Uri.parse("package:"+ mContext.getPackageName());
                //设置安装未知应用来源的权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageURI);
                mContext.startActivity(intent);
                return;
            }
        }*/

            File apkFile;
            if (downloadManager != null && taskId != 0) {
                // apk文件类型
                //String type = downloadManager.getMimeTypeForDownloadedFile(taskId);
                // 得到下载文件
                Uri downloadFileUri = downloadManager.getUriForDownloadedFile(taskId);
                apkFile = new File(AppUtils.getRealFilePathFromUri(mContext, downloadFileUri));
            } else {
                String downloadPath = Uri.parse(downloadFile).getPath();
                File file = new File(downloadPath);
                if (!file.exists()) {
                    handlerError("下载失败， 文件不存在");
                    return;
                }
                apkFile = file;
            }
            if (autoInstall) {
                installApk(apkFile);
            }
            handlerFinish(apkFile);
        }
        // 一次下载结束后，取消下载广播
        unregisterReceiver();
    }

    private void installApk(File fileApk) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        install.addCategory(Intent.CATEGORY_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true); //表明不是未知来源
        // 不能直接用dowanload下载的Uri, android M 不认
        install.setDataAndType(AppUtils.fromFile24(mContext, fileApk), "application/vnd.android.package-archive");
        try {
            mContext.startActivity(install);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("没有找到打开此类文件的程序");
        }
    }

    private void handlerFinish(File newApk) {
        if (mHandler != null) {
            Message msg3 = new Message();
            msg3.what = 3;
            msg3.obj = newApk;
            mHandler.sendMessage(msg3);
        } else {
            if (mCallback != null) {
                mCallback.onFinish(newApk);
            }
        }
    }

    private void handlerError(String err) {
        if (mHandler != null) {
            Message msg4 = new Message();
            msg4.what = 4;
            msg4.obj = new Exception(err);
            mHandler.sendMessage(msg4);
        } else {
            if (mCallback != null) {
                mCallback.onError(new Exception(err));
            }
        }
    }


    private void unregisterReceiver() {
        mTaskId = 0;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (receiver != null) {
            mContext.unregisterReceiver(receiver);
            receiver = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }

    public void downloadCancel() {
        if (mTaskId != 0) {
            downloadManager.remove(mTaskId);
        }
        unregisterReceiver();
        if (mHandler != null) {
            mHandler.sendEmptyMessage(5);
        }
    }

}
