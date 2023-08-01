package sample;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

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
public class Main extends Application {
    static int width = 720;
    static int height = 1280;
    static float scaleRatio = 0.5f;
    static int scaleWidth = (int) (width * scaleRatio);
    static int scaleHeight = (int) (height * scaleRatio);

    @Override
    public void start(Stage primaryStage) throws Exception {
        Class<? extends Main> clz = getClass();
        FXMLLoader loader = new FXMLLoader(clz.getResource("sample.fxml"));
        Parent root = loader.load();

        Controller controller = (Controller) loader.getController();

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, scaleWidth, scaleHeight));

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

    public static Image createImage(String path) {
        return new Image("file:" + path, scaleWidth, scaleHeight, false, true, false);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
