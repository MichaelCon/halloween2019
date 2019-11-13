package com.ninox.opencv;

import java.util.Comparator;

import org.opencv.core.Rect;

public class RectXCoorComparator implements Comparator {

	int x;
	
	public RectXCoorComparator(int value) {
		x = value;
	}
	
	public int compare(Object o1, Object o2) {
		Rect r1 = (Rect) o1;
		Rect r2 = (Rect) o2;
		return Math.abs(x - center(r1)) - Math.abs(x - center(r2));
	}

    private int center(Rect r) {
    	return r.x + r.width / 2;
    }
}
