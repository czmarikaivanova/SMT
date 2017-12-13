package model;


import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;

public class SMTX2 extends SMTX1VI {

	public SMTX2(Graph graph, boolean isLP) {
		super(graph, isLP);
	}
	
	protected IloNumVar[][][][] f;


	// Initialize variables	
	protected void initVars() {
		try { 
			super.initVars(); // all vars from X1
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
	
	//objective from X1
	
	@Override
	public void createConstraints() {
		try{
			super.createConstraints();
			
			// Flow conservation constraints at i \in V \ {s,t}
			for (int s = 0; s < d; s++) {					
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {						
						if (t != i && s != i && s != t) {
							IloLinearNumExpr sumLeave = cplex.linearNumExpr();
							IloLinearNumExpr sumEnter = cplex.linearNumExpr();	
							for (int j = 0; j < n; j++) {
								if (i != j && j != s) {
									sumLeave.addTerm(1.0, f[i][j][s][t]);									
								}								
							}
							for (int j = 0; j < n; j++) {
								if (i != j && j != t) {								
									sumEnter.addTerm(1.0, f[j][i][s][t]);
								}								
							}						
							cplex.addEq(0,cplex.sum(sumLeave, cplex.negative(sumEnter)));
						}
					}
				}	
			}		
			
			// Flow conservation constraints at t \in D for a commodity (s,t)
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					if (s != t) {
						IloLinearNumExpr sumLeave = cplex.linearNumExpr();
						IloLinearNumExpr sumEnter = cplex.linearNumExpr();	
						for (int i = 0; i < n; i++) {
							if (i != t) {
								sumLeave.addTerm(1.0, f[t][i][s][t]);									
								sumEnter.addTerm(1.0, f[i][t][s][t]);									
							}								
						}
						cplex.addEq(-1,cplex.sum(sumLeave, cplex.negative(sumEnter)));
					}
				}
			}				
			
			// capacity
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
								cplex.addLe(f[i][j][s][t], x[i][j][s]);
							}
						}
					}
				}
			}					
			
			
			// f sym - stated explicitly
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
								cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
							}
						}
					}
				}
			}					
				
			//	sym h implication - can be inferred from Polzin's hook symmetry
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if( i != j) {
						for (int s = 1; s < d; s++) {
							for (int t = 1; t < d; t++) {
								if (s != t) {
									for (int u = 0; u < d; u++) {							
										cplex.addEq(cplex.sum(f[i][j][u][t], f[j][i][u][s], f[i][j][t][s]), cplex.sum(f[i][j][u][s], f[j][i][u][t], f[i][j][s][t]));
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
							cplex.addEq(f[i][t][s][t], x[i][t][s]);
						}
					}
				}
			}	
	
			// Slack equality 
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (i != j && s != t) {
								cplex.addEq(cplex.sum(x[i][j][s], cplex.negative(f[i][j][s][t])), cplex.sum(x[i][j][t], cplex.negative(f[i][j][t][s])));
							}
						}
					}
				}
			}
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @return four index variable f_{ij}^{st}
	 */
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
	
	@Override
	public String toString() {
    	return "X2(" + n + "," + d + ")";
	}
	

}
