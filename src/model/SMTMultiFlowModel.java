package model;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import graph.Graph;

public class SMTMultiFlowModel extends SMTModel {

	public SMTMultiFlowModel(Graph graph, boolean willAddVIs, boolean isLP) {
		super(graph, willAddVIs, isLP);
	}
	
	protected IloNumVar[][][][] f;
	
	@Override
	protected void initVars() {
//		n = graph.getVertexCount();
//		d = n - 1;
		try {
			super.initVars();
			cplex = new IloCplex();
			f = new IloNumVar[n][n][d][];
			for (int i = 0; i < n; i++) {				
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						if (isLP) {
							f[i][j][k] = cplex.numVarArray(d, 0, 1);
						}
						else {
							f[i][j][k] = cplex.boolVarArray(d);							
						}
					}
				}					
			}

		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void createConstraints() {
		try {
			super.createConstraints();
			// create model and solve it							
			// -------------------------------------- constraints							
			

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
//			for (int s = 0; s < n; s++) {
//				for (int t = 0; t < n; t++) {
//					for (int i = 0; i < n; i++) {
//						for (int j = 0; j < n; j++) {
//							if (j > i && s != t) {
//								cplex.addLe(f[i][j][s][t], x[i][j][s]);
//							}
//						}
//					}
//				}
//			}		
			
			
			// capacity alt
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j > i && s != t) {
								cplex.addLe(cplex.sum(f[i][j][s][t], f[j][i][s][t]), z[i][j]);
							}
						}
					}
				}
			}					
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}	
	}
	
	public Double[][][][] getFVar() {
		try {
			Double[][][][] fval = new Double[z.length][z.length][z.length][z.length];
			for (int i = 0 ; i < f.length; i++) {
				for (int j = 0; j < f.length; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							for (int t = 0; t < d; t++) {
								if (t != s) {
									 fval[i][j][s][t] = cplex.getValue(f[i][j][s][t]);						
								}
							}
						}
					}
				}
			}
			System.out.println("Objective: " + cplex.getObjValue());
			return fval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	
	
	public String toString() {
    	return Constants.SMT_MULTI_FLOW_STRING + "(" + n + "," + d + ")";
	}
		


}
