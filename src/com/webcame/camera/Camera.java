package com.webcame.camera;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class Camera extends Application {

    private static final int SCENE_W = 640;
    private static final int SCENE_H = 480;

    CascadeClassifier faceDetector;
    VideoCapture videoCapture;

    Canvas canvas;
    GraphicsContext g2d;
    Stage stage;
    AnimationTimer timer;

    @Override
    public void start(Stage stage) {

        this.stage = stage;

        initOpenCv();

        canvas = new Canvas(SCENE_W, SCENE_H);
        g2d = canvas.getGraphicsContext2D();
        g2d.setStroke(Color.GREEN);

        Group group = new Group(canvas);

        Scene scene = new Scene(group, SCENE_W, SCENE_H);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        timer = new AnimationTimer() {

            Mat mat = new Mat();

            @Override
            public void handle(long now) {

                videoCapture.read(mat);

                List<Rectangle2D> rectList = detectFaces(mat);

                Image image = mat2Image(mat);

                g2d.drawImage(image, 0, 0);

                for (Rectangle2D rect : rectList) {
                    g2d.strokeRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
                }

            }
        };
        timer.start();

    }

    public List<Rectangle2D> detectFaces(Mat mat) {

        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale( mat, faceDetections);

        System.out.println(String.format("Detected %s faces", faceDetections.toArray().length));

        List<Rectangle2D> rectList = new ArrayList<>();
        for (Rect rect : faceDetections.toArray()) {

            int x = rect.x;
            int y = rect.y;
            int w = rect.width;
            int h = rect.height;

            rectList.add(new Rectangle2D(x, y, w, h));
        }

        return rectList;
    }

    private void initOpenCv() {

        setLibraryPath();

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        videoCapture = new VideoCapture();
        videoCapture.open(0);

        System.out.println("Camera open: " + videoCapture.isOpened());

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {

                timer.stop();
                videoCapture.release();

                System.out.println("Camera released");

            }
        });

        faceDetector = new CascadeClassifier(getOpenCvResource(getClass(), "/opencv/data/lbpcascades/lbpcascade_frontalface.xml"));

    }

    public static Image mat2Image(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
    
     static {
        
    }


    private static void setLibraryPath() {

        try {
            
           // String file = "/home/arun/Downloads/OpenCV/data/lbpcascades/lbpcascade_frontalface.xml";
    //private CascadeClassifier faceCascade = new CascadeClassifier(file);

           // System.setProperty("java.library.path", "lib/x64");
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            //Field fieldSysPath = ClassLoader.class.getDeclaredField("/home/arun/Downloads/OpenCV/data/lbpcascades/lbpcascade_frontalface.xml");
            //fieldSysPath.setAccessible(true);
            //fieldSysPath.set(null, null);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

    }

    public static String getOpenCvResource(Class<?> clazz, String path) {
        try {
            return Paths.get( clazz.getResource(path).toURI()).toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}