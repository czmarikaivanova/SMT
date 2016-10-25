package model;

import graph.Clique;
import graph.Graph;
import graph.Node;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;

import org.javatuples.Quartet;

import smt.Constants;

public class SMTOnlyFlow extends ILPModel {
	protected int n; 
	protected int d;
		
	protected IloNumVar[][][] y;		
	protected IloNumVar[][][][] f;
	public SMTOnlyFlow(Graph graph, boolean allowCrossing) {
		super(graph, allowCrossing);
	}
	
	@Override
	protected void initVars() {
		try {
			n = graph.getVertexCount();
			d = graph.getDstCount();
			cplex = new IloCplex();
			f = new IloNumVar[n][n][d][];
			y = new IloNumVar[n][n][];				
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {			
					for (int s = 0; s < d; s++) {
						f[i][j][s] = cplex.boolVarArray(d);	
					}
					
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

	@Override
	protected void createConstraints() {
		try {
			// create model and solve it				
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							obj.addTerm(graph.getRequir(i,j), y[i][j][s]);
						}
					}
				}
			}
			cplex.addMinimize(obj);				
			// -------------------------------------- constraints							
			
			// Size
			IloLinearNumExpr expr = cplex.linearNumExpr();				
			for (int i = 0; i < n; i++) {					
				for (int j = i+1; j < n; j++) {
					expr.addTerm(1.0, z[i][j]);
				}	
			}
			cplex.addLe(expr, n-1);				
	
			// Y-var
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							for (int t = 0; t < d; t++) {
								if (s != t) {
									IloLinearNumExpr expr1 = cplex.linearNumExpr();
									for (int k = 0; k < n; k++) {
										if ((graph.getRequir(i,k) >= graph.getRequir(i,j)) && (i != k)) {
											expr1.addTerm(1.0, y[i][k][s]);
										}
									}
									cplex.addLe(f[i][j][s][t], expr1);
								}
							}
						}
					}
				}
			}
			IloLinearNumExpr expr1 = cplex.linearNumExpr();
			
			// Flow conservation - normal
			for (int s = 0; s < d; s++) {					
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {						
						if (t != i && s != i && s != t) {
							IloLinearNumExpr expr1a = cplex.linearNumExpr();
							IloLinearNumExpr expr1b = cplex.linearNumExpr();	
							for (int j = 0; j < n; j++) {
								if (i != j && j != s) {
									expr1a.addTerm(1.0, f[i][j][s][t]);									
								}								
							}
							for (int j = 0; j < n; j++) {
								if (i != j && j != t) {								
									expr1b.addTerm(1.0, f[j][i][s][t]);
								}								
							}						
							cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b)));
						}
					}
				}	
			}		
			
			// Flow conservation - dest
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					if (s != t) {
						IloLinearNumExpr expr2a = cplex.linearNumExpr();
						IloLinearNumExpr expr2b = cplex.linearNumExpr();	
						for (int i = 0; i < n; i++) {
							if (i != t) {
								expr2a.addTerm(1.0, f[t][i][s][t]);									
								expr2b.addTerm(1.0, f[i][t][s][t]);									
							}								
						}
						cplex.addEq(-1,cplex.sum(expr2a, cplex.negative(expr2b)));
					}
				}
			}				

			// Flow conservation - source
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					if (s != t) {
						IloLinearNumExpr expr2a = cplex.linearNumExpr();
						IloLinearNumExpr expr2b = cplex.linearNumExpr();	
						for (int i = 0; i < n; i++) {
							if (i != s) {
								expr2a.addTerm(1.0, f[s][i][s][t]);									
								expr2b.addTerm(1.0, f[i][s][s][t]);									
							}								
						}
						cplex.addEq(1,cplex.sum(expr2a, cplex.negative(expr2b)));
					}
				}
			}				
						
			// capacity
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < d; i++) {
						for (int j = 0; j < d; j++) {
							if (j > i && s != t) {
								IloLinearNumExpr expr3 = cplex.linearNumExpr();
								expr3.addTerm(1.0, f[i][j][s][t]);
								expr3.addTerm(1.0, f[j][i][s][t]);
								cplex.addLe(expr3, z[i][j]);
							}
						}
					}
				}
			}	
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
		
	}

	@Override
	public void addCrossCliqueConstraints(ArrayList<Clique> cliqueList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double[][] getZVar() {
		try {
			Double[][] zval = new Double[z.length][z.length];
			for (int i = 0 ; i < z.length; i++) {
				for (int j = i+1; j < z.length; j++) {
					System.out.print(cplex.getValue(z[i][j]) + " ");						
					zval[i][j] = cplex.getValue(z[i][j]);						
				}
				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
			return zval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}	
	}
	public String toString() {
    	return Constants.SMT_ONLY_FLOW_STRING + "(" + Integer.toString(n) + "," + Integer.toString(d) + ")";
	}

	@Override
	public Double[][][] getXVar() {
		// TODO Auto-generated method stub
		return null;
	}
}
