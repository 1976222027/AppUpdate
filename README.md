#### 数据格式
配置java环境 
生成工具 运行gradle任务sources2Jar生成jar包 拷走out文件夹去使用

用于生成补丁的俩包要用一个渠道的包，对于美团多渠道，这样补丁不会影响渠道信息，原来是啥渠道，补丁合成新包后还是啥渠道，随旧包。

使用：
[github下载release](https://github.com/1976222027/AppUpdate/releases) 
[gitee下载release](https://gitee.com/mahongyin/AppUpdate/releases) 

配合[客户端github](https://github.com/1976222027/HYAppUpdate)使用
配合[客户端gitee](https://gitee.com/mahongyin/HYAppUpdate)使用
运行脚本

```shell
Windows双击: start.bat
Mac: ./MacStart.sh
```
或命令行 
```shell
java -jar appupdate-1.0-all.jar
不需要保留cmd窗口看日志的话运行下面这个
javaw -jar appupdate-1.0-all.jar
```
如果不需要生成差分包不传旧包即可。
完成过后，拷走out/dits文件夹放到服务端使用即可

### ---------------------------------------------------------------------------------------------------------------------------------------
```json
{
  "title": "版本更新",  
  "message": "央视体育客户端巴黎奥运会版本上线啦！1、奥运会全量直播  2、有趣的直播间玩法：弹幕、三分屏、小窗播放、多路直播，互动不停！3、礼物雨来袭 一起嗨 4、项目资讯，精准传递  5、丰富的赛事数据",	// 更新提示 用空格分割换行
  "apkSize": 1956631,	// 最新apk文件大小
  "apkUrl": "https://app/update.apk", // 最新apk 绝对url地址，也可用相对地址，如下方的"patchURL"字段
  "apkHash": "ea97c8efa490a2eaf7d10b37e63dab0e", // 最新apk文件的md5值
  "minVersion": 3900, // 低于此数值的app版本将强制更新 < 3940, 0不强制，最新版则全部强制
  "newVersionCode": 3940, // 当前最新版本代码
  "newVersionName": "3.9.4", // 当前最新版本代码
  "patchInfo": {  // 差分包信息,有那个版本，就是有补丁包 ,没有则全量更新
    "3.9.2": { // 表示393升393需要下载的补丁包
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

```cmd.bat运行脚本
//@echo off
//# 获取当前执行脚本的目录
//set jarPath=%~dp0
//# 使用 javaw 启动一个jar
//start javaw -jar %jarPath%appupdate-1.0-all.jar


差异库用的这个
https://github.com/sisong/HDiffPatch/releases
用命令行创建一个补丁:
$hdiffz -m-6 -SD -c-zstd-21-24 -d oldPath newPath outDiffFile
如果文件非常大，可以试试将 -m-6 改为 -s-64
打补丁:
$hpatchz oldPath diffFile outNewPath

