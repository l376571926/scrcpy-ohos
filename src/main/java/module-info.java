module com.huawei.scrcpy_ohos {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.huawei.scrcpy_ohos to javafx.fxml;
    exports com.huawei.scrcpy_ohos;
}
