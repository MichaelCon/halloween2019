package com.ninox.opencv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class HalloweenController {
	public static final long CLOUD_EVERY = 30000;
	public static final long CLOUD_LIMIT = 5000;
	
	static HalloweenController self;
	FaceTrack camera;
	Skeleton scott;
	SimpleWebServer ears;
	MSFaceDetection msft;
	Process voice;
	
	boolean cloudAuto = true;			// default to on
	boolean skeletonDefault = true;

	boolean cloudOnNextFace = false;
	long nextCloudTime = 0;
	
	public static HalloweenController getInstance() {
		if(self == null) {
			self = new HalloweenController();
		}
		return self;
	}
	
	public HalloweenController() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public void setup() {		
		camera = new FaceTrack();
		camera.cameraDevice = 3;	// 0 (laptop) or 3 (usb)
		camera.setup();
		camera.controller = this;
		camera.start();
		
		scott = new Skeleton();
		scott.connected = skeletonDefault;
		scott.rotate(90);
		scott.eyes(false);
		
		msft = new MSFaceDetection();
		
		ears = new SimpleWebServer();
		ears.controller = this;
		ears.start();
	}
	
	/** Called by the face tracking / eyes of the system */
	public void handleFaces(List<Rect> listOfFaces) {
		int angle = 90;
		
		// Remove faces that are too big or small
		ArrayList<Rect> validFaces = new ArrayList<Rect>();
		for(Rect face : listOfFaces) {
			if(face.width >= 25 && face.width < 200) {
				validFaces.add(face);
			}
		}
		// We see a face
        if(validFaces.size() > 0) {
        	Collections.sort(validFaces, new RectAreaComparator());
        	Rect face = validFaces.get(0);
        	int center = face.x + face.width / 2;
        	angle = 500 - center; // also reverses value (ie * -1)
        	angle /= 10;
        	angle += 90;
        	// 620 center 1100 200 -- so 500 is about 45 degrees - 1 degree per 10 pixels
        	scott.rotate(angle);
        	scott.eyes(true);
        	if(cloudOnNextFace || 
        			(cloudAuto && nextCloudTime < System.currentTimeMillis())) {
        		cloudOnNextFace = false;
        		cloudLook();
        	}
        } else {
        	scott.eyes(false);
        }		
	}
	
	public void handlerWebCommand(char c) {
		System.out.println("Command : " + c);
		if(c == 'm')
			cloudOnNextFace = true;
		else if(c == 'd')
			bladeDropped();
		else if(c == 'A')
			cloudAuto = true;
		else if(c == 'a')
			cloudAuto = false;
	}
	
	void getVoice(boolean waitForIt, boolean killIt) {
		// Wait until the current speaking is done
		if(waitForIt) {
			while(voice != null && voice.isAlive()) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// Kill the current speaking
		if(killIt && voice != null && voice.isAlive()) {
			voice.destroy();
		}
	}
	
	void say(String s, boolean waitForIt, boolean killIt) {
		getVoice(waitForIt, killIt);
		Runtime rt = Runtime.getRuntime();
		try {
			voice = rt.exec("say -v whisper \"" + s + "\"");
			//voice = rt.exec("say \"" + s + "\"");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		scott.jaw();
	}
	void laugh() {
		getVoice(false, true);
	    try
	    {
	    	// This audio clip seems to intefere with opencv library - so going with subprocess
//	        Clip clip = AudioSystem.getClip();
//	        clip.open(AudioSystem.getAudioInputStream(new File("/Users/mconcannon/sounds/2019Laugh.wav")));
//	        clip.start();
	    	Runtime rt = Runtime.getRuntime();
	    	voice = rt.exec("/usr/bin/afplay /Users/mconcannon/sounds/2019Laugh.wav");
	    }
	    catch (Exception exc)
	    {
	        exc.printStackTrace(System.out);
	    }
		scott.jaw();
	}
	
	/** Go run the cloud based face finding */
	public void cloudLook() {
		System.out.println("Calling msft");
		nextCloudTime = System.currentTimeMillis() + CLOUD_LIMIT;
		byte[] jpg = camera.toJPG();
		List<MSFace> list = msft.detectFaces(jpg);
		
		System.out.println("Found " + list.size());
		//say("Found " + list.size() + " face", true, false);
		if(list.size() > 0) {
			nextCloudTime = System.currentTimeMillis() + CLOUD_EVERY;
			for(MSFace f : list) {
				System.out.println(f.json);
				f.parseJSON();
				StringBuffer sb = new StringBuffer("I see a ");
				sb.append(f.age).append(" year old ");
				if(f.bald > 0.85)
					sb.append("bald ");
				sb.append(f.gender);
				say(sb.toString(), true, false);
			}
			say("Reach in for a halloween treat or trick", true, false);
			writeByte(jpg, "/Users/mconcannon/halloweenPics/pic" + System.currentTimeMillis() + ".jpg");
		}

	}
	
	/** Write a set of bytes to a file */
	void writeByte(byte[] bytes, String file) { 
        try { 
            OutputStream os = new FileOutputStream(file); 
            os.write(bytes); 
            os.close(); 
        } catch (Exception e) { 
            System.out.println("Exception: " + e); 
        } 
    } 
	
	// Functions
	// Respond to blade drop!!!
	public void bladeDropped() {
		System.out.println("Got the message");
		laugh();
	}
	
	public static void main(String[] args) {
		HalloweenController me = HalloweenController.getInstance();
		me.setup();
		
		Scanner keyboard = new Scanner(System.in);
		int x = 500;
		while(x-- > 0) {
			String key = keyboard.next();
			System.out.println("Key = " + key);
			if(key.startsWith("m")) {
				me.cloudOnNextFace = true;
				System.out.println("Cloud triggered");
			}
		}
		keyboard.close();
		
		me.writeByte(me.camera.toJPG(), "/Users/mconcannon/test.jpg");
	}	
}
