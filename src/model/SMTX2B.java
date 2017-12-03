package model;


import smt.App;
import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;
import graph.Node;

public class SMTX2B extends SMTX1VI {

	public SMTX2B(Graph graph, boolean isLP, boolean includeC) {
		super(graph, isLP, includeC);
	}
	
	protected IloNumVar[][][][] f;
	
	protected void initVars() {
		try {
			super.initVars();
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
	
	//objective from SMTX1VI
	
	public void createConstraints() {
		try{
			super.createConstraints();
			// Flow conservation - normal
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
			
			// Flow conservation - dest
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
			
			// capacity A
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
			
			// capacity B
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
			
			// f sym
//			for (int s = 0; s < d; s++) {
//				for (int t = 0; t < d; t++) {
//					for (int i = 0; i < n; i++) {
//						for (int j = 0; j < n; j++) {
//							if (j != i && s != t) {
//								cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
//							}
//						}
//					}
//				}
//			}					
				
//		sym h implication
			if (includeC) 
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if( i != j) {
					for (int t = 1; t < d; t++) {
						for (int s = 1; s < t; s++) {
							if (s != t) {
								for (int u = 0; u < t; u++) {
									if (u < s ) {
//										cplex.addGe(f[i][j][s][t], cplex.sum(f[i][j][u][t], cplex.negative(f[i][j][u][s])));  // this is just (2g)
										//remove the comment of the folloi
										cplex.addEq(cplex.sum(f[i][j][u][t], f[j][i][u][s], f[j][i][s][t]), cplex.sum(f[i][j][u][s], f[j][i][u][t], f[i][j][s][t]));
									}
								}																												
							}
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
