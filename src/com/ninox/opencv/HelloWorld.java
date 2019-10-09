package com.ninox.opencv;

import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/** Simple app to prove OpenCV is installed */
public class HelloWorld extends JPanel {

	public HelloWorld() {
		super();
	}
		
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat mat = Mat.eye(3, 3, CvType.CV_8UC1);
        System.out.println("mat = " + mat.dump());
	}

}
