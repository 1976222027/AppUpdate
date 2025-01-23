# appupdate

#### 介绍
Android app应用升级补丁包制作，产出差分补丁包和升级清单文件
目录结构：
├───AppUpdate-----------Android项目
├───appupdate-aar-------Android aar产物
├───AppUpdate-GUI-------GUI工具，需要JAVA11+环境，生成升级补丁包，生成更新清单文件
├───update--------------WebServer 接口服务

#### 工具使用教程 [更多参考](https://gitee.com/mahongyin/appupdate/blob/master/AppUpdate-GUI/README.md)

1.  下载工具包[releases](https://gitee.com/mahongyin/appupdate/releases)
2.  使用脚本启动，MacStart.sh / WinStart.bat
3.  启动后，选择新版本APK，（如果需要制作补丁，需要旧版本挨个填入，点击获取包信息）不需要制作补丁包的 直接点击获取包信息
4.  上一步中如果apk是美团Walle多渠道或[ApkBatchPackage多渠道](https://github.com/chiclaim/ApkBatchPackage)是支持的。在下一步中会在对应channel文件夹里生成产物
5.  填好配置信息，点击创建升级清单。在out目录生成app名的文件夹进入找到新版本号的文件夹，将清单文件放到WebServer 接口服务的update目录下。


#### Android 使用教程 [更多参考](https://gitee.com/mahongyin/appupdate/blob/master/AppUpdate/README.md)

1.  Android 端 引入 implementation "com.gitee.mahongyin:appupdate-aar:TAG" [![](https://jitpack.io/v/com.gitee.mahongyin/appupdate-aar.svg)](https://jitpack.io/#com.gitee.mahongyin/appupdate-aar)
2.  [接入参考Demo](https://gitee.com/mahongyin/appupdate/tree/master/AppUpdate/app)

