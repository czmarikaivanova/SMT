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
	
	public boolean[][] getZVar() {
		try {
			boolean[][] zval = new boolean[z.length][z.length];
			for (int i = 0 ; i < z.length; i++) {
				for (int j = 0; j < z.length; j++) {
					if (i < j) {
						System.out.print(cplex.getValue(z[i][j]) + " ");						
						zval[i][j] = cplex.getValue(z[i][j]) < 0.5 ? false : true;						
					}
				}
				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
			cplex.end();
			return zval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
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
