package smt;

import java.awt.Point;

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
}
