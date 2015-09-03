package jp.saiki.myfirsttoy;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * FXML Controller class
 *
 * @author akio
 */
public class MainController implements Controller {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private AnchorPane root;

    @FXML
    private ImageView cameraView;

    private VideoCapture videoCapture;

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

    static final Path BASE_PATH = Paths.get("/usr/local/opt/opencv3/share/OpenCV/haarcascades/");

    static final String[] HAAR_FACE_CASCADE_XML = new String[]{
//            "haarcascade_frontalcatface.xml",
//            "haarcascade_frontalcatface_extended.xml",
//            "haarcascade_frontalface_alt.xml",
//            "haarcascade_frontalface_alt2.xml",
//            "haarcascade_frontalface_alt_tree.xml",
            "haarcascade_frontalface_default.xml"
    };

    static final String[] HAAR_FULL_BODY_CASCADE_XML = new String[] {
            "haarcascade_fullbody.xml"
    };

    static final String[] HAAR_UPPER_BODY_CASCADE_XML = new String[] {
            "haarcascade_upperbody.xml"
    };



    private Service<Image> service = new Service<Image>() {

        final List<CascadeClassifier> classifiers = Arrays.stream(HAAR_FACE_CASCADE_XML).parallel().map(xmlFileName ->
                        BASE_PATH.resolve(xmlFileName)
        ).map((Path path) -> {
            CascadeClassifier cl = new CascadeClassifier();
            cl.load(path.toAbsolutePath().toString());
            return cl;
        }).collect(Collectors.toList());

        @Override
        protected Task<Image> createTask() {
            return new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    Mat cap   = new Mat();
                    videoCapture.read(cap);
                    if ( cap.empty() ) {
                        return null;
                    }
                    CascadeClassifier classifier = classifiers.get(0);
                    Mat gray = new Mat();
                    Imgproc.cvtColor(cap, gray, Imgproc.COLOR_RGB2GRAY);
                    MatOfRect rects = new MatOfRect();
                    classifier.detectMultiScale(gray, rects);
                    for ( Rect rect : rects.toArray() ) {
                        Point leftTop = new Point(rect.x, rect.y);
                        Point rightBottom = new Point(rect.x + rect.width, rect.y + rect.height);
                        Imgproc.rectangle(cap, leftTop, rightBottom, new Scalar(255, 255, 255));
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
                    return SwingFXUtils.toFXImage(image, null);
                }
            };
        }
    };

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        this.cameraView.fitWidthProperty().bind(MainApp.getPrimaryState().widthProperty());
        this.cameraView.fitHeightProperty().bind(MainApp.getPrimaryState().heightProperty());

        this.videoCapture = new VideoCapture();
        this.videoCapture.open(0);

        this.service.setOnSucceeded(workerStateEvent -> {
            if (this.service.getValue() != null) {
                this.cameraView.setImage(this.service.getValue());
            }
            this.service.restart();
        });
        this.service.start();
    }

    @Override
    public void close() throws IOException {
        this.service.cancel();
        if ( this.videoCapture == null ) {
            return;
        }
        if ( ! this.videoCapture.isOpened() ) {
            return;
        }
        this.videoCapture.release();
    }
}
