package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;

public class SMTF2 extends SMTF1VI {

	public SMTF2(Graph graph, boolean isLP) {
		super(graph, isLP);
	}

	protected IloNumVar[][][][] h;  // y hook
	
	@Override
	protected void initVars() {
		try {
			super.initVars();  // initialize all variables from F1VI
			h = new IloNumVar[n][n][d][];	
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						h[i][j][k] = cplex.numVarArray(d,0,1);	
					}
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	
	
	// objective from F1
	
	@Override
	public void createConstraints() {
		try {
			super.createConstraints();
			
			// flow conservation of hooks at source v_0
			for (int k = 0; k < d; k++) { 
				for (int l = 0; l < d; l++) {
					if (k != l) {
						IloLinearNumExpr sumEnter = cplex.linearNumExpr();
						IloLinearNumExpr sumLeave = cplex.linearNumExpr();
						for (int j = 1; j < n; j++) { // must not be zero
							sumEnter.addTerm(1.0, h[j][0][k][l]);									
							sumLeave.addTerm(1.0, h[0][j][k][l]);
						}
						cplex.addGe(cplex.sum(sumEnter, cplex.negative(sumLeave)), -1.0);
					}
				}
			}		

			// flow conservation of hooks at i \in V \ {v_0}
			for (int i = 1; i < n; i++) { // must not be zero
				for (int k = 0; k < d; k++) {
					for (int l = 0; l < d; l++) {
						if (k != l) {
							IloLinearNumExpr sumEnter = cplex.linearNumExpr();
							IloLinearNumExpr sumLeave = cplex.linearNumExpr();
							for (int j = 0; j < n; j++) {
								if (i != j) {
									sumEnter.addTerm(1.0, h[j][i][k][l]);									
									sumLeave.addTerm(1.0, h[i][j][k][l]);
								}								
							}
							cplex.addGe(cplex.sum(sumEnter, cplex.negative(sumLeave)), 0.0);
						}
					}
				}				
			}

			// relation between hooks and fs (1)
			for (int k = 0; k < d; k++) { 
				for (int l = 0; l < d; l++) {
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(h[i][j][k][l], f3[i][j][k]);
								}
							}
						}
					}
				}
			}
			
			// relation between hooks and fs (2)
			for (int k = 0; k < d; k++) { 
				for (int l = 0; l < d; l++) {
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(h[i][j][k][l], f3[i][j][l]);
								}
							}
						}
					}
				}
			}			
			
			// h_x_stronger
			for (int k = 0; k < d; k++) {
				for (int l = 0; l < d; l++) {
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(cplex.sum(f3[i][j][k], f3[i][j][l], cplex.negative(h[i][j][k][l])), pz[i][j]);
								}
							}
						}
					}
				}
			}
			
			// h sym
			for (int k = 0; k < d; k++) {
				for (int l = 0; l < k; l++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i) {
								cplex.addLe(h[i][j][k][l], h[i][j][l][k]);
							}
						}
					}
				}
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
	
	public Double[][][][] getH() {
		try {
			Double[][][][] hval = new Double[n][n][d][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
							for (int l = 0; l < d; l++) {
								if (l != k) {
									hval[i][j][k][l] = cplex.getValue(h[i][j][k][l]);
								}
							}
						}
					}
				}
			}
			return hval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}		

	@Override
	public String toString() {
    	return "F2(" + n + "," + d + ")";
	}
}
