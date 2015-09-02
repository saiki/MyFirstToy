package jp.saiki.myfirsttoy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;


public class MainApp extends Application {

    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    private static Stage primaryState = null;

    private static VideoCapture videoCapture;

    static Stage getPrimaryState() {
        return primaryState;
    }

    static VideoCapture getVideoCapture() {
        return videoCapture;
    }

    @Override
    public void start(Stage stage) throws Exception {

        MainApp.primaryState = stage;
        stage.setMaximized(true);
        stage.setFullScreen(true);
        stage.setResizable(true);

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Main.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("JavaFX and Maven");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if ( this.videoCapture == null ) {
            return;
        }
        if ( ! this.videoCapture.isOpened() ) {
            return;
        }
        this.videoCapture.release();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        videoCapture = new VideoCapture();
        videoCapture.open(0);
        launch(args);
    }

}
