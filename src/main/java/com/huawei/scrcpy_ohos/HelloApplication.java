package com.huawei.scrcpy_ohos;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * C:\Users\admin>hdc shell
 * # snapshot_display
 * process: set filename to /data/snapshot_2023-07-06_13-56-30.jpeg
 * process: display 0: width 720, height 1280
 * snapshot: pixel format is: 3
 * snapshot: convert rgba8888 to rgb888 successfully.
 * <p>
 * success: snapshot display 0 , write to /data/snapshot_2023-07-06_13-56-30.jpeg as jpeg, width 720, height 1280
 */
public class HelloApplication extends Application {
    static int width = 720;
    static int height = 1280;
    static double scaleRatio = 1;
    static int scaleWidth = (int) (width * scaleRatio);
    static int scaleHeight = (int) (height * scaleRatio);

    @Override
    public void start(Stage primaryStage) throws IOException {
        Size size1 = getPcScreenInfo();
        int pcScreenWidth = size1.width;
        int pcScreenHeight = size1.height;
        System.out.println("电脑屏幕尺寸，宽 = " + pcScreenWidth + " 高 = " + pcScreenHeight);

        Size size = getDeviceScreenInfo();
        if (size != null) {
            int phoneScreenWidth = size.width;
            int phoneScreenHeight = size.height;
            if (phoneScreenWidth != 0 || phoneScreenHeight != 0) {
                width = phoneScreenWidth;
                height = phoneScreenHeight;
            }
            System.out.println("手机屏幕信息，宽 = " + phoneScreenWidth + " 高 = " + phoneScreenHeight);
        }
        if (height >= pcScreenHeight) {
            scaleHeight = (int) (pcScreenHeight * 0.65f);
            scaleRatio = height * 1.0f / scaleHeight;
            scaleWidth = (int) (width / scaleRatio);

            System.out.println("手机高度大于电脑，手机屏幕缩放信息，宽 = " + scaleWidth + " 高 = " + scaleHeight + " 比例 = " + scaleRatio);
        } else {
            scaleWidth = width;
            scaleHeight = height;
            scaleRatio = 1;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
        Parent root = loader.load();
        HelloController controller = (HelloController) loader.getController();

        //设置标题栏标题
        primaryStage.setTitle("scrcpy-ohos");
        //初始化窗口位置
        primaryStage.setX(25);
        primaryStage.setY(25);

        Scene scene = new Scene(root, scaleWidth, scaleHeight + 100);
        primaryStage.setScene(scene);

        Image place_holder = new Image(getClass().getResource("screenshot.jpeg").toString(), scaleWidth, scaleHeight, false, true, false);
        controller.my_image.setImage(place_holder);

        primaryStage.show();

        //检测OpenHarmony设备是否已连接
        Map<String, Object> exec1 = RuntimeHelper.getInstance().exec("hdc list targets");
        List<String> successMessage = RuntimeHelper.getInstance().getData(exec1);
        if ("[Empty]".equals(successMessage.get(0))) {
            System.out.println("OpenHarmony设备未连接");
            return;
        }

        //切换为性能模式，不让设备熄屏
        Map<String, Object> exec = RuntimeHelper.getInstance().exec("hdc shell power-shell setmode 602");
        if (!RuntimeHelper.getInstance().isSuccess(exec)) {
            RuntimeHelper.getInstance().printErrorMessage(exec);
            return;
        }

        controller.captureScreenAndRender();
        controller.loadLatestScreenImage();

        root.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                System.out.println("addEventFilter = " + event.toString());
                //鼠标滚轮滚动距离
                controller.onMouseScroll(event);
            }
        });
    }

    private Size getPcScreenInfo() {
        Size size = new Size();
        Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
//        System.out.println("电脑屏幕尺寸，w = " + rectangle2D.getWidth() + " h = " + rectangle2D.getHeight());
        size.width = (int) rectangle2D.getWidth();
        size.height = (int) rectangle2D.getHeight();
        return size;
    }

    private Size getDeviceScreenInfo() {
        Size size = new Size();

        //[Fail]ExecuteCommand need connect-key?
        Map<String, Object> exec = RuntimeHelper.getInstance().exec("hdc shell snapshot_display");
        List<String> stringList = RuntimeHelper.getInstance().getData(exec);
        if ("[Fail]ExecuteCommand need connect-key?".equals(stringList.get(0))) {
            return null;
        }
        for (String line : stringList) {
            if (line.contains("process: display 0: width ")) {
                int index1 = line.indexOf("width") + 6;
                int index2 = line.indexOf(",");
                int index3 = line.indexOf("height") + 7;

                String width = line.substring(index1, index2);
                String height = line.substring(index3);
//                    System.out.println("设备屏幕尺寸，宽 = <" + width + "> 高 = <" + height + ">");
                size.width = Integer.parseInt(width);
                size.height = Integer.parseInt(height);
            }
        }
        return size;
    }

    public static Image createImage(String path) {
        return new Image("file:" + path, scaleWidth, scaleHeight, false, true, false);
    }

    public static void main(String[] args) {
        launch();
    }
}
