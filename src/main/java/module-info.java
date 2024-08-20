module com.mhy.appupdate {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.dongliu.apkparser;
    requires com.alibaba.fastjson2;
//    requires org.apache.commons.codec;

    opens com.mhy.appupdate to javafx.fxml;
    exports com.mhy.appupdate;
}