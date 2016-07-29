package model;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.io.File;
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
	
	public ILPModel(Graph graph) {
		this.graph = graph;
		amplDataFile = generateAMPLData(graph);
		populate();
		createModel();		
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
	
	// also creates the file !
    public File generateAMPLData(Graph graph) {
        try
        {
        	File datafile = new File("amplfiles/ampl " +  new File("amplfiles/").list().length + ".dat");
            System.out.println("Saving: AMPL input");
            FileWriter fw = new FileWriter(datafile,false); //the true will append the new data
            fw.write(Constants.INST_ID + graph.getInstId() + "\n");
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
            String paramStr = "param requir :=\n";
            String distancesStr = "";
            for (int i = 0; i < graph.getVertexCount(); i++) {
                for (int j = 0; j < graph.getVertexCount(); j++) {
                    distancesStr += " " +i + " " + j + " " + Miscellaneous.dst(graph.getNode(i).getPoint(), graph.getNode(j).getPoint()) + "\t"; 
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
