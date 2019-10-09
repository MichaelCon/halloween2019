package com.ninox.opencv;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

public class FaceTrack {
    final static String filenameFaceCascade = "/Users/mconcannon/workspace/faceDetect/haarcascade_frontalface_alt.xml";
    final static String filenameEyesCascade = "/Users/mconcannon/workspace/faceDetect/haarcascade_eye_tree_eyeglasses.xml";
	
    final static boolean DISPLAY = true;
    
	CascadeClassifier faceCascade;
	CascadeClassifier eyesCascade;
	VideoCapture capture;
	Skeleton scott;
	long t1,t2;
	
	public void detectAndDisplay(Mat frame) {
        Mat frameGray = new Mat();
        Mat smallFrameGray = new Mat();
        // Convert it to B&W
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        //System.out.println("Size " + frame.size());
        Imgproc.resize(frameGray, smallFrameGray, new Size(), 0.5, 0.5, Imgproc.INTER_AREA);
        Core.flip(smallFrameGray, frameGray, -1);
        //frameGray = smallFrameGray;
        //System.out.println("Size " + frameGray.size());
        // Normalize
        Imgproc.equalizeHist(frameGray, frameGray);
        // Find the faces
        MatOfRect faces = new MatOfRect();
        t1 = System.currentTimeMillis();
        faceCascade.detectMultiScale(frameGray, faces);
        t2 = System.currentTimeMillis();
        //System.out.println(" Detect Time : " + (t2 - t1));
        List<Rect> listOfFaces = faces.toList();
        for (Rect face : listOfFaces) {
            Point center = new Point(face.x + face.width / 2, face.y + face.height / 2);
            //Imgproc.ellipse(frame, center, new Size(face.width / 2, face.height / 2), 0, 0, 360, new Scalar(255, 0, 255));
            Imgproc.ellipse(frameGray, center, new Size(face.width / 2, face.height / 2), 0, 0, 360, new Scalar(255, 0, 255));
            // -- In each face, detect eyes
//            Mat faceROI = frameGray.submat(face);
//            MatOfRect eyes = new MatOfRect();
//            eyesCascade.detectMultiScale(faceROI, eyes);
//            List<Rect> listOfEyes = eyes.toList();
//            for (Rect eye : listOfEyes) {
//                Point eyeCenter = new Point(face.x + eye.x + eye.width / 2, face.y + eye.y + eye.height / 2);
//                int radius = (int) Math.round((eye.width + eye.height) * 0.25);
//                Imgproc.circle(frame, eyeCenter, radius, new Scalar(255, 0, 0), 4);
//            }
        }
        if(listOfFaces.size() > 0) {
        	Rect face = listOfFaces.get(0);
        	int center = face.x + face.width / 2;
        	System.out.println("Face at : " + center);
        	// 620 center 1100 200 -- so 500 is about 45 degrees - 1 degree per 10 pixels
//        	int angle = 620 - center; // also reverses value (ie * -1)
//        	angle /= 10;
//        	angle += 90;
        	
        	int angle = (480 - center) / 20;
        	scott.rotate(angle);
        }
        if(DISPLAY) {
        	//HighGui.imshow("Capture - Face detection", frame );
        	HighGui.imshow("Capture - Face detection", frameGray );
        	HighGui.waitKey(10);
        }
    }
	
    public void setup() {
        // When no usb webcam, 0 is mac camera
        // When USB webcam, 0 is null mac is 2 and webcam is 3
        int cameraDevice = 0;
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
    
    public void loop() {
        Mat frame = new Mat();
        long t1 = System.currentTimeMillis();
        while (capture.read(frame)) {
            if (frame.empty()) {
                System.err.println("Video Problem !");
                break;
            }
            detectAndDisplay(frame);
            long t2 = System.currentTimeMillis();
            //System.out.println("Loop Time: " + (t2 - t1));
            t1 = t2;
        }
        System.exit(0);
    }

    public byte[] takePic() {
        Mat frame = new Mat();
        
        int countDown = 60;
        while(countDown-- > 0) {
        	capture.read(frame);
        	HighGui.imshow("Capture - Face detection", frame );
        	HighGui.waitKey(10);
        	System.out.println(countDown);
        }
        return toJPG(frame);
    }
    
	public byte[] toJPG(Mat frame) {
		MatOfByte buffer = new MatOfByte();
		MatOfInt compressParams = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 80);
		Imgcodecs.imencode(".jpg", frame, buffer, compressParams);
		byte[] imageArr = new byte[(int) (buffer.total() * buffer.elemSize())];
		buffer.get(0, 0, imageArr);
		return imageArr;
	}
	
	void writeByte(byte[] bytes, String file) { 
        try { 
            OutputStream os = new FileOutputStream(file); 
            os.write(bytes); 
            os.close(); 
        } catch (Exception e) { 
            System.out.println("Exception: " + e); 
        } 
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
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		FaceTrack me = new FaceTrack();
		//me.moveToAngle(90);
		me.setup();
		//me.loop();
		
		byte[] pic = me.takePic();
		me.writeByte(pic, "/Users/mconcannon/test.jpg");
		MSFaceDetection msft = new MSFaceDetection();
		List<MSFace> list = msft.detectFaces(pic);
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