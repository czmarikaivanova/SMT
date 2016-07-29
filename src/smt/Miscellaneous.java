package smt;

import java.awt.Point;

public class Miscellaneous {
    
    public static float dst(Point v1, Point v2) {
    	return (float) (Math.pow(v1.getX()-v2.getX(),2) + Math.pow(v1.getY()-v2.getY(),2));
    }
}
