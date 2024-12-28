-keep class com.mhy.appupdate.UpdateConfig { *; }
#-keep class com.mhy.appupdate.constant.Constants { *; }
-keep class com.mhy.appupdate.AppUpdater { *; }
-keep class com.mhy.appupdate.AppUpdater$Builder { *; }
# service
-keep class com.mhy.appupdate.service.DownloadService { *; }
-keep class com.mhy.appupdate.service.SystemDownload { *; }
# FileProvider
-keep class com.mhy.appupdate.provider.DownloadFileProvider { *; }
# 接口
#-keep interface com.mhy.appupdate.listener.* { *; }
#-keep interface com.mhy.appupdate.http.* { *; }
#-keep interface com.mhy.appupdate.notify.* { *; }
#-keep class com.mhy.appupdate.util.*  { *; }
# 保留native方法
-keepclasseswithmembernames class * {
    native <methods>;
}
# 这个文件是让外部引用执行的混淆规则
#-keep class com.mhy.appupdate.** { *; }
-keep class com.github.sisong.HPatch { *; }