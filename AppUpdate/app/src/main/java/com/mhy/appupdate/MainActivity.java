package com.mhy.appupdate;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.mhy.appupdate.listener.UpdateCallback;
import com.mhy.appupdate.util.AppUtils;
import com.mhy.appupdate.util.LogUtils;

import java.io.File;
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

    private Button downloadBtn;
    private Button cancelBtn;
    private TextView textView;
    private ProgressBar progressBar;
    private AppUpdater mAppUpdater;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        downloadBtn = findViewById(R.id.downloadBtn);
        cancelBtn = findViewById(R.id.cancelBtn);
        progressBar = findViewById(R.id.progressBar);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAppUpdater != null) {
                    mAppUpdater.stop();
                }
            }
        });

        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestUpdateApi("http://192.168.1.3/update", new Function1<String, Void>() {
                    @Override
                    public Void invoke(String data) {
                        LogUtils.i("请求结果==" + data);
                        textView.setText(data);
                        downloadApk(data);
                        return null;
                    }
                });
            }
        });

    }

    private void downloadApk(String data) {
        try {
            Gson gson = new Gson();
            UpdateInfo updateInfo = gson.fromJson(data, UpdateInfo.class);
            if (updateInfo.getCode() == 0) {
                UpdateInfo.DataDTO dataDTO = updateInfo.getData();
                UpdateInfo.DataDTO.PatchInfoDTO patchInfo = dataDTO.getPatchInfo().get(AppUtils.getPackageInfo(MainActivity.this).versionName);
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
                        .setSaveFilename("new.apk")
                        .build();

                mAppUpdater.setUpdateCallback(new UpdateCallback() {
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

                    }

                    @Override
                    public void onProgress(long progress, long total, boolean isChanged) {
                        if (isChanged) {
                            LogUtils.i(progress + "/" + total);
                            progressBar.setProgress((int) (progress * 1.0f / total * 100.0f));
                        }
                    }

                    @Override
                    public void onFinish(File file) {
                        LogUtils.i("下载完成");
                        textView.setText("下载完成");
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
                });
                mAppUpdater.start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
}
