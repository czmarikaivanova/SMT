package model;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import graph.Graph;

public class SMTF2B extends SMTF1VI {

	public SMTF2B(Graph graph, boolean isLP) {
		super(graph, isLP);
	}

	protected IloNumVar[][][][] h;  // y hook
	
	protected void initVars() {
		try {
			super.initVars();  // all vars from F1
			h = new IloNumVar[n][n][d][d];	
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						for (int l = 0; l < k; l++) {
							h[i][j][l][k] = cplex.numVar(0,1);
						}
					}
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	
	// objecive from F1
	
	@Override
	public void createConstraints() {
		try {
			super.createConstraints(); // all constraints from F1VI
		
			// flow conservation of hooks at source v_0
			for (int k = 0; k < d; k++) { 
				for (int l = 0; l < k; l++) {
					if (k != l) {
						IloLinearNumExpr sumEnter = cplex.linearNumExpr();
						IloLinearNumExpr sumLeave = cplex.linearNumExpr();
						for (int j = 1; j < n; j++) { // must not be zero
							sumEnter.addTerm(1.0, h[j][0][l][k]);									
							sumLeave.addTerm(1.0, h[0][j][l][k]);
						}
						cplex.addGe(cplex.sum(sumEnter, cplex.negative(sumLeave)), -1.0);
					}
				}
			}		
			
			// flow conservation of hooks at i \in V \ {v_0}
			for (int i = 1; i < n; i++) { // must not be zero
				for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
					for (int l = 0; l < k; l++) { // must not be zero OR CAN BE???
						if (k != l) {
							IloLinearNumExpr sumEnter = cplex.linearNumExpr();
							IloLinearNumExpr sumLeave = cplex.linearNumExpr();
							for (int j = 0; j < n; j++) {
								if (i != j) {
									sumEnter.addTerm(1.0, h[j][i][l][k]);									
									sumLeave.addTerm(1.0, h[i][j][l][k]);
								}								
							}
							cplex.addGe(cplex.sum(sumEnter, cplex.negative(sumLeave)), 0.0);
						}
					}
				}				
			}
			
			// relation between hooks and fs (1)
			for (int k = 0; k < d; k++) { 
				for (int l = 0; l < k; l++) { 
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i) {
								cplex.addLe(h[i][j][l][k], f3[i][j][l]);
							}
						}
					}
				}
			}

			// relation between hooks and fs (2)
			for (int k = 0; k < d; k++) { 
				for (int l = 0; l < k; l++) { 
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i) {
								cplex.addLe(h[i][j][l][k], f3[i][j][k]);
							}
						}
					}
				}
			}			
			
			// h_x_stronger
			for (int k = 0; k < d; k++) {
				for (int l = 0; l < k; l++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i) {
								cplex.addLe(cplex.sum(f3[i][j][l], f3[i][j][k], cplex.negative(h[i][j][l][k])), pz[i][j]);
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
							for (int l = 0; l < k; l++) {
								if (l != k) {
									hval[i][j][l][k] = cplex.getValue(h[i][j][l][k]);
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
    	return "F2B(" + n + "," + d + ")";
	}
}
