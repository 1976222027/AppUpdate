package com.mhy.appupdate;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.PackageInfoCompat;

import com.google.gson.Gson;
import com.mhy.appupdate.constant.UpdateConstants;
import com.mhy.appupdate.http.OkHttpManager;
import com.mhy.appupdate.listener.UpdateCallback;
import com.mhy.appupdate.service.SystemDownload;
import com.mhy.appupdate.util.AppUtils;
import com.mhy.appupdate.util.LogUtils;
import com.mhy.appupdate.util.PermissionUtils;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import kotlin.jvm.functions.Function1;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * =====================================
 * 作    者：mahongyin
 * 项    目：HYNetSpeedUtils
 * 目    录：com.mhy.hynetspeedutils
 * 创建日期：2024/8/22 上午9:47
 * 描    述：
 * 使    用：
 * =====================================
 */
public class MainActivity extends AppCompatActivity {

    private Button downloadBtn1;
    private Button downloadBtn2;
    private Button downloadBtn3;
    private Button cancelBtn;
    private TextView textView, textJson, tvProgress;
    private ProgressBar progressBar;
    private AppUpdater mAppUpdater;
    private UpdateCallback updateCallback;
    private boolean autoInstall = true;
    private UpdateInfo updateInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        textJson = findViewById(R.id.textJson);
        tvProgress = findViewById(R.id.tv_progress);
        downloadBtn1 = findViewById(R.id.downloadBtn1);
        downloadBtn2 = findViewById(R.id.downloadBtn2);
        downloadBtn3 = findViewById(R.id.downloadBtn3);
        cancelBtn = findViewById(R.id.cancelBtn);
        progressBar = findViewById(R.id.progressBar);
        textJson.setMovementMethod(ScrollingMovementMethod.getInstance());
        LogUtils.setShowLog(true);


        PermissionUtils.verifyReadAndWritePermissions(this, UpdateConstants.RE_CODE_STORAGE_PERMISSION);
        if (!PermissionUtils.isNotificationEnabled(this)) {
            PermissionUtils.startNotificationSetting(this);
        }
        setListener();

    }

    private void setListener() {
        updateCallback = new UpdateCallback() {
            @Override
            public void onDownloading(boolean isDownloading) {
                if (isDownloading) {
                    LogUtils.i("已经在下载中,请勿重复下载。");
                    textView.setText("已经在下载中,请勿重复下载。");
                } else {
                    LogUtils.i("开始下载…");
                    textView.setText("开始下载…");
                }
            }

            @Override
            public void onStart(String url) {
                LogUtils.i("start: " + url);
            }

            @Override
            public void onProgress(long progress, long total, boolean isChanged) {
                if (isChanged) {
                    LogUtils.i(progress + "/" + total);
                    int progressInt = Math.round(progress * 1.0f / total * 100.0f);
                    progressBar.setProgress(progressInt);
                    tvProgress.setText(String.format("%d%%", progressInt));
                }
            }

            @Override
            public void onFinish(File file) {
                LogUtils.i("下载完成");
                textView.setText("下载完成");
                if (!autoInstall) {//没有自动安装,自己安装
                    AppUtils.installApk(MainActivity.this, file);
                }
            }

            @Override
            public void onError(Exception e) {
                LogUtils.i("下载失败");
                textView.setText("下载失败");
            }

            @Override
            public void onCancel() {
                LogUtils.i("取消下载");
                textView.setText("取消下载");
            }
        };

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAppUpdater != null) {
                    mAppUpdater.stop();
                }
                SystemDownload.getInstance(MainActivity.this).downloadCancel();
                LogUtils.i("取消下载");
            }
        });

        btnClick(downloadBtn1, 1);
        btnClick(downloadBtn2, 2);
        btnClick(downloadBtn3, 3);
        findViewById(R.id.downloadBtn4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.openMarket(MainActivity.this, getPackageName());
            }
        });

    }

    private void btnClick(Button btn, int type) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.this.updateInfo == null) {
                    requestUpdateApi("http://192.168.1.7/update", new Function1<String, Void>() {
                        @Override
                        public Void invoke(String data) {
                            LogUtils.i("请求结果==" + data);
                            textJson.setText(data);
                            Gson gson = new Gson();
                            UpdateInfo updateInfo = gson.fromJson(data, UpdateInfo.class);
                            MainActivity.this.updateInfo = updateInfo;

                            update(updateInfo, type);
                            return null;
                        }
                    });
                } else {
                    update(updateInfo, type);
                }
            }
        });
    }

    private void update(UpdateInfo updateInfo, int type) {
        if (updateInfo != null && updateInfo.getCode() == 0) {
            boolean enable = updateInfo.getData().isEnableUpdate();
            if (!enable) {
                LogUtils.i("功能禁用");
                return;
            }
            //最小可用版本
            long minVersion = updateInfo.getData().getMinVersion();
            PackageInfo packageInfo = null;
            try {
                packageInfo = AppUtils.getPackageInfo(MainActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long versionCode = 0;
            if (packageInfo != null) {//当前版本号
                versionCode = PackageInfoCompat.getLongVersionCode(packageInfo);
            }
            // 静默下载,autoInstall要false,并且下载完再弹出安装界面; 强制更新时不可关闭更新弹窗
            autoInstall = updateInfo.getData().isAutoUpdate();
            boolean force;
            // 自行处弹窗UI
            if (versionCode < minVersion) {
                //强制更新
                force = true;
            } else {
                //非强制更新
                force = false;
            }
            if (type == 1) {
                // 系统下载
                systemDownload(updateInfo, true, true);
            } else if (type == 2) {
                //自定义下载
                downloadApk(updateInfo);
            } else {
                //浏览器下载，只能是.apk文件，不能是补丁
                AppUtils.downloadByBrowser(MainActivity.this, updateInfo.getData().getApkUrl());
            }
        }
    }

    private void systemDownload(UpdateInfo updateInfo, boolean showNotification, boolean needProgress) {
        try {
            PackageInfo packageInfo = AppUtils.getPackageInfo(MainActivity.this);
            String url = "";
            String newVerName = updateInfo.getData().getNewVersionName();
            long newVerCode = updateInfo.getData().getNewVersionCode();

            Map<String, UpdateInfo.DataDTO.PatchInfoDTO> mapPatch = updateInfo.getData().getPatchInfo();
            if (mapPatch != null) {//有补丁先下载补丁
                UpdateInfo.DataDTO.PatchInfoDTO patch = mapPatch.get(packageInfo.versionName);
                if (patch != null && !TextUtils.isEmpty(patch.getPatchUrl())) {
                    String md5Patch = patch.getPatchHash();
                    url = patch.getPatchUrl();
                    SystemDownload.getInstance(MainActivity.this)
                            .setNotifyTitle(AppUtils.getAppName(MainActivity.this))
                            .setNotifyDescription(packageInfo.versionName)
                            .setShowLog(true)
                            .setAutoInstall(updateInfo.getData().isAutoUpdate())
                            .setUpdateCallback(updateCallback)
                            .downloadPatch(url, newVerCode, newVerName, md5Patch, showNotification, needProgress);
                    return;
                }
            }

            String md5Apk = updateInfo.getData().getApkHash();
            url = updateInfo.getData().getApkUrl();
            SystemDownload.getInstance(MainActivity.this)
                    .setNotifyTitle(AppUtils.getAppName(MainActivity.this))
                    .setNotifyDescription(AppUtils.getPackageInfo(MainActivity.this).versionName)
                    .setShowLog(true)
                    .setAutoInstall(updateInfo.getData().isAutoUpdate())
                    .setUpdateCallback(updateCallback)
                    .downloadAPK(url, newVerCode, newVerName, md5Apk, showNotification, needProgress);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadApk(UpdateInfo updateInfo) {
        try {
            UpdateInfo.DataDTO dataDTO = updateInfo.getData();
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            UpdateInfo.DataDTO.PatchInfoDTO patchInfo = dataDTO.getPatchInfo().get(packageInfo.versionName);
            String patchUrl = "";
            String patchMd5 = "";
            long patchSize = 0;
            if (patchInfo != null) {
                patchUrl = patchInfo.getPatchUrl();
                patchSize = patchInfo.getPatchSize();
                patchMd5 = patchInfo.getPatchHash();
            }

            mAppUpdater = new AppUpdater.Builder(MainActivity.this)
                    .setApkUrl(dataDTO.getApkUrl())
                    .setApkMD5(dataDTO.getApkHash())
                    .setApkSize(dataDTO.getApkSize())
                    .setPatchUrl(patchUrl)
                    .setPatchMD5(patchMd5)
                    .setPatchSize(patchSize)
                    .setVersionCode(dataDTO.getNewVersionCode())
                    .setVersionName(dataDTO.getNewVersionName())
                    .setSaveFilename("app_" + dataDTO.getNewVersionName() + ".apk")
                    .setAutoInstall(dataDTO.isAutoUpdate())
                    .build();
            mAppUpdater.setShowLog(true);
            mAppUpdater.setHttpManager(OkHttpManager.getInstance());//不自定义就用默认的
            mAppUpdater.setUpdateCallback(updateCallback);
            mAppUpdater.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestUpdateApi(String url, Function1<String, Void> function) {
        //1.构建OkHttpClient实例
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)//链接超时为2秒，单位为秒
                .writeTimeout(15, TimeUnit.SECONDS)//写入超时
                .readTimeout(15, TimeUnit.SECONDS)//读取超时
                .build();

        //2.通过Builder辅助类构建请求对象
        final Request request = new Request.Builder()
                .url(url)//URL地址
                .build();//构建


        FutureTask<String> futureTask = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                //3.通过mOkHttpClient调用请求得到Call
                final Call call = okHttpClient.newCall(request);
                //4.执行同步请求，获取响应体Response对象
                Response response = call.execute();
                if (response.body() != null) {
                    return response.body().string();
                }
                return "";
            }
        });
        Executors.newCachedThreadPool().execute(futureTask);
        try {
            String result = futureTask.get();
            if (!TextUtils.isEmpty(result)) {
                function.invoke(result);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == UpdateConstants.RE_CODE_STORAGE_PERMISSION) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UpdateConstants.RE_CODE_NOTIFY_PERMISSION) {

        }
    }
}
