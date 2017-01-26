package model;

import javax.swing.SpringLayout.Constraints;

import smt.Constants;

import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;

public class STFlow extends ILPModel {

	private int s;
	private int t;
	private double[][] cap;
	private IloNumVar[][][][] f;

	
	
	public STFlow(Graph graph, Double[][][] xvar) {
		super(graph, false, Constants.LP, false);
		cap = new double[n][n];
		for (int i = 0; i < n; i ++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
//					this.cap[i][j] = xvar[i][j][s];
					this.cap[i][j] = 1.0;
				}
			}
		}
	}
	
	@Override
	protected void initVars() {
		try {
			s = 0;
			t = 1;
			f = new IloNumVar[n][n][d][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						f[i][j][k] = cplex.numVarArray(d,0,1);	
					}	
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}

	protected void createObjFunction() {
		try {
			
			IloLinearNumExpr obj1 = cplex.linearNumExpr();
			IloLinearNumExpr obj2 = cplex.linearNumExpr();	
			for (int i = 0; i < n; i++) {
				if (i != t) {
					obj1.addTerm(1.0, f[t][i][s][t]);									
					obj2.addTerm(1.0, f[i][t][s][t]);									
				}								
			}
			cplex.addMinimize(cplex.sum(obj1, cplex.negative(obj2)));
					
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createConstraints() {
		try {
			// Flow conservation - normal
//			for (int s = 0; s < d; s++) {					
//				for (int t = 0; t < d; t++) {
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
//				}	
//			}		
			
			// Flow conservation - dest
//			for (int s = 0; s < d; s++) {
//				for (int t = 0; t < d; t++) {
//					if (s != t) {
						IloLinearNumExpr expr2a = cplex.linearNumExpr();
						IloLinearNumExpr expr2b = cplex.linearNumExpr();	
						for (int i = 0; i < n; i++) {
							if (i != t) {
								expr2a.addTerm(1.0, f[t][i][s][t]);									
								expr2b.addTerm(1.0, f[i][t][s][t]);									
							}								
						}
						cplex.addEq(-1,cplex.sum(expr2a, cplex.negative(expr2b)));
//					}
//				}
//			}				

			// Flow conservation - source
//			for (int s = 0; s < d; s++) {
//				for (int t = 0; t < d; t++) {
//					if (s != t) {
						IloLinearNumExpr expr3a = cplex.linearNumExpr();
						IloLinearNumExpr expr3b = cplex.linearNumExpr();	
						for (int i = 0; i < n; i++) {
							if (i != s) {
								expr3a.addTerm(1.0, f[s][i][s][t]);									
								expr3b.addTerm(1.0, f[i][s][s][t]);									
							}								
						}
						cplex.addEq(1,cplex.sum(expr3a, cplex.negative(expr3b)));
//					}
//				}
//			}				
			
			
			// capacity
//			for (int s = 0; s < d; s++) {
//				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
								cplex.addLe(f[i][j][s][t], 1.0);
							}
						}
					}
//				}
//			}					
			
			// f sym
//			for (int s = 0; s < d; s++) {
//				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
								cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
							}
						}
					}
//				}
//			}					
			

		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}

	@Override
	public Double[][] getZVar() {
		return null;
	}

	@Override
	public Double[][][] getXVar() {
		return null;
	}

	@Override
	public String toString() {
		return "ST-FLOW";
	}
	
}
