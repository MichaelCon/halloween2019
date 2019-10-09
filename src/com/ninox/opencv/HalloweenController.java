package com.ninox.opencv;

import org.opencv.core.Core;

public class HalloweenController {
	static HalloweenController self;
	
	public static HalloweenController getInstance() {
		if(self == null) {
			self = new HalloweenController();
		}
		return self;
	}
	
	public HalloweenController() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

	}
}
