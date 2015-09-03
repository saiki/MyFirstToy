package jp.saiki.myfirsttoy;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;

import javax.security.auth.Destroyable;


public class MainApp extends Application {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    private static Stage primaryState = null;

    static Stage getPrimaryState() {
        return primaryState;
    }

    private Controller currentController;

    @Override
    public void start(Stage stage) throws Exception {

        MainApp.primaryState = stage;
        stage.setMaximized(true);
        stage.setFullScreen(true);
        stage.setResizable(true);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
        Parent root = loader.load();
        this.currentController = loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        this.currentController.close();
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
        launch(args);
    }

}
