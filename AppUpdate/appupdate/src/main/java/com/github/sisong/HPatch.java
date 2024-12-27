package com.github.sisong;

import android.content.Context;

import com.mhy.appupdate.util.LogUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * 子线程异步去执行
 */
public class HPatch {

    private static volatile HPatch instance;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private boolean init = false;

    public static HPatch getInstance() {
        if (instance == null) {
            synchronized (HPatch.class) {
                if (instance == null) {
                    instance = new HPatch();
                }
            }
        }
        return instance;
    }
    // auto load libhpatchz.so ?
    // static { System.loadLibrary("hpatchz"); }
    public void initSo(Context context) {
        if (!init) {
            init = true;
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//                ReLinker.loadLibrary(context, "hpatchz");
//            } else {
                System.loadLibrary("hpatchz");
//            }
        }
    }

    public interface PatchCallback {
        void onPatchResult(boolean success);
    }


    /**
     * 外部调用合并补丁方法
     *
     * @param oldFileName    旧文件
     * @param diffFileName   差分补丁
     * @param outNewFileName 新文件目标
     * @param callback       回调
     */
    public void patchApk(String oldFileName, String diffFileName, String outNewFileName, PatchCallback callback) {
        if (!init) throw new RuntimeException("please call initSo() first");
        LogUtils.e(oldFileName+","+diffFileName+","+outNewFileName);
        FutureTask<Integer> task = new FutureTask<Integer>(new Callable<Integer>() {
            @Override
            public Integer call() {
                return patch(oldFileName, diffFileName, outNewFileName);
            }

        });
        executor.execute(task);
        try {
            callback.onPatchResult(task.get() == 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //patch:
    //  return THPatchResult, 0 is ok
    //  cacheMemory:
    //    cacheMemory is used for file IO, different cacheMemory only affects patch speed;
    //    recommended 256*1024,1024*1024,... if cacheMemory<0 then default 256*1024; 
    //    NOTE: if diffFile created by $xdelta3(-S,-S lzma),$open-vcdiff, alloc memory+=sourceWindowSize+targetWindowSize;
    //        ( sourceWindowSize & targetWindowSize is set when diff, C API getVcDiffInfo() can got values from diffFile. )
    //    if diffFile created by $bsdiff4, and patch very slow,
    //      then cacheMemory recommended oldFileSize+256*1024;

    private static native int patch(String oldFileName, String diffFileName, String outNewFileName, long cacheMemory);

    /**
     * 合并补丁方法
     * @return 0 成功
     */
    private static int patch(String oldFileName, String diffFileName, String outNewFileName) {
        return patch(oldFileName, diffFileName, outNewFileName, -1);
    }
}
