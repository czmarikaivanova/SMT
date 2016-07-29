package model;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import smt.Constants;
import smt.Miscellaneous;
import smt.Node;


public abstract class ILPModel {
	protected File input;
	protected IloCplex cplex;

	protected IloNumVar[][] z;
	
	public ILPModel(File input) {
		this.input = input;
	}
	
	public abstract void createModel();
	public abstract void populate();	
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
	
	public static boolean isNumeric(String str)  {  
		  try  {  
		    double d = Double.parseDouble(str);  
		  }  
		  catch(NumberFormatException nfe)  {  
		    return false;  
		  }  
		  return true;  
		}	
	
	// also creates the file !
    public static File generateAMPLData(int instanceID, int vertexCount, int dstCount, Node[] nodes) {
        try
        {
        	File datafile = new File("amplfiles/ampl " +  new File("amplfiles/").list().length + ".dat");
            System.out.println("Saving: AMPL input");
            FileWriter fw = new FileWriter(datafile,false); //the true will append the new data
            fw.write(Constants.INST_ID + instanceID + "\n");
            String dstStr = "set DESTS :=";
            String nonDstStr = "set NONDESTS :=";
            for (int i = 0; i < vertexCount; i++) {
                if (i < dstCount) {
                    dstStr += " " + i ;
                } else {
                    nonDstStr += " " + i;
                }
            }
            dstStr += " ;\n";
            nonDstStr += " ;\n";
            
            String paramStr = "param requir :=\n";
            String distancesStr = "";
            for (int i = 0; i < vertexCount; i++) {
                for (int j = 0; j < vertexCount; j++) {
                    distancesStr += " " +i + " " + j + " " + Miscellaneous.dst(nodes[i].getPoint(), nodes[j].getPoint()) + "\t"; 
                }
                distancesStr += "\n";
            }
            fw.write(dstStr);
            fw.write(nonDstStr);
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
	
}
