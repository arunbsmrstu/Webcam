package com.webcame.video;

import static com.webcame.camera.Camera.mat2Image;
import com.webcame.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import static sun.management.snmp.jvminstr.JvmThreadInstanceEntryImpl.ThreadStateMap.Byte0.runnable;

/**
 * FXML Controller class
 *
 * @author arun
 */
public class VideoController {
    
    @FXML
    private Label faceDetection;

    @FXML
    private ImageView videoView;
    @FXML
    private ImageView face;
    //@FXML
    //private Button camer;
    @FXML
    private Button image;
    @FXML
    private Button close;

    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private VideoCapture capture;
    // a flag to change the button behavior
    private boolean cameraActive = false;

    private int width = 640, height = 480;

    // face cascade classifier
    String file = "/home/arun/Downloads/OpenCV/data/lbpcascades/lbpcascade_frontalface.xml";
    private CascadeClassifier faceCascade = new CascadeClassifier(file);
    private int absoluteFaceSize;
    
    
    CascadeClassifier faceDetector;
    VideoCapture videoCapture;
    AnimationTimer atimer;
    GraphicsContext g2d;
    Thread th;
   
    
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }


    public void initialize() {

        /*
        if (!this.cameraActive) {
            capture = new VideoCapture(0);
            if (capture.isOpened()) {
                this.cameraActive = true;
                System.out.println("Camera is open");

                //grab a frame every 33ms(30 frames/sec)
                Runnable frameGrabber;
                frameGrabber = new Runnable() {
                    @Override
                    public void run() {

                        //efective grab and process a single frame
                        Mat frame = grabFrame();
                        
                        //if the frame is not empty, process if 
                        if (!frame.empty()) {
                            //face detection
                            detectAndDisplay(frame);
                            
                        }

                        //convert and show the frame
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(videoView, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
            } else {
                System.err.println("Failed to open camra connection.....");
            }
        } else {
            // the camera is not active at this point
            this.cameraActive = false;
            // stop the timer
            this.stopAcquisition();
        }
        */
        
        
        /*
        if (!this.cameraActive) {
            capture = new VideoCapture(0);
            if (capture.isOpened()) {
                this.cameraActive = true;
                System.out.println("Camera is open");
                atimer = new AnimationTimer() {
                    @Override
                    public void handle(long now) {
                        Mat frame = grabFrame();
                        
                        if (frame!=null){
                            detectAndDisplay(frame);
                        }else{
                            System.out.println("no frm");
                        }
                        
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(videoView, imageToShow);
                    }
                };
                atimer.start();
                //this.atimer = Executors.newSingleThreadScheduledExecutor();
                //this.atimer.scheduleAtFixedRate(atimer, 0, 33, TimeUnit.MILLISECONDS);
        
        
        
            }else{
                System.err.println("Failed to open camra connection.....");
        }
        
        
        }else{
            // the camera is not active at this point
            this.cameraActive = false;
            // stop the timer
            this.stopAcquisition();
        }*/
        
        
        
        
        if (!this.cameraActive) {
            capture = new VideoCapture(0);
            if (capture.isOpened()) {
                this.cameraActive = true;
                System.out.println("Camera is open");
                Task<Void> task = new Task<Void>() {

		
			@Override
			protected Void call() throws Exception {

				while (capture.isOpened()) {
					try {
                                            Mat frame=grabFrame();
						if (frame!= null) {
                                                    Platform.runLater(new Runnable() {
								@Override
								public void run() {
									//final Image mainiamge = SwingFXUtils.toFXImage(grabbedImage, null);
                                                                        //detectAndDisplay(bufferedImageToMat(grabbedImage));
									//imageProperty.set(mainiamge);
                                                                        detectAndDisplay(frame);
                                                                        Image imageToShow =mat2Image(frame);
                                                                        updateImageView(videoView, imageToShow);
                                                                       
                                                                }
							});

							//grabbedImage.flush();

						}
					} catch (Exception e) {
					} finally {

					}

				}

				return null;

			}

		};
		th = new Thread(task);
		th.setDaemon(true);
		th.start();
                
            }else{
                System.err.println("Failed to open camra connection.....");
        }
        
        
        }else{
            // the camera is not active at this point
            this.cameraActive = false;
            // stop the timer
            this.stopAcquisition();
        }
        
    }
    
    
    @FXML
    private void image(ActionEvent event) {
        Mat frame = grabFrame();
        Image imageToShow = Utils.mat2Image(frame);
        face.setImage(imageToShow);
        
        Platform.runLater(new Runnable() {
            @Override public void run() {
                faceDetection.setText("Hello");
            }
        });
    }

//<editor-fold defaultstate="collapsed" desc="comment">
    
//</editor-fold>
  
    /**
     * Get a frame from the opened video stream if any
     *
     * @return @linked Image to show
     *
     *
     */
    private Mat grabFrame() {
        Mat frame = new Mat();

        if (this.capture.isOpened()) {
            try {
                //read the current frame
                this.capture.read(frame);

            } catch (Exception exc) {
                System.err.println("Exception during the image elaboration" + exc);
            }
        }
        
        System.out.println("height :"+frame.height());
        System.out.println("weight :"+frame.width());

        return frame;
    }

    /**
     * Method for face detection and tracking
     */
    private void detectAndDisplay(Mat frame) {

        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        //convert the frame in gray scale
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

        //equalize the frame histogram to improve the result
        Imgproc.equalizeHist(grayFrame, grayFrame);

        // compute minimum face size (20% of the frame height, in our case)
        if (this.absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                this.absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        // detect faces
        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
                new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());

       // System.out.println(String.format("Detected %s faces", faces.toArray().length));
        

        // each rectangle in faces is a face: draw them!
        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) {
            Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
        }

    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition() {
     
        if (this.capture.isOpened()) {
            // release the camera
            //atimer.stop();
            th.stop();
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view the {@link ImageView} to update
     * @param image the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    public void setClosed() {
        this.stopAcquisition();
    }

}
