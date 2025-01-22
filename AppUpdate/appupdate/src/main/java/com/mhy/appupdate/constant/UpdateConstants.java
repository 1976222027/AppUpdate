package com.mhy.appupdate.constant;


public final class UpdateConstants {

    public static final String KEY_UPDATE_CONFIG = "app_update_config";

    public static final int DEFAULT_NOTIFICATION_ID = 0x66;

    public static final String DEFAULT_NOTIFICATION_CHANNEL_ID = "0x66";

    public static final String DEFAULT_NOTIFICATION_CHANNEL_NAME = "AppUpdater";

    public static final String KEY_STOP_DOWNLOAD_SERVICE = "stop_download_service";

    public static final String KEY_RE_DOWNLOAD = "app_update_re_download";

    public static final int RE_CODE_STORAGE_PERMISSION = 0x66;
    public static final int RE_CODE_NOTIFY_PERMISSION = 0x55;

    public static final int NONE = -1;

    public static final String DEFAULT_FILE_PROVIDER = ".AppUpdaterFileProvider";

    public static final String DEFAULT_DIR = "apk";
    // 进度更新频率
    public static final long MINIMUM_INTERVAL_MILLIS = 200L;

    public static final String PATCH_SUFFIX = ".patch";
}