package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Controller {
    @FXML
    ImageView my_image;
    Runtime runtime;

    public Controller() {
        System.out.println("constructor");

//        List<String> logList = new ArrayList<>();
//        logList.add("Notify windowvisibilityinfo changed start");
//        logList.add("Active window not change, id: 38, 5101");

        runtime = Runtime.getRuntime();
    }

    /**
     * hdc shell uinput -T -d 67 258
     * hdc shell uinput -T -u 67 258
     * pause
     *
     * @param x
     * @param y
     */
    private void click(int x, int y) {
        System.out.printf("要通过hdc发送给设备的坐标：%d %d%n", x, y);
        try {
            runtime.exec("hdc shell uinput -T -c " + x + " " + y);
            System.out.println("command exec finish");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.printf("error: %s%n", e);
        }
    }

    public void initialize() {
        System.out.println("controller initialize");
//        testMouseEvent();
        my_image.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                System.out.println("setOnMouseReleased x = " + x + " y = " + y);
                switch (event.getButton()) {
                    case PRIMARY:
                        System.out.println("event.getButton() 左键");
                        click((int) (x * Main.scaleRatio), (int) (y * Main.scaleRatio));

                        delayRefreshUI();
                        break;
                    case SECONDARY:
                        System.out.println("event.getButton() 右键");

                        //返回按钮的坐标：208 1244
                        click(208, 1244);

                        delayRefreshUI();
                        break;
                    case MIDDLE:
                        System.out.println("event.getButton() 中键");

                        captureScreenAndRender();

                        loadLatestScreenImage();
                        break;
                }
            }
        });
    }

    public void onMouseScroll(ScrollEvent event) {
        double deltaY = event.getDeltaY();//-40
        double startX = event.getX();//233.0
        double startY = event.getY();//374.0
        double endX = startX;//233.0
        double endY = startY + deltaY;//334.0

        //hdc shell uinput -T -g 466 748 466 668
        try {
            Process process = runtime.exec(String.format("hdc shell uinput -T -g %d %d %d %d", (int) (startX * Main.scaleRatio), (int) (startY * Main.scaleRatio), (int) (startX * Main.scaleRatio), (int) ((startY + deltaY) * Main.scaleRatio)));
            System.out.println("滚动日志--------start------------");
            printShellLog(process.getInputStream());
            System.out.println("滚动日志--------finish------------");
//            process.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        captureScreenAndRender();
    }

    private List<String> printShellLog(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        List<String> log = new ArrayList<>();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
            log.add(line);
        }
        reader.close();
        return log;
    }

    public String captureScreen() throws IOException, InterruptedException {
//        String temp_device_file_path = "/data/local/tmp/temp" + System.currentTimeMillis() + ".jpeg";
        String temp_device_file_path = "/data/local/tmp/temp111.jpeg";

        runtime.exec("hdc shell rm /data/local/tmp/temp111.jpeg");

        Thread.sleep(500);

        Process process = runtime.exec("hdc shell snapshot_display -f " + temp_device_file_path);
        System.out.println("截屏日志--------start------------");
        List<String> list = printShellLog(process.getInputStream());
        System.out.println("截屏日志--------finish------------");
//        process.destroy();
        for (String log : list) {
            if (log.contains("error: snapshot display 0, write to ")) {
                return null;
            }
        }
        return temp_device_file_path;
    }

    public boolean sendScreen2PC(String screenshot_path) throws IOException {
        Process process1 = runtime.exec("hdc file recv " + screenshot_path + " " + getProjectRootDir() + "\\snapshot\\temp" + System.currentTimeMillis() + ".jpeg");
        System.out.println("图传日志------------start---------------");
        List<String> list = printShellLog(process1.getInputStream());
        System.out.println("图传日志------------finish---------------");
//        process1.destroy();

        boolean ret = true;
        for (String log : list) {
            if (log.contains("Error ")) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    /**
     * hdc shell snapshot_display -f /data/local/tmp/snapshot.jpeg
     * hdc file recv /data/local/tmp/snapshot.jpeg ./snapshot/snapshot.jpeg
     */
    public void captureScreenAndRender() {
        try {
            String screenshot_file_path = captureScreen();
            if (screenshot_file_path == null) {
                System.out.println("截屏失败");
                return;
            }
            Thread.sleep(100);
            boolean ret = sendScreen2PC(screenshot_file_path);
            if (!ret) {
                System.out.println("截屏文件发送到电脑失败");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private String getProjectRootDir() {
        File file = new File("");
        String absolutePath = file.getAbsolutePath();
        //absolutePath = C:\Users\admin\IdeaProjects\scrcpy_ohos
        System.out.println("absolutePath = " + absolutePath);//项目根目录
        return absolutePath;
    }

    public void loadLatestScreenImage() {
        File file = new File(getProjectRootDir() + "\\snapshot");
        File[] files = file.listFiles();
        if (files == null) {
            System.out.println("files is null");
            return;
        }
        if (files.length >= 2) {
            boolean delete = files[0].delete();
        }
        File target = files[files.length - 1];
        String path = target.getAbsolutePath();
        if (!path.endsWith(".jpeg")) {
            System.out.println("图片模式应为jpeg：" + path);
            return;
        }
        my_image.setImage(Main.createImage(path));

        System.out.println("更新界面为device最新截图");
    }

    void delayRefreshUI() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                captureScreenAndRender();

                loadLatestScreenImage();
            }
        }).start();
    }

//    void testMouseEvent() {
//        my_image.setOnMouseDragEntered(event -> System.out.println("setOnMouseDragEntered"));
//        my_image.setOnMouseDragExited(event -> System.out.println("setOnMouseDragExited"));
//        my_image.setOnMouseDragOver(event -> System.out.println("setOnMouseDragOver"));
//        my_image.setOnMouseDragReleased(event -> System.out.println("setOnMouseDragReleased"));
//
//        //点击状态拖动
//        my_image.setOnMouseDragged(event -> System.out.println("setOnMouseDragged"));
//
//        //光标进入
////        my_image.setOnMouseEntered(event -> System.out.println("setOnMouseEntered"));
//        //光标退出
////        my_image.setOnMouseExited(event -> System.out.println("setOnMouseExited"));
//
//        //鼠标非点击状态进区域内移动光标会不断回调
////        my_image.setOnMouseMoved(event -> System.out.println("setOnMouseMoved"));
//
//        //按下，放开，分别回调下面三个方法
//        my_image.setOnMousePressed(event -> System.out.println("setOnMousePressed"));
//        my_image.setOnMouseReleased(event -> System.out.println("setOnMouseReleased"));
//        my_image.setOnMouseClicked(event -> System.out.println("setOnMouseClicked"));
//    }
}
