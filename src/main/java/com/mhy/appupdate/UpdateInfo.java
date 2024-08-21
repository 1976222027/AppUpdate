package com.mhy.appupdate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class UpdateInfo {

    /**
     * minVersion : 3900
     * newVersionCode : 3940
     * newVersionName : 3.9.4
     * title : 版本更新
     * message : 央视体育客户端巴黎奥运会版本上线啦！
     * size : 1956631
     * apkUrl : apk
     * apkHash : ea97c8efa490a2eaf7d10b37e63dab0e
     * patchInfo : {"3.9.2":{"patchUrl":"dits/3.9.2_3.9.4_apk.patch","apkHash":"ea97c8efa490a2eaf7d10b37e63dab0e","oldHash":"ea97c8efa490a2eaf7d10b37e63dab0e","patchHash":"ea97c8efa490a2eaf7d10b37e63dab0e","size":1114810},"3.9.1":{"patchUrl":"dits/3.9.1_3.9.3_apk.patch","apkHash":"ea97c8efa490a2eaf7d10b37e63dab0e","oldHash":"ea97c8efa490a2eaf7d10b37e63dab0e","patchHash":"ea97c8efa490a2eaf7d10b37e63dab0e","size":1114810}}
     */

    private int minVersion;
    private int newVersionCode;
    private String newVersionName;
    private String title;
    private String message;
    private int apkSize;
    private String apkUrl;
    private String apkHash;

    private Map<String, PatchBean> patchInfo;// = new HashMap<>();//不给默认值的话 fastjson转json后是null

    public UpdateInfo(){
        patchInfo = new HashMap<>();//或者在构造给之
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getApkHash() {
        return apkHash;
    }

    public void setApkHash(String apkHash) {
        this.apkHash = apkHash;
    }

    public Map<String, PatchBean> getPatchInfo() {
        return patchInfo;
    }

    public void setPatchInfo(Map<String, PatchBean> patchInfo) {
        this.patchInfo = patchInfo;
    }

    public void addPatch(String versionName, PatchBean patch) {
        if (this.patchInfo == null) {
            this.patchInfo = new HashMap<>();
        }
        this.patchInfo.put(versionName, patch);
    }

    public PatchBean getPatch(String versionName) {
        if (this.patchInfo == null) {
            return null;
        }
        return this.patchInfo.get(versionName);
    }

    public static class PatchBean implements Serializable {

        /**
         * patchUrl : dits/3.9.2_3.9.4_apk.patch
         * apkHash : ea97c8efa490a2eaf7d10b37e63dab0e
         * oldHash : ea97c8efa490a2eaf7d10b37e63dab0e
         * patchHash : ea97c8efa490a2eaf7d10b37e63dab0e
         * size : 1114810
         */

        private String patchUrl;
        private String apkHash;
        private String oldHash;
        private String patchHash;
        private int patchSize;

        public String getPatchUrl() {
            return patchUrl;
        }

        public void setPatchUrl(String patchUrl) {
            this.patchUrl = patchUrl;
        }

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

    }
}
