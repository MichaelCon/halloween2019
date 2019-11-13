package com.ninox.opencv;

import java.util.Comparator;

import org.opencv.core.Rect;

public class RectAreaComparator implements Comparator<Rect> {

	public int compare(Rect r1, Rect r2) {
		return sign(r2.area() - r1.area());
	}

    private int sign(double d) {
        if(d > 0)
            return 1;
        if(d < 0)
            return -1;
        return 0;
    }
}
