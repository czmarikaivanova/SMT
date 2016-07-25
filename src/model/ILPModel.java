package model;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.io.File;


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
	
	
	
}
