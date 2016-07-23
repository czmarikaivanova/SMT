package logs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import smt.Edge;
import smt.Pair;


public class ViLogger {
	
	private File constraintFile;
	private static String DELIMITER = "------------------------------";
	
	
	public ViLogger(File constraintFile) {
		this.constraintFile = constraintFile;
		try
		{				
			FileWriter fw = new FileWriter(constraintFile,true); 
			fw.write(DELIMITER);
		    fw.close();
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
		}
	}
	
	

	
	public void addPlanarConstraints(ArrayList<Pair<Edge, Edge>> edgePairList) {
		for (Pair<Edge, Edge> edgePair: edgePairList) {
			try
			{				
				FileWriter fw = new FileWriter(constraintFile,true); 
				fw.write("z[" + edgePair.getFirst().getU().id + "," + edgePair.getFirst().getV().id + "]<=z["
				+ edgePair.getSecond().getU().id + "," + edgePair.getSecond().getV().id + "])");
			    fw.close();
			}
			catch(IOException ioe)
			{
			    System.err.println("IOException: " + ioe.getMessage());
			}
		}
		
		
	}

}
