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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
    static int width;
    static int height;
    static double scaleRatio;
    static int scaleWidth = (int) (width * scaleRatio);
    static int scaleHeight = (int) (height * scaleRatio);

    @Override
    public void start(Stage primaryStage) throws IOException {
//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
//        stage.setTitle("Hello!");
//        stage.setScene(scene);
//        stage.show();

        Size size1 = getPcScreenInfo();
        int pcScreenWidth = size1.width;
        int pcScreenHeight = size1.height;
        System.out.println("电脑屏幕尺寸，宽 = " + pcScreenWidth + " 高 = " + pcScreenHeight);

        Size size = getDeviceScreenInfo();
        int phoneScreenWidth = size.width;
        int phoneScreenHeight = size.height;
        System.out.println("手机屏幕信息，宽 = " + phoneScreenWidth + " 高 = " + phoneScreenHeight);

        width = phoneScreenWidth;
        height = phoneScreenHeight;

        if (phoneScreenHeight >= pcScreenHeight) {
            scaleHeight = (int) (pcScreenHeight * 0.65f);
            scaleRatio = phoneScreenHeight * 1.0f / scaleHeight;
            scaleWidth = (int) (phoneScreenWidth / scaleRatio);

            System.out.println("手机高度大于电脑，手机屏幕缩放信息，宽 = " + scaleWidth + " 高 = " + scaleHeight + " 比例 = " + scaleRatio);
        } else {
            scaleWidth = phoneScreenWidth;
            scaleHeight = phoneScreenHeight;
            scaleRatio = 1;
        }

        Class<? extends HelloApplication> clz = getClass();
        FXMLLoader loader = new FXMLLoader(clz.getResource("hello-view.fxml"));
        Parent root = loader.load();

        HelloController controller = (HelloController) loader.getController();

        primaryStage.setTitle("Hello World");
        //初始化窗口位置
        primaryStage.setX(25);
        primaryStage.setY(25);

        primaryStage.setScene(new Scene(root, scaleWidth, scaleHeight + 50));

        primaryStage.show();

        controller.captureScreenAndRender();
        controller.loadLatestScreenImage();

        root.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {

            }
        });
        root.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                System.out.println("addEventFilter = " + event.toString());
                //鼠标滚轮滚动距离
                double deltaY = event.getDeltaY();
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

        try {
            Process process = Runtime.getRuntime().exec("hdc shell snapshot_display");
            InputStream inputStream = process.getInputStream();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
//                System.out.println("日志打印：" + line);
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
            reader.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//            }
//        }).start();
        return size;
    }

    public static Image createImage(String path) {
        return new Image("file:" + path, scaleWidth, scaleHeight, false, true, false);
    }

    public static void main(String[] args) {
        launch();
    }
}
