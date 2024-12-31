# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.mhy.appupdate.UpdateConfig { *; }
-keep class com.mhy.appupdate.constant.Constants { *; }
-keep class com.mhy.appupdate.AppUpdater { *; }
-keep class com.mhy.appupdate.AppUpdater$Builder { *; }
# service
-keep class com.mhy.appupdate.service.DownloadService { *; }
-keep class com.mhy.appupdate.service.SystemDownload { *; }
# FileProvider
-keep class com.mhy.appupdate.provider.DownloadFileProvider { *; }
# 接口
-keep interface com.mhy.appupdate.listener.* { *; }
-keep interface com.mhy.appupdate.http.* { *; }
-keep interface com.mhy.appupdate.notify.* { *; }
#-keep class com.mhy.appupdate.util.*  { *; }
-keep class com.mhy.appupdate.util.LogUtils  { *; }
# 保留native方法
-keepclasseswithmembernames class * {
    native <methods>;
}
# 这个文件是自己打包执行的混淆规则