package jp.saiki.myfirsttoy;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author akio
 */
public class MainController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private AnchorPane root;

    @FXML
    private ImageView cameraView;

    public AnchorPane getRoot() {
        return this.root;
    }

    public void setRoot(AnchorPane root) {
        this.root = root;
    }

    public ImageView getCameraView() {
        return this.cameraView;
    }

    public  void setCameraView(ImageView cameraView) {
        this.cameraView = cameraView;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        this.cameraView.fitWidthProperty().bind(MainApp.getPrimaryState().widthProperty());
        this.cameraView.fitHeightProperty().bind(MainApp.getPrimaryState().heightProperty());

        Timeline timer = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Mat cap   = new Mat();
                MainApp.getVideoCapture().read(cap);
                if ( cap.empty() ) {
                    return;
                }
                Mat flip = new Mat();
                Core.flip(cap, flip, 1);
                Mat bgr = new Mat();
                Imgproc.cvtColor(flip, bgr, Imgproc.COLOR_RGB2BGR);
                Mat frame = new Mat();
                Imgproc.resize(bgr, frame, new Size(root.getWidth(), root.getHeight()));

                int type = BufferedImage.TYPE_BYTE_GRAY;
                if ( frame.channels() > 1 ) {
                    type = BufferedImage.TYPE_3BYTE_BGR;
                }
                int bufferSize = frame.channels() * frame.cols() * frame.rows();
                byte [] b = new byte[bufferSize];
                frame.get(0, 0, b); // get all the pixels
                BufferedImage image = new BufferedImage(frame.cols(),frame.rows(), type);
                image.getRaster().setDataElements(0, 0, frame.cols(), frame.rows(), b);
                cameraView.setImage(SwingFXUtils.toFXImage(image, null));
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }
}
