package com.ninox.opencv;

import java.io.IOException;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class FaceTrack implements Runnable {
    final static String filenameFaceCascade = "/Users/mconcannon/workspace/faceDetect/haarcascade_frontalface_alt.xml";
    final static String filenameEyesCascade = "/Users/mconcannon/workspace/faceDetect/haarcascade_eye_tree_eyeglasses.xml";
	
    final static boolean DISPLAY = true;
    
	CascadeClassifier faceCascade;
	CascadeClassifier eyesCascade;
	VideoCapture capture;
	Skeleton scott;
	long t1,t2;
	boolean freeze = false;
	Thread runner;
	Mat latestFrame;
	HalloweenController controller;

    // When no usb webcam, 0 is mac camera
    // When USB webcam, 0 is null mac is 2 and webcam is 3
    int cameraDevice = 0;
    boolean downsample = true;
	
	/** Process the frame, then call the controller with list of faces */
	public List<Rect> detectAndDisplay(Mat frame) {
        Mat frameGray = new Mat();
        // Convert it to B&W
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        
        if(downsample)
        	Imgproc.resize(frameGray, frameGray, new Size(), 0.5, 0.5, Imgproc.INTER_AREA);
//        Core.flip(frameGray, frameGray, -1);
        
        // Normalize
        Imgproc.equalizeHist(frameGray, frameGray);
        // Find the faces
        MatOfRect faces = new MatOfRect();
        t1 = System.currentTimeMillis();
        faceCascade.detectMultiScale(frameGray, faces);
        t2 = System.currentTimeMillis();
        //System.out.println(" Detect Time : " + (t2 - t1));
        List<Rect> listOfFaces = faces.toList();
        if(DISPLAY) {
	        for (Rect face : listOfFaces) {
	            Point center = new Point(face.x + face.width / 2, face.y + face.height / 2);
	            Imgproc.ellipse(frameGray, center, new Size(face.width / 2, face.height / 2), 0, 0, 360, new Scalar(255, 0, 255));
	        }
        	//HighGui.imshow("Capture - Face detection", frame );
        	HighGui.imshow("Capture - Face detection", frameGray );
        	HighGui.waitKey(10);
        }
    	if(controller != null)
    		controller.handleFaces(listOfFaces);
        return listOfFaces;
    }
	
    public void setup() {
        faceCascade = new CascadeClassifier();
        eyesCascade = new CascadeClassifier();
        if (!faceCascade.load(filenameFaceCascade)) {
            System.err.println("Error loading face cascade: " + filenameFaceCascade);
            System.exit(0);
        }
        if (!eyesCascade.load(filenameEyesCascade)) {
            System.err.println("Error loading eyes cascade: " + filenameEyesCascade);
            System.exit(0);
        }
        capture = new VideoCapture(cameraDevice);
        if (!capture.isOpened()) {
            System.err.println("Error opening video capture");
            System.exit(0);
        }
        scott = new Skeleton();
    }
    
    public void run() {
        Mat frame = new Mat();
        while (capture.read(frame) && !freeze) {
        	latestFrame = frame;
            if (frame.empty()) {
                System.err.println("Video Problem !");
                break;
            }
            detectAndDisplay(frame);
        }
    }
    
    public void start() {
    	freeze = false;
    	runner = new Thread(this);
    	runner.start();
    }
    
    public void stop() {
    	freeze = true;
    }
    
    /** Get the latest frame as a JPG */
	public byte[] toJPG() {
		MatOfByte buffer = new MatOfByte();
		MatOfInt compressParams = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 80);
		Imgcodecs.imencode(".jpg", latestFrame, buffer, compressParams);
		byte[] imageArr = new byte[(int) (buffer.total() * buffer.elemSize())];
		buffer.get(0, 0, imageArr);
		return imageArr;
	}
	
	void say(String s) {
		Runtime rt = Runtime.getRuntime();
		try {
			Process pr = rt.exec("say -v whisper \"" + s + "\"");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		FaceTrack me = new FaceTrack();
		//me.moveToAngle(90);
		me.setup();
		me.start();
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Click");
		
		MSFaceDetection msft = new MSFaceDetection();
		List<MSFace> list = msft.detectFaces(me.toJPG());
		for(MSFace f : list) {
			System.out.println(f.json);
			f.parseJSON();
			StringBuffer sb = new StringBuffer("I see a ");
			sb.append(f.age).append(" year old ");
			if(f.bald > 0.85)
				sb.append("bald ");
			sb.append(f.gender);
			me.say(sb.toString());
		}
		
		HighGui.destroyAllWindows();
		me.say("Hello");
	}
}   