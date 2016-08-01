package smt;

import java.awt.Point;
import java.awt.geom.Line2D;

public class Miscellaneous {
    
    public static float dst(Point v1, Point v2) {
    	return (float) (Math.pow(v1.getX()-v2.getX(),2) + Math.pow(v1.getY()-v2.getY(),2));
    }
    
	public static boolean isNumeric(String str)  {  
		  try  {  
		    double d = Double.parseDouble(str);  
		  }  
		  catch(NumberFormatException nfe)  {  
		    return false;  
		  }  
		  return true;  
	}	    
	
	public static boolean edgesProperlyIntersect(Point p1, Point p2, Point p3, Point p4) {
        //determine if the lines intersect
        boolean intersects = Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
        //determines if the lines share an endpoint
        boolean shareAnyPoint = shareAnyPoint(p1, p2, p3, p4);
        if (intersects && !shareAnyPoint) {
            return true;
        } 
        return false;
	}

	private static boolean shareAnyPoint(Point p1, Point p2, Point p3, Point p4) {
		   if (isPointOnTheLine(p1, p2, p3)) return true;
		    else if (isPointOnTheLine(p1, p2, p4)) return true;
		    else if (isPointOnTheLine(p3, p4, p1)) return true;
		    else if (isPointOnTheLine(p3, p4, p2)) return true;
		    else return false;
	}

	private static boolean isPointOnTheLine(Point p1, Point p2, Point p) {
	    //handle special case where the line is vertical
	    if (p2.x == p1.x) {
	        if(p1.x == p.x) return true;
	        else return false;
	    }
	    double m = (p2.y - p1.y) / (p2.x - p1.x);
	    if ((p.y - p1.y) == m * (p.x - p1.x)) return true;
	    else return false;
	}
}
