package com.mhy.appupdate;

import com.alibaba.fastjson2.JSONObject;
import com.mhy.utils.ApkUtil;
import com.mhy.utils.JsonUtil;
import com.mhy.utils.ProgressFrom;
import com.mhy.utils.ToastUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.dongliu.apk.parser.bean.ApkMeta;

public class HelloApplication extends Application {
    private static JSONObject config;

    @Override
    public void start(Stage stage) throws IOException {
//        File dits = new File("out/dits/apkInfo");
//        if (!dits.exists()) {
//            dits.mkdirs();
//        }
        String configJson = ApkUtil.readFile(new File("config.json"));
        config = JSONObject.parseObject(configJson);
        if (config == null) {
            config = new JSONObject();
        }
        dragFile(stage);
    }

    public static void main(String[] args) {
        launch();
    }

    private static String newVersionName = "";
    private static String newAppName = "";
    private static String upTitle = "";
    private static String upMessage = "";
    private static int upMinVersion = 0;
    private static String upApkUrl = "";
    private static String upPatchUrl = "";
    private static Boolean enableUpdate = null;
    /**
     * 静默更新，后台自动下载
     */
    private static Boolean autoUpdate = null;

    public static void dragFile(Stage primaryStage) {
        Label label = new Label("拖拽新版本apk到这里 或 ");
        TextField textFieldNew = new TextField();
        textFieldNew.setMinHeight(40);
        String newPath = config.getString("newApkPath");
        if (newPath != null && !newPath.isEmpty()) {
            textFieldNew.setText(newPath);
        }
        Button btOpen = new Button("选择新版apk文件");
        fileChoose(primaryStage, textFieldNew, btOpen);
        Label label2 = new Label("拖拽旧版本apk到这里 或 ");
        TextField textFieldOld = new TextField();
        textFieldOld.setMinHeight(40);
        String oldPath = config.getString("oldApkPath");
        if (oldPath != null && !oldPath.isEmpty()) {
            textFieldOld.setText(oldPath);
        }
        Button btOpen2 = new Button("选择旧版apk文件");
        fileChoose(primaryStage, textFieldOld, btOpen2);
        Label info = new Label();
        info.setTextFill(Color.MAGENTA);
        info.setBorder(new Border(new BorderStroke(Color.GREEN, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(2))));
        info.setMinHeight(30);
        info.setMaxWidth(560);
        info.setWrapText(true);
        // 创建一个垂直布局容器
        VBox vbox = new VBox();
        vbox.setSpacing(5);

        // 创建5个文本输入框
        TextField textField1 = new TextField();
        upTitle = config.getString("title");
        if (upTitle != null && !upTitle.isEmpty()) {
            textField1.setText(upTitle);
        } else {
            textField1.setText("版本升级");
        }
        TextArea textField2 = new TextArea();
        //大小
        //textField2.setFont(Font.font(12));
        //允许自动换行
        textField2.setWrapText(true);
        //初始化设置行数
        textField2.setPrefRowCount(3);
        //设置宽高
        textField2.setPrefWidth(560);
        textField2.setPrefHeight(60);
        upMessage = config.getString("message");
        if (upMessage != null && !upMessage.isEmpty()) {
            textField2.setText(upMessage);
        }
        upMinVersion = config.getIntValue("minVersion", 0);
        TextField textField3 = new TextField();//0不强制升级
        textField3.setText(String.valueOf(upMinVersion));
        TextArea textField4 = new TextArea();
        textField4.setPrefRowCount(2);
        upApkUrl = config.getString("apkUrl");
        textField4.setText(upApkUrl);
        upPatchUrl = config.getString("patchUrl");
        enableUpdate = config.getBoolean("enableUpdate");
        if (enableUpdate == null) {
            enableUpdate = true;
        }
        autoUpdate = config.getBoolean("autoUpdate");
        if (autoUpdate == null) {
            autoUpdate = false;
        }
        TextArea textField5 = new TextArea();
        textField5.setPrefRowCount(2);
        textField5.setText(upPatchUrl);
        HBox chbox = new HBox();
        CheckBox cbox = new CheckBox("启用升级功能");
        cbox.setPadding(new Insets(5));
        cbox.setSelected(enableUpdate);
        cbox.selectedProperty().addListener((obs, oldValue, newValue) -> {
            enableUpdate = newValue;
        });
        CheckBox cbox2 = new CheckBox("开启静默升级");// 先wifi下载，后提示安装
        cbox2.setPadding(new Insets(5));
        cbox2.setSelected(autoUpdate);
        cbox2.selectedProperty().addListener((obs, oldValue, newValue) -> {
            autoUpdate = newValue;
        });
        chbox.getChildren().addAll(cbox,cbox2);
        Button update = new Button("2.创建升级清单文件");
        update.setTextFill(Color.WHITE);
        update.setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(8), null)));
        update.setMinHeight(30);
        update.setMinWidth(100);
        Separator separator3 = new Separator();
        // 添加标签和文本框到VBox
        vbox.getChildren().addAll(
                separator3,
                new Label("标题"),
                textField1,
                new Label("更新内容，【空格表示换行】"),
                textField2,
                new Label("minVersion 本地版本低于该值的一律强制更新,例如填新包的[versionCode]则全部强制更新, 0不强制"),
                textField3,
                new Label("全量更新包地址"),
                textField4,
                new Label("补丁包地址[不包含文件名字],文件名导出是啥就是啥，约定好"),
                textField5,
                chbox,
                update
        );
        vbox.setPadding(new Insets(10, 0, 0, 0));
        //创建清单按钮
        update.setOnMouseClicked(event -> {
            if (newVersionName != null && !newVersionName.isEmpty()) {
                ToastUtil.toast("开始创建升级清单文件");
                upTitle = textField1.getText();
                upMessage = textField2.getText();
                upMinVersion = Integer.parseInt(Optional.ofNullable(textField3.getText()).orElse("0"));
                upApkUrl = textField4.getText();
                upPatchUrl = textField5.getText();
                config.put("title", upTitle);
                config.put("message", upMessage);
                config.put("minVersion", upMinVersion);
                config.put("apkUrl", upApkUrl);
                config.put("patchUrl", upPatchUrl);
                config.put("enableUpdate", enableUpdate);
                config.put("autoUpdate", autoUpdate);
                //取保存新版apk的信息
//                String newMeta = ApkUtil.readFile(new File("out/dits/apkInfo/" + newVersionName + "_apkInfo.json"));
                String newMeta = ApkUtil.readFile(new File("out/" + newAppName + "/apkInfo/" + newVersionName + "_apkInfo.json"));
                ApkMeta apkMeta = JSONObject.parseObject(newMeta, ApkMeta.class);
                UpdateInfo updateInfo = new UpdateInfo();
                updateInfo.setNewVersionCode(Math.toIntExact(Optional.ofNullable(apkMeta.getVersionCode()).orElse(0L)));//int
                updateInfo.setNewVersionName(apkMeta.getVersionName());
                updateInfo.setApkSize(Integer.parseInt(apkMeta.getTargetSdkVersion())); //size
                updateInfo.setApkHash(apkMeta.getInstallLocation());//md5
                updateInfo.setTitle(upTitle);
                updateInfo.setMessage(upMessage);
                updateInfo.setMinVersion(upMinVersion);//低于此版 强制更新
                updateInfo.setApkUrl(upApkUrl);
                updateInfo.setEnableUpdate(enableUpdate);
                updateInfo.setAutoUpdate(autoUpdate);
//                File[] listFiles = new File("out/dits/" + newVersionName).listFiles();
                File[] listFiles = new File("out/" + newAppName + "/" + newVersionName).listFiles();
                //目录下有差分补丁包吗
                if (listFiles != null && listFiles.length > 0) {
                    if (!upPatchUrl.endsWith("/")) {
                        upPatchUrl = upPatchUrl + "/";
                    }
                    for (File listFile : listFiles) {
                        String patchName = listFile.getName();
                        if (listFile.isFile() && patchName.endsWith(".patch")) {//升新版补丁文件
                            String oldVersion = patchName.split("_")[0];
                            System.out.println("file:" + patchName);
                            long size = ApkUtil.getFileSize(listFile);
                            UpdateInfo.PatchBean bean = new UpdateInfo.PatchBean();
                            bean.setPatchHash(ApkUtil.getFileMD5(listFile));
                            //保存旧版apk信息
//                            String oldJson = ApkUtil.readFile(new File("out/dits/apkInfo/" + oldVersion + "_apkInfo.json"));
                            String oldJson = ApkUtil.readFile(new File("out/" + newAppName + "/apkInfo/" + oldVersion + "_apkInfo.json"));
                            ApkMeta oldMeta = JSONObject.parseObject(oldJson, ApkMeta.class);
                            bean.setOldHash(oldMeta.getInstallLocation());//md5
                            bean.setApkHash(apkMeta.getInstallLocation());//md5
                            bean.setPatchSize(Math.toIntExact(size));
                            //相对路径 相对updateVersion.json文件的地址
                            bean.setPatchUrl(upPatchUrl + listFile.getName());
                            //文件夹下有文件
                            updateInfo.addPatch(oldVersion, bean);
                        }
                    }
                }
                //生成更新表单，给前端使用
//                JsonUtil.createJsonFile(updateInfo, "out/dits/" + newVersionName + "/updateVersion.json");
                JsonUtil.createJsonFile(updateInfo, "out/" + newAppName + "/" + newVersionName + "/updateVersion.json");
                //保存配置文件
                ApkUtil.writeFile(JSONObject.toJSONString(config), new File( "config.json"));
                ToastUtil.toast("创建完成");
            } else {
                ToastUtil.toast("请先完成->1.获取包信息");
            }
        });
        VBox vBox = new VBox(5);
        Button button = new Button();
        button.setMinHeight(30);
        button.setMinWidth(100);
        button.setTextFill(Color.WHITE);
        button.setBackground(new Background(new BackgroundFill(Color.GREEN, new CornerRadii(8), null)));
        button.setText("1.获取包信息(旧包空则不生成差分包),结果看下面提示");
        vBox.getChildren().addAll(button, info, vbox);
        vBox.setPadding(new Insets(10, 0, 0, 0));

        VBox dragTarget = new VBox();
        HBox hBox1 = new HBox();
        TextField labelNew = new TextField();
        labelNew.setEditable(false);
        labelNew.getStyleClass().add("copyablelabel");
        hBox1.getChildren().addAll(label, btOpen, labelNew);
        HBox hBox2 = new HBox();
        TextField labelOld = new TextField();
        labelOld.setEditable(false);
        labelOld.getStyleClass().add("copyablelabel");
        hBox2.getChildren().addAll(label2, btOpen2, labelOld);
        hBox2.setPadding(new Insets(16, 0, 0, 0));
        dragTarget.getChildren().addAll(hBox1, textFieldNew, hBox2, textFieldOld, vBox);
        textFieldNew.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (event.getGestureSource() != dragTarget && event.getDragboard().hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            }
        });
        // 拖拽监听
        textOnDragDropped(textFieldNew);
        //使用 DragEvent 事件处理器 可以在输入框的事件处理器中使用
        textFieldOld.setOnDragOver(event -> {
            if (event.getGestureSource() != dragTarget && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        // 拖拽监听
        textOnDragDropped(textFieldOld);

        StackPane root = new StackPane();
        //[获取包信息]生成差分包按钮，不制作差分包 不填旧包即可，但也要点一下生成差分包，用于产出新包md5
        button.setOnMouseClicked(event -> {
            if (textFieldNew.getText() != null && !textFieldNew.getText().isEmpty()) {
                config.put("newApkPath", textFieldNew.getText());
                ToastUtil.toast("获取ing,请稍等");
                info.setText("获取ing,请稍等");
                ProgressFrom progress = new ProgressFrom(primaryStage);
                progress.activateProgressBar();
                ApkMeta apkMetaNew = ApkUtil.getApkInfo(textFieldNew.getText());
                if (apkMetaNew != null) {//文件存在
                    //String newMd5 = ApkUtil.getFileMD5(textFieldNew.getText());
                    //拿到新版本号
                    newVersionName = apkMetaNew.getVersionName();
                    newAppName = apkMetaNew.getName();//app名
                    labelNew.setText(" VersionCode: " + apkMetaNew.getVersionCode());
                    //应用名作为目录
                    File dits = new File("out/" + newAppName + "/apkInfo");
                    if (!dits.exists()) {
                        dits.mkdirs();
                    }
                    String jsonNew = JSONObject.toJSONString(apkMetaNew);
                    //保存新版apk信息
//                    ApkUtil.writeFile(jsonNew, new File("out/dits/apkInfo/" + newVersionName + "_apkInfo.json"));
                    ApkUtil.writeFile(jsonNew, new File(dits, newVersionName + "_apkInfo.json"));
                    //创建新版本升级目录 输出差分包路径 out/dits/3.9.4/3.9.2_3.9.4_apk.patch
//                    File newVer = new File("out/dits/" + newVersionName);
                    File newVer = new File("out/" + newAppName + "/" + newVersionName);
                    if (!newVer.exists()) {
                        newVer.mkdirs();
                    }
                    if (textFieldOld.getText() != null && !textFieldOld.getText().isEmpty()) {//有旧包，制作差分文件
                        config.put("oldApkPath", textFieldOld.getText());
                        ApkMeta apkMetaOld = ApkUtil.getApkInfo(textFieldOld.getText());
                        if (apkMetaOld != null) {
                            String oldVersionName = apkMetaOld.getVersionName();
                            labelOld.setText(" VersionCode: " + apkMetaOld.getVersionCode());
                            //制作补丁时旧版md5已保存
                            //String oldApkMd5 = ApkUtil.getFileMD5(textFieldOld.getText());
                            String jsonOld = JSONObject.toJSONString(apkMetaOld);
                            //保存旧版apk信息
//                            ApkUtil.writeFile(jsonOld, new File("out/dits/apkInfo/" + oldVersionName + "_apkInfo.json"));
                            ApkUtil.writeFile(jsonOld, new File("out/" + newAppName + "/apkInfo/" + oldVersionName + "_apkInfo.json"));
                            //制作差分包命令
                            List<String> cmd = new ArrayList<>();
                            //创建一个补丁
                            cmd.add(getCmdByOSName());
                            cmd.add("-m-6");
                            cmd.add("-SD");
                            cmd.add("-c-zstd-21-24");
                            cmd.add("-d");
                            cmd.add(textFieldOld.getText());//旧版本
                            cmd.add(textFieldNew.getText());//新版本
//                            cmd.add("out/dits/" + newVersionName + "/" + oldVersionName + "_" + newVersionName + "_apk.patch");//差分包名称
                            cmd.add("out/" + newAppName + "/" + newVersionName + "/app_" + oldVersionName + "_" + newVersionName + "_apk.patch");//差分包名称
                            commandStart(cmd, info, progress);
                        }
                    } else {//只有新包
                        info.setText("获取包信息成功[out file ok!]");
                        progress.cancelProgressBar();
//                    new Alert(Alert.AlertType.ERROR, "FFmpeg.exe Not Found.").show();
//                    new Alert(Alert.AlertType.INFORMATION, "没有转码任务，请选择视频进行转码。").show();
                    }
                    // 更新apk路径配置文件，其他信息还是旧的怕弄混，只留最后一个保存配置文件的操作
                    //ApkUtil.writeFile(JSONObject.toJSONString(config), new File("config.json"));
                } else {
                    ToastUtil.toast("获取apk信息失败，请确认文件是否存在");
                    info.setText("获取apk信息失败，请确认文件是否存在");
                    progress.cancelProgressBar();
                }
            } else {
                ToastUtil.toast("包路径空，请正确选择apk文件");
                info.setText("包路径空，请正确选择apk文件");
            }
        });
        root.setPadding(new Insets(20, 20, 10, 20));

        root.getChildren().add(dragTarget);
        //滚动布局包裹
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true); // 不显示横向滚动条
        Scene scene = new Scene(scrollPane, 600, 700);
        String myValue = getVersionFromManifest();
        primaryStage.setTitle("制作apk差分包" + myValue);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    /**
     * 文件选择
     */
    private static void fileChoose(Stage primaryStage, TextField textField, Button btOpen) {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.dir")/*当前工作目录*/));   //设置初始路径，默认为我的电脑
        chooser.setTitle("打开文件");//设置窗口标题，默认为“打开”
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("apk", "*.apk"));
        //筛选文件扩展
        btOpen.setOnMouseClicked(e -> {
            try {
                textField.setText(chooser.showOpenDialog(primaryStage).getAbsolutePath());
                //chooser.showOpenDialog(stage)得到File对象
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * 拖拽监听
     */
    private static void textOnDragDropped(TextField textFieldOld) {
        textFieldOld.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                List<File> files = db.getFiles();
                for (File file : files) {
                    textFieldOld.setText(file.getAbsolutePath());
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public static String getVersionFromManifest() {
        String jarPath = HelloApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String MANIFEST_VERSION_KEY = "Implementation-Version";
        String MANIFEST_BUILD = "Built-Timestamp";
        try {
            JarFile jar = new JarFile(jarPath);
            Manifest manifest = jar.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            return "v_" + attributes.getValue(MANIFEST_VERSION_KEY) + "   build_" + attributes.getValue(MANIFEST_BUILD);
        } catch (Exception ignored) {
        }
        return "";
    }

    /**
     * 系统类型
     */
    private static String getCmdByOSName() {
        String osName = System.getProperty("os.name");
        System.out.println(osName);
        // 当前工作目录
        String path = System.getProperty("user.dir");
        if (osName.startsWith("Mac OS")) {
            // 苹果
            return path + "/diff/macos/hdiffz";
        } else if (osName.startsWith("Windows")) {
            // windows
            return path + "/diff/windows64/hdiffz.exe";
        } else {
            // unix or linux
            return path + "/diff/linux64/hdiffz";
        }
    }

    /**
     * 调用命令行执行
     *
     * @param command  命令行参数
     * @param progress
     */
    public static void commandStart(List<String> command, Label info, ProgressFrom progress) {
        command.forEach(v -> System.out.print(v + " "));
        System.out.println();
        System.out.println();
        ProcessBuilder builder = new ProcessBuilder();
        //正常信息和错误信息合并输出
        builder.redirectErrorStream(true);
        builder.command(command);
        //开始执行命令
        Process process = null;
        try {
            process = builder.start();
            //如果你想获取到执行完后的信息，那么下面的代码也是需要的
            String line = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            info.setText("请等待完成");
            while ((line = br.readLine()) != null) {
                //info.setText("");
                System.out.println(line);
                //info.setText("" + info.getText().trim() + line);
            }
            info.setText("获取包信息成功[out file ok!]");
        } catch (IOException e) {
            e.printStackTrace();
            info.setText("[出错]" + e.toString());
        } finally {
            progress.cancelProgressBar();
            ToastUtil.toast("完成");
        }
    }

}