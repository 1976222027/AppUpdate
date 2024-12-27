<?php
//127.0.0.1/update/index.php?version=0&appkey=com.app.app
header('Content-type:text/html;charset=utf-8');
$appVersionCode = $_GET['version'];
$appkey = $_GET['appkey'];
//把unicode转化成中文
function decodeUnicode($str)
{
	$func = function ($matches) {
		return mb_convert_encoding(pack("H*", $matches[1]), "UTF-8", "UCS-2BE");
	};
	return preg_replace_callback('/\\\\u([0-9a-f]{4})/i', $func, $str);
}
//统一数据返回。
function success($okdata)
{
	$data = array("code" => 0, 'message' => "请求成功", "data" => $okdata,);
	$json = json_encode($data);
	echo decodeUnicode($json);
}
function error($error)
{
	//$obj='';//array();
	$data = array('code' => 1, 'message' => "请求失败," . $error); //, 'data' => $obj,
	$json = json_encode($data);
	echo decodeUnicode($json);
}
function getMsg($data, $appVersionCode)
{
	$versionCode = $data["newVersionCode"];
	if ($appVersionCode != null && $appVersionCode >= $versionCode) {
		error("已经是最新版本");
	} else {
		$minVersion = $data["minVersion"];
		$autoUpdate = $data["autoUpdate"];
		$updateStatus = 0; //-1不升级,0普通升级，1须要强制升级. 2静默 3增量
		if ($autoUpdate) {
			$updateStatus = 2;
		}
		if ($appVersionCode != null && $appVersionCode < $minVersion) { //小于最小版本要强制更新
			$updateStatus = 1;
		}
		$enableUpdate = $data["enableUpdate"];
		if (!$enableUpdate) {
			$updateStatus = -1;
		}

		$versionName = $data["newVersionName"];
		$apkUrl = $data["apkUrl"];
		$apkMd5 = $data["apkHash"];
		$apkSize = $data["apkSize"];
		$mTitle = $data["title"];
		$contentMsg = $data["message"];

		// $msg = array(
		// 	"Title" => $mTitle,
		// 	"UpdateStatus" => $updateStatus, //0普通升级，1须要强制升级. 2静默 3增量
		// 	"VersionCode" => $versionCode,
		// 	"VersionName" => $versionName,
		// 	"ModifyContent" => $contentMsg,
		// 	"DownloadUrl" => $apkUrl,
		// 	"ApkSize" => $apkSize,
		// 	"ApkMd5" => $apkMd5
		// ); //md5值没有的话，就没法保证apk是否完整，每次都会从新下载。

		success($data);//$msg
	}
}

$file = './updateVersion.json';
if (file_exists($file)) {
	$jsonString = file_get_contents($file);
	$data = json_decode($jsonString, true); // 注意，第二个参数为true，将返回一个数组，而不是一个对象
} else {
	die("File does not exist.");
}
//fopen
//$file = fopen('./updateVersion.json', 'r');
//$jsonString = fread($file, filesize('./updateVersion.json'));
//fclose($file);
//$data = json_decode($jsonString, true);
getMsg($data, $appVersionCode);
