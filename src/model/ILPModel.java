package model;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import smt.Constants;
import smt.Graph;
import smt.Miscellaneous;

public abstract class ILPModel {
	protected File amplDataFile;
	protected IloCplex cplex;
	protected IloNumVar[][] z;
	protected Graph graph;
	protected double [][]	 requir;	
	
	public ILPModel(Graph graph) {
		this.graph = graph;
		amplDataFile = generateAMPLData();
		populate();
		createModel();		
	}
	
	public abstract void createModel();
	public abstract boolean[][] getZVar();
	
	public boolean solve() {
		try {
			return cplex.solve();
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public IloCplex getModel() {
		return cplex;
	}
	
	// also creates the file !
    public File generateAMPLData() {
        try
        {
        	File datafile = new File("amplfiles/ampl" +  new File("amplfiles/").list().length + ".dat");
            System.out.println("Saving: AMPL input");
            FileWriter fw = new FileWriter(datafile,false); //the true will append the new data
            fw.write(Constants.INST_ID + graph.getInstId() + "\n");
            String head = genAmplHead();
            String crossStr = genCrossing();
            String paramStr = "param requir :=\n";
            String distancesStr = "";
            for (int i = 0; i < graph.getVertexCount(); i++) {
                for (int j = 0; j < graph.getVertexCount(); j++) {
                    distancesStr += " " +i + " " + j + " " + Miscellaneous.dst(graph.getNode(i).getPoint(), graph.getNode(j).getPoint()) + "\t"; 
                }
                distancesStr += "\n";
            }
            fw.write(head);
            fw.write(crossStr);
            fw.write(paramStr);
            fw.write(distancesStr + ";");
            fw.close();
            return datafile;
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
            return null;
        } 
    }	 
    
    private String genCrossing() {
    	String crossStr = "";
    	for (int i = 0; i < graph.getVertexCount(); i++) {
    		for (int j = i+1; j < graph.getVertexCount(); j++) {
    	    	for (int k = i+1; k < graph.getVertexCount(); k++) {
    	    		for (int l = k+1; l < graph.getVertexCount(); l++) {
    	    			if (i!=k && i!=l && j!=k && j!=l) {
							if (Miscellaneous.edgesProperlyIntersect(graph.getNode(i).getPoint(), 
									graph.getNode(j).getPoint(), 
									graph.getNode(k).getPoint(), 
									graph.getNode(l).getPoint())) {
								crossStr += "(" + i + "," + j + "," + k + "," + l + ") ";
							}    	    				
    	    			}
    	    		}
    	    	}    			
    		}
    	}
    	return  "set CROSS := " + crossStr + ";\n"; 
    }

	protected String genAmplHead() {
        String dstStr = "set DESTS :=";
        String nonDstStr = "set NONDESTS :=";
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (i < graph.getDstCount()) {
                dstStr += " " + i ;
            } else {
                nonDstStr += " " + i;
            }
        }
        dstStr += " ;\n";
        nonDstStr += " ;\n";
        return dstStr + nonDstStr;
    }
    
	/**
	 *  Create AMPL file
	 */
	public void populate() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(amplDataFile));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.matches("CROSS.*")) {
					// TODO !!
				}
				else if (line.matches("param.*")) {
					requir = new double[graph.getVertexCount()][graph.getVertexCount()];
					String parLine;
					while ((parLine = br.readLine()) != null) {	
						if (parLine.length() > 2) { // skip the last line
							String[] entries = parLine.split("\t");
							for (String s: entries) {
								s = s.trim();
								String[] entry = s.split(" ");
								requir[Integer.parseInt(entry[0])][Integer.parseInt(entry[1])] = Double.parseDouble(entry[2]);								
							}
						}
					}
				}					
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {				
			e.printStackTrace();
		}
	}    
}
