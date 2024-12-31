package com.mhy.appupdate.service;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.github.sisong.HPatch;
import com.mhy.appupdate.constant.Constants;
import com.mhy.appupdate.provider.DownloadFileProvider;
import com.mhy.appupdate.util.AppUtils;
import com.mhy.appupdate.util.LogUtils;

import java.io.File;

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
    private long mTaskId;
    private static SystemDownload instance;
    private boolean isPatch = false;
    private String newVerName = "";

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

    //广播接收者，接收下载状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//             <!-- 配置 点击通知 和 下载完成 两个 action -->
//                <action android:name="android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED"/>
//                <action android:name="android.intent.action.DOWNLOAD_COMPLETE"/>
//            if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())) {
//                installAPK(mTaskId);
//            } else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            checkDownloadStatus();//检查下载状态
//            }
        }
    };

    public void downloadPatch(String patchUrl, String newVersionName) {
        newVerName = newVersionName;
        isPatch = true;
        download(patchUrl, "app_" + newVersionName + "_apk.patch");
    }

    public void downloadAPK(String patchUrl, String newVersionName) {
        newVerName = newVersionName;
        isPatch = false;
        download(patchUrl, "app_" + newVersionName + ".apk");
    }

    //使用系统下载器下载
    private void download(String versionUrl, String versionName) {
        //将下载请求加入下载队列
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(versionUrl));
        request.setAllowedOverRoaming(false);//漫游网络是否可以下载
        //设置文件类型，可以在下载结束后自动打开该文件
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(versionUrl));
        request.setMimeType(mimeString);//加入任务队列
        /*
         * 设置在通知栏是否显示下载通知(下载进度), 有 3 个值可选:
         *    VISIBILITY_VISIBLE:                   下载过程中可见, 下载完后自动消失 (默认)
         *    VISIBILITY_VISIBLE_NOTIFY_COMPLETED:  下载过程中和下载完成后均可见
         *    VISIBILITY_HIDDEN:                    始终不显示通知
         */
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        // 设置通知栏的标题，如果不设置，默认使用文件名
        request.setTitle("新版本");
        // 设置通知栏的描述
        request.setDescription("下载中...");

        // 是否允许漫游时下载
        request.setAllowedOverRoaming(true);
        // 允许在流量下下载
        request.setAllowedOverMetered(true);
        // 是否允许该记录在下载管理界面可见，Q只有公共目录才可见
        request.setVisibleInDownloadsUi(false);
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        //request.allowScanningByMediaScanner(); // Q私有目录不扫描
        // 1.sdcard的目录下的download文件夹，下载sd卡需要权限
        //request.setDestinationInExternalPublicDir("/sdcard/download", versionName);
        // 2.必须是外部存储路径的文件 URI，并且调用应用程序必须具有 WRITE_EXTERNAL_STORAGE 权限。
        // request.setDestinationUri(Uri);
        // 3.下载到 Android/data/packagename/files/apk/不需要权限
        request.setDestinationInExternalFilesDir(mContext, Constants.DEFAULT_DIR, versionName);
        //加入下载列后会给该任务返回一个long型的id,通过该id可以取消任务，重启任务等等
        mTaskId = downloadManager.enqueue(request);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);//通知点击
        //注册广播接收，监听下载状态
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mContext.registerReceiver(receiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            mContext.registerReceiver(receiver, intentFilter);
        }
    }

    public void downloadCancel() {
        if (mTaskId != 0) {
            downloadManager.remove(mTaskId);
        }
    }

    private void unregisterReceiver() {
        mContext.unregisterReceiver(receiver);
    }

    //检查下载状态
    private void checkDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(mTaskId);//筛选下载任务，传入任务ID,可变参数
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
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    LogUtils.i("下载完成");
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
                    //打开文件进行安装
                    installAPK(mTaskId, localFilename);
                    cursor.close();
                    break;
                case DownloadManager.STATUS_FAILED:
                    //下载失败
                    LogUtils.e("下载失败");
                    unregisterReceiver();
                    cursor.close();
                    break;
            }
        }

    }

    //下载到本地后执行安装根据任务的id进行安装
    protected void installAPK(long taskId, String downloadFile) {
        // 得到下载文件
        Uri downloadFileUri = downloadManager.getUriForDownloadedFile(taskId);
        if (isPatch) {
            String path = AppUtils.getApkCacheFilesDir(mContext);//Android/data/packagename/files/apk/
            File dirFile = new File(path);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            File newApk = new File(path, "app_" + newVerName + ".apk");

            String downloadPath = Uri.parse(downloadFile).getPath();
            File file = new File(downloadPath);
            if (!file.exists()) {
                downloadPath = AppUtils.getPhotoPathFromContentUri(mContext, downloadFileUri);
            }
            HPatch.getInstance().patchApk(mContext.getPackageCodePath(),
                    downloadPath, newApk.getAbsolutePath(), new HPatch.PatchCallback() {
                        @Override
                        public void onPatchResult(boolean success) {
                            if (success) {
                                Intent install = new Intent(Intent.ACTION_VIEW);
                                install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                install.addCategory(Intent.CATEGORY_DEFAULT);
                                Uri uriApk;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    uriApk = DownloadFileProvider.getUriForFile(mContext, mContext.getPackageName() + Constants.DEFAULT_FILE_PROVIDER, newApk);
                                } else {
                                    uriApk = Uri.fromFile(newApk);
                                }
                                install.setDataAndType(uriApk, "application/vnd.android.package-archive");
                                mContext.startActivity(install);
                            }
                        }
                    });
        } else {
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
            // apk文件类型
            String type = downloadManager.getMimeTypeForDownloadedFile(taskId);
            if (TextUtils.isEmpty(type)) {
                type = "application/vnd.android.package-archive";//"*/*";
            }
            Intent install = new Intent(Intent.ACTION_VIEW);

            install.setDataAndType(downloadFileUri, type);
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addCategory(Intent.CATEGORY_DEFAULT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            mContext.startActivity(install);
        }

        unregisterReceiver();
    }

    public void downloadByBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setData(Uri.parse(url));
        mContext.startActivity(intent);
    }

}
