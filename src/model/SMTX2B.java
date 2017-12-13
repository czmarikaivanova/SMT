package model;


import smt.App;
import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;
import graph.Node;

public class SMTX2B extends SMTX1VI {

	public SMTX2B(Graph graph, boolean isLP) {
		super(graph, isLP);
	}
	
	protected IloNumVar[][][][] f;
	
	protected void initVars() {
		try {
			super.initVars(); // variables from X1
			f = new IloNumVar[n][n][d][d];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						for (int l = 0; l < k; l++) {
							f[i][j][l][k] = cplex.numVar(0,1);
						}
					}	
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	
	
	//objective from X1
	
	
	public void createConstraints() {
		try{
			super.createConstraints();
			
			// Flow conservation constraints at i \in V \ {s,t}
			for (int t = 0; t < d; t++) {
				for (int s = 0; s < t; s++) {					
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
			
			// Flow conservation constraints at t \in D for a commodity (s,t)
			for (int t = 0; t < d; t++) {
				for (int s = 0; s < t; s++) {
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
			
			// capacity (1)
			for (int t = 0; t < d; t++) {
				for (int s = 0; s < t; s++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
								cplex.addLe(f[i][j][s][t], x[i][j][s]);
							}
						}
					}
				}
			}					
			
			// capacity (2)  
			for (int t = 0; t < d; t++) {
				for (int s = 0; s < t; s++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
								cplex.addLe(f[i][j][s][t], x[j][i][t]);
							}
						}
					}
				}
			}				

			// f-symmetry is implicit
			
	//		sym h implication
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if( i != j) {
						for (int t = 1; t < d; t++) {
							for (int s = 1; s < t; s++) {
								if (s != t) {
									for (int u = 0; u < t; u++) {
										if (u < s ) {
											cplex.addEq(cplex.sum(f[i][j][u][t], f[j][i][u][s], f[j][i][s][t]), cplex.sum(f[i][j][u][s], f[j][i][u][t], f[i][j][s][t]));
										}
									}																												
								}
							}
						}
					}
				}
			}
				
			// f_it^st = x_it^s  -- MAKES IT FASTER
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						if (i != t && s != t) {
							if (s < t) {
								cplex.addEq(f[i][t][s][t], x[i][t][s]);
							}
							else {
								cplex.addEq(f[t][i][t][s], x[i][t][s]);
							}
						}
					}
				}
			}	


		} catch (IloException e) {
			e.printStackTrace();
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
			return fval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	
	
	public String toString() {
    	return "X2B(" + n + "," + d + ")";
	}

}
