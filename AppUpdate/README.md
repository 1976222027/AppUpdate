下载，合并补丁，安装
[![](https://jitpack.io/v/com.gitee.mahongyin/appupdate.svg)](https://jitpack.io/#com.gitee.mahongyin/appupdate)
[![](https://jitpack.io/v/com.gitee.mahongyin/appupdate-aar.svg)](https://jitpack.io/#com.gitee.mahongyin/appupdate-aar)
```groovy
implementation "com.gitee.mahongyin:appupdate-aar:1.5.0"

```
android sdk 9 armv5    
android sdk 16 armv7    
android sdk 21 arm64    
```text
    
更新策略
enableUpdate = true; 优先级最高，如果设置为false，整个更新功能都不生效
autoUpdate = true; 下载后自动弹出安装
minVersion = 0; 本地版本低于该值的要强制更新，为0则不强制更新
patchInfo = {versionName:{},...}; Map内包含多个版本信息，versionName为本地包的versionName，只要不为空那就是有针对本地版本的更新补丁。
也就是有补丁先下载补丁，没有则全量下载。补丁优先。
```
```json
{
  "title": "版本更新",  
  "message": "央视体育客户端巴黎奥运会版本上线啦！1、奥运会全量直播  2、有趣的直播间玩法：弹幕、三分屏、小窗播放、多路直播，互动不停！3、礼物雨来袭 一起嗨 4、项目资讯，精准传递  5、丰富的赛事数据",	// 更新提示 用空格分割换行
  "apkSize": 1956631,	// 最新apk文件大小
  "apkUrl": "https://app/update.apk", // 最新apk 绝对url地址，也可用相对地址，如下方的"patchURL"字段
  "apkHash": "ea97c8efa490a2eaf7d10b37e63dab0e", // 最新apk文件的md5值
  "minVersion": 3900, //最小支持版本 低于此数值的本地版本将强制更新, 0不强制，最新版versioncode则全部强制
  "enableUpdate": true, //更新功能总开关，优先级最高, 可以随时设置更能功能是否可用
  "autoUpdate": false, //下载后自动安装
  "newVersionCode": 3940, // 当前最新版本代码
  "newVersionName": "3.9.4", // 当前最新版本代码
  "patchInfo": {  // 差分包信息,有那个版本，就是有补丁包 ,没有则全量更新
    "3.9.2": { // 表示392升394需要下载的补丁包
      "patchUrl": "dits/3.9.2_3.9.4_apk.patch", //差分包地址，相对此updateVersion.json文件的地址,也可用绝对地址
      "apkHash": "ea97c8efa490a2eaf7d10b37e63dab0e", // 合成后apk(即版本代码101)的文件md5值
      "oldHash": "ea97c8efa490a2eaf7d10b37e63dab0e", // 旧文件md5值
      "patchHash": "ea97c8efa490a2eaf7d10b37e63dab0e", // 补丁包md5
      "patchSize": 1114810 // 差分包大小
    },
    "3.9.1": { //  表示391升393需要下载的补丁包
      "patchUrl": "dits/3.9.1_3.9.4_apk.patch", //差分包地址，相对此updateVersion.json文件的地址,也可用绝对地址
      "apkHash": "ea97c8efa490a2eaf7d10b37e63dab0e", // 合成后apk(即版本代码101)的文件md5值
      "oldHash": "ea97c8efa490a2eaf7d10b37e63dab0e", // 旧文件md5值
      "patchHash": "ea97c8efa490a2eaf7d10b37e63dab0e", // 补丁包md5
      "patchSize": 1114810 // 差分包大小
    }
  }
}
```
