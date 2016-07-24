package model;


import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SMTModel extends ILPModel {
	
	
	public SMTModel(File input) {
		super(input);
	}

	static int[] dests;
	static int[] nondests;	
	static int[] vertices;
	static double [][]	 requir;	
	protected int n; 
	protected int d;
	
	protected IloNumVar[][][] x;
	protected IloNumVar[][][] y;	



	public void createModel() {
		initVars();
		createConstraints();
	}
	
	protected void initVars() {
		try {
			n = vertices.length;
			d = dests.length;	
			cplex = new IloCplex();
			x = new IloNumVar[n][n][];
			y = new IloNumVar[n][n][];				
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {				
					x[i][j] = cplex.boolVarArray(d);
					y[i][j] = cplex.boolVarArray(d);				
				}					
			}
			z = new IloNumVar[n][];				
			for (int j = 0; j < n; j++) {		
				z[j] = cplex.boolVarArray(n);	
			}									
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public void createConstraints() {
		try {
			// create model and solve it				
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							obj.addTerm(requir[i][j], y[i][j][s]);
						}
					}
				}
			}
			cplex.addMinimize(obj);				
			// -------------------------------------- constraints							
			
			// Size
			IloLinearNumExpr expr = cplex.linearNumExpr();				
			for (int i = 0; i < n; i++) {					
				for (int j = 0; j < n; j++) {
					if (i < j) {
						expr.addTerm(1.0, z[i][j]);
					}
				}	
			}
			cplex.addLe(expr, n-1);				
	
			// OneDirDest
			for (int j = 0; j < d; j++) {					
				for (int s = 0; s < d; s++) {
					if (j != s) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();			
						for (int i = 0; i < n; i++) {
							if (i != j) {
								expr1.addTerm(1.0, x[i][j][s]);									
							}								
						}
						cplex.addEq(expr1, 1.0);
					}
				}	
			}		
			
			// OneDirNonDest_A
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					for (int k = 0; k < n; k++) {
						if (j != k) {
							IloLinearNumExpr expr2 = cplex.linearNumExpr();
							for (int i = 0; i < n; i++) {
								if (i != j) {
									expr2.addTerm(1.0, x[i][j][s]);
								}
							}
							cplex.addLe(x[j][k][s], expr2);
						}
					}
				}
			}
			
			// OneDirNonDest_B	
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					for (int k = 0; k < n; k++) {
						IloLinearNumExpr expr3 = cplex.linearNumExpr();
						for (int i = 0; i < n; i++) {
							if (i != j) {
								expr3.addTerm(1.0, x[i][j][s]);
							}
						}
						cplex.addLe(expr3, 1.0);
					}
				}
			}
			
			// NonDestNoLEaf
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					IloLinearNumExpr expr4 = cplex.linearNumExpr();
					for (int i = 0; i < n; i++) {
						if (i != j) {
							expr4.addTerm(1.0, x[i][j][s]);
						}
					}
					IloLinearNumExpr expr5 = cplex.linearNumExpr();
					for (int k = 0; k < n; k++) {
						if (j != k) {
							expr5.addTerm(1.0, x[j][k][s]);
						}
					}
					cplex.addLe(expr4, expr5);
				}
			}
	
			// OneDir
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i < j) {
						for (int s = 0; s < d; s++) {
							IloLinearNumExpr expr6 = cplex.linearNumExpr();								
							expr6.addTerm(1.0, x[i][j][s]);
							expr6.addTerm(1.0, x[j][i][s]);
							cplex.addEq(expr6, z[i][j]);						
						}
					}
				}
			}
	
			// NoCycles
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < d; j++) {
					if (i != j) {
						cplex.addEq(0.0, x[i][j][j]);
					}
				}
			}
	
	//		// YVar
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							IloLinearNumExpr expr7 = cplex.linearNumExpr();
							for (int k = 0; k < n; k++) {
								if ((requir[i][k] >= requir[i][j]) && (i != k)) {
									expr7.addTerm(1.0, y[i][k][s]);
								}
							}
//							cplex.addLazyConstraint((IloRange) cplex.le(x[i][j][s], expr7));
							cplex.addLe(x[i][j][s], expr7);
						}			
					}
				}					
			}			
			
		//	cplex.addEq(z[0][1], 0);
			//cplex.addEq(z[0][1], 1);
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}

	
	/**
	 *  Create AMPL file
	 */
	public void populate() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(input));
			String line;
			String[] indices;
			while ((line = br.readLine()) != null) {
				if (line.matches("set DESTS.*")) {
					indices = line.split(" ");						
					dests = new int[indices.length - 4];
					for (int i = 3; i < indices.length; i++) {
						if (isNumeric(indices[i])) {
							dests[i-3] = Integer.parseInt(indices[i]);								
						}							
					}
				} else if (line.matches("set NONDESTS.*")) {
					indices = line.split(" ");						
					nondests = new int[indices.length - 4];
					for (int i = 3; i < indices.length; i++) {
						if (isNumeric(indices[i])) {
							nondests[i-3] = Integer.parseInt(indices[i]);								
						}							
					}
					
				} else if (line.matches("param.*")) {
					append(); // crate array vertices
					requir = new double[vertices.length][vertices.length];
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
	
	private static void append() {
		vertices = new int[dests.length + nondests.length];
		for (int i = 0; i < dests.length; i++) {
			vertices[i] = dests[i];
		}
		for (int i = 0; i < nondests.length; i++) {
			vertices[i + dests.length] = nondests[i];
		}

	}
	
}
	
	
	
