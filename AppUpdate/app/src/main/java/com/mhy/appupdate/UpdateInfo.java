package com.mhy.appupdate;


import java.io.Serializable;
import java.util.Map;

/**
 * Created By Mahongyin
 * Date    2024/12/26 22:10
 * {
 * 	"apkHash": "ea95c27bbf8d469f82ace41fc88e3b9d",
 * 	"apkSize": 31945087,
 * 	"apkUrl": "http://192.168.1.13/update/app.apk",
 * 	"autoUpdate": false,
 * 	"enableUpdate": true,
 * 	"message": "央视体育客户端巴黎奥运会版本上线啦！1、奥运会全量直播 2、有趣的直播间玩法：弹幕、三分屏、小窗播放、多路直播，互动不停！3、礼物雨来袭 一起嗨 4、项目资讯，精准传递 5、丰富的赛事数据",
 * 	"minVersion": 72300,
 * 	"newVersionCode": 72300,
 * 	"newVersionName": "7.23.0",
 * 	"patchInfo": {
 * 		"7.0.0": {
 * 			"apkHash": "ea95c27bbf8d469f82ace41fc88e3b9d",
 * 			"oldHash": "7691a849e00c5cbb2ae0ff7c95d8119a",
 * 			"patchHash": "973de2ca5160ec065df833ffc56d785c",
 * 			"patchSize": 30198505,
 * 			"patchUrl": "http://192.168.1.13/update/app_apk.patch"
 * 		        }    * 	},
 * 	"title": "版本升级"
 * }
 */
public class UpdateInfo implements Serializable {
    private int code;
    private String message;
    private DataDTO data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataDTO getData() {
        return data;
    }

    public void setData(DataDTO data) {
        this.data = data;
    }

    public class DataDTO implements Serializable {
        private String apkHash;
        private int apkSize;
        private String apkUrl;
        /**
         * 自动安装
         */
        private boolean autoUpdate;
        private boolean enableUpdate;
        private String message;
        private int minVersion;
        private int newVersionCode;
        private String newVersionName;
        private Map<String,PatchInfoDTO> patchInfo;
        private String title;

        public String getApkHash() {
            return apkHash;
        }

        public void setApkHash(String apkHash) {
            this.apkHash = apkHash;
        }

        public int getApkSize() {
            return apkSize;
        }

        public void setApkSize(int apkSize) {
            this.apkSize = apkSize;
        }

        public String getApkUrl() {
            return apkUrl;
        }

        public void setApkUrl(String apkUrl) {
            this.apkUrl = apkUrl;
        }

        public boolean isAutoUpdate() {
            return autoUpdate;
        }

        public void setAutoUpdate(boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
        }

        public boolean isEnableUpdate() {
            return enableUpdate;
        }

        public void setEnableUpdate(boolean enableUpdate) {
            this.enableUpdate = enableUpdate;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getMinVersion() {
            return minVersion;
        }

        public void setMinVersion(int minVersion) {
            this.minVersion = minVersion;
        }

        public int getNewVersionCode() {
            return newVersionCode;
        }

        public void setNewVersionCode(int newVersionCode) {
            this.newVersionCode = newVersionCode;
        }

        public String getNewVersionName() {
            return newVersionName;
        }

        public void setNewVersionName(String newVersionName) {
            this.newVersionName = newVersionName;
        }

        public Map<String,PatchInfoDTO> getPatchInfo() {
            return patchInfo;
        }

        public void setPatchInfo(Map<String,PatchInfoDTO> patchInfo) {
            this.patchInfo = patchInfo;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public class PatchInfoDTO implements Serializable {
            private String apkHash;
            private String oldHash;
            private String patchHash;
            private int patchSize;
            private String patchUrl;

            public String getApkHash() {
                return apkHash;
            }

            public void setApkHash(String apkHash) {
                this.apkHash = apkHash;
            }

            public String getOldHash() {
                return oldHash;
            }

            public void setOldHash(String oldHash) {
                this.oldHash = oldHash;
            }

            public String getPatchHash() {
                return patchHash;
            }

            public void setPatchHash(String patchHash) {
                this.patchHash = patchHash;
            }

            public int getPatchSize() {
                return patchSize;
            }

            public void setPatchSize(int patchSize) {
                this.patchSize = patchSize;
            }

            public String getPatchUrl() {
                return patchUrl;
            }

            public void setPatchUrl(String patchUrl) {
                this.patchUrl = patchUrl;
            }
        }
    }
}
