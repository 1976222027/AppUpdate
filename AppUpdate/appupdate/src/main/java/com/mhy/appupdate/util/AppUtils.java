package com.mhy.appupdate.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.core.content.pm.PackageInfoCompat;

import com.mhy.appupdate.constant.UpdateConstants;
import com.mhy.appupdate.provider.DownloadFileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Locale;


public final class AppUtils {

    /**
     * 十六进制字符
     */
    private static char hexChars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private AppUtils() {
        throw new AssertionError();
    }

    /**
     * 通过url获取App的全名称
     *
     * @param context 上下文
     * @return 返回App的名称；（例如：AppName.apk）
     */
    public static String getAppFullName(Context context, String url, String defaultName) {
        if (!TextUtils.isEmpty(url) && url.endsWith(".apk")) {
            String apkName = url.substring(url.lastIndexOf("/") + 1);
            if (apkName.length() <= 64) {
                return apkName;
            }
        }

        String filename = getAppName(context);
        LogUtils.d("AppName: " + filename);
        if (TextUtils.isEmpty(filename)) {
            filename = defaultName;
        }
        if (filename.endsWith(".apk")) {
            return filename;
        }
        return String.format("%s.apk", filename);
    }

    /**
     * 获取包信息
     *
     * @param context 上下文
     * @return {@link PackageInfo}
     * @throws PackageManager.NameNotFoundException
     */
    public static PackageInfo getPackageInfo(Context context) throws PackageManager.NameNotFoundException {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
        return packageInfo;
    }

    /**
     * 通过APK路径获取包信息
     *
     * @param context         上下文
     * @param archiveFilePath apk文件路径
     * @return
     */
    public static PackageInfo getPackageInfo(Context context, String archiveFilePath) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(archiveFilePath, 0);
        return packageInfo;
    }

    /**
     * 获取App的名称
     *
     * @param context 上下文
     */
    public static String getAppName(Context context) {
        try {
            int labelRes = getPackageInfo(context).applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取App的图标
     *
     * @param context 上下文
     * @return
     */
    public static int getAppIcon(Context context) {
        try {
            return getPackageInfo(context).applicationInfo.icon;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void installApk(Context context, File file) {
        installApk(context, file, AppUtils.getFileProviderAuthority(context));
    }

    /**
     * 安装APK
     *
     * @param context   上下文
     * @param file      APK文件
     * @param authority 文件访问授权
     */
    public static void installApk(Context context, File file, String authority) {
        Intent intent = getInstallIntent(context, file, authority);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("没有找到打开此类文件的程序");
        }
    }

    /**
     * 获取安装Intent
     *
     * @param context   上下文
     * @param file      APK文件
     * @param authority 文件访问授权
     * @return
     */
    public static Intent getInstallIntent(Context context, File file, String authority) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        Uri uriData;
        String type = "application/vnd.android.package-archive";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            uriData = DownloadFileProvider.getUriForFile(context, authority, file);
        } else {
            uriData = Uri.fromFile(file);
        }
        intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true); //表明不是未知来源
        intent.setDataAndType(uriData, type);
        return intent;
    }

    /**
     * APK是否存在相同版本的APK
     *
     * @param context     上下文
     * @param versionCode 版本号
     * @param file        APK文件
     * @return
     * @throws Exception
     */
    public static boolean apkExists(Context context, long versionCode, File file) {
        if (file != null && file.exists()) {
            String packageName = context.getPackageName();
            PackageInfo packageInfo = AppUtils.getPackageInfo(context, file.getAbsolutePath());

            if (packageInfo != null) {
                // 比对versionCode
                long apkVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo);
                LogUtils.d(String.format(Locale.getDefault(), "ApkVersionCode: %d", apkVersionCode));
                if (versionCode == apkVersionCode) {
                    ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                    if (applicationInfo != null && packageName.equals(applicationInfo.packageName)) {//比对packageName
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断文件是否存在
     *
     * @param context 上下文
     * @param path    文件路径
     * @return
     */
    public static boolean isAndroidQFileExists(Context context, String path) {
        return isAndroidQFileExists(context, new File(path));
    }

    /**
     * 判断文件是否存在
     *
     * @param context 上下文
     * @param file    文件
     * @return
     */
    public static boolean isAndroidQFileExists(Context context, File file) {
        AssetFileDescriptor descriptor = null;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            Uri uri = Uri.fromFile(file);
            descriptor = contentResolver.openAssetFileDescriptor(uri, "r");
            if (descriptor == null) {
                return false;
            } else {
                close(descriptor);
            }
            return true;
        } catch (FileNotFoundException e) {
            LogUtils.w(e.toString());
        } finally {
            close(descriptor);
        }
        return false;
    }

    /**
     * 校验文件的MD5
     *
     * @param file 文件
     * @param md5  MD5
     * @return 如果文件的MD5与 传入的 MD5字符比对一致，则返回 true，反之返回 false
     */
    public static boolean verifyFileMD5(File file, String md5) {
        String fileMD5 = getFileMD5(file);
        LogUtils.d("FileMD5: " + fileMD5);
        if (!TextUtils.isEmpty(md5)) {
            return md5.equalsIgnoreCase(fileMD5);
        }

        return false;
    }

    /**
     * 获取文件的MD5
     *
     * @param file 文件
     * @return 返回文件的MD5
     */
    public static String getFileMD5(File file) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, length);
            }
            return byteArrayToHexString(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 字节转为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 返回十六进制字符串
     */
    public static String byteArrayToHexString(byte bytes[]) {
        String hexString = null;
        if (bytes != null) {
            int length = bytes.length;
            StringBuilder out = new StringBuilder(length * 2);
            for (int x = 0; x < length; x++) {
                int nybble = bytes[x] & 0xF0;
                nybble = nybble >>> 4;
                out.append(hexChars[nybble]);
                out.append(hexChars[bytes[x] & 0x0F]);
            }
            hexString = out.toString();
        }
        return hexString;
    }

    /**
     * 获取文件访问授权
     *
     * @param context 上下文
     * @return 返回文件访问授权
     */
    public static String getFileProviderAuthority(Context context) {
        return context.getPackageName() + UpdateConstants.DEFAULT_FILE_PROVIDER;
    }

    /**
     * 关闭
     *
     * @param descriptor {@link AssetFileDescriptor}
     */
    private static void close(AssetFileDescriptor descriptor) {
        if (descriptor != null) {
            try {
                descriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除文件或文件夹
     *
     * @param file
     */
    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        } else if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (f.isFile()) {
                    f.delete(); // 删除所有文件
                } else if (f.isDirectory()) {
                    deleteFile(f); // 递规的方式删除文件夹
                }
            }
            // 删除目录本身
            return file.delete();
        }
        return false;
    }

    /**
     * 获取更新缓存的文件夹
     */
    public static String getUpdateCacheFilesDir(Context context) {
        File files = context.getExternalFilesDir(UpdateConstants.DEFAULT_DIR);
        if (files != null) {
            return files.getAbsolutePath();
        }
        return new File(context.getFilesDir(), UpdateConstants.DEFAULT_DIR).getAbsolutePath();
    }

    /**
     * 清除 更新缓存的文件夹
     *
     * @param savePath 自定义保存路径 空则默认路径
     */
    public static void clearUpdateApkCache(Context context, String savePath) {
        if (!TextUtils.isEmpty(savePath)) {
            File file = new File(savePath);
            if (file.exists()) {
                deleteFile(file);
                return;
            }
        }
        File dir = new File(getUpdateCacheFilesDir(context));
        if (dir.exists()) {
            deleteFile(dir);

        }
    }

    /**
     * apk 安装路径
     */
    public static String getApkPath(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
//    return context.getPackageCodePath();
        return applicationInfo.sourceDir;
    }

    public static Uri fromFile24(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return DownloadFileProvider.getUriForFile(context, getFileProviderAuthority(context), file);
        } else {
            return Uri.fromFile(file);
        }
    }

    /**
     * 通过文件后缀名获取文件的MIME类型
     */
    public static String getMIMEType(File file) {
        String mime = "";
        String name = file.getName();
        String ext = name.substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
        return mime;
    }

    /**
     * 以文件的形式打开
     */
    private static void openFile(File file, Context context) {
        Intent var2 = new Intent();
        var2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        var2.setAction(Intent.ACTION_VIEW);
        String var3 = getMIMEType(file);
        var2.setDataAndType(Uri.fromFile(file), var3);
        try {
            context.startActivity(var2);
        } catch (Exception var5) {
            var5.printStackTrace();
            LogUtils.e("没有找到打开此类文件的程序");
        }
    }

    public static void openMarket(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
    /**
     * 通过浏览器下载
     */
    public static void downloadByBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        intent.setData(Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e("没有找到打开此类文件的程序");
        }
    }

    /**
     * 获取真实路径
     */
    public static String getRealFilePathFromUri(Context context, Uri uri) {
        String photoPath = "";
        if (context == null || uri == null) {
            return photoPath;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if (isExternalStorageDocument(uri)) {
                String[] split = docId.split(":");
                if (split.length >= 2) {
                    String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        photoPath = Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
            } else if (isDownloadsDocument(uri)) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                photoPath = getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                String[] split = docId.split(":");
                if (split.length >= 2) {
                    String type = split[0];
                    Uri contentUris = null;
                    if ("image".equals(type)) {
                        contentUris = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUris = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUris = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    String selection = MediaStore.Images.Media._ID + "=?";
                    String[] selectionArgs = new String[]{split[1]};
                    photoPath = getDataColumn(context, contentUris, selection, selectionArgs);
                }
            }
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(uri.getScheme())) {
            photoPath = uri.getPath();
        } else {
            photoPath = getDataColumn(context, uri, null, null);
        }

        return photoPath;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return "";
    }

}