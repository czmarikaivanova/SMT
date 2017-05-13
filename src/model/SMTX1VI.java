package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloRange;
import graph.Graph;

public class SMTX1VI extends SMTX1 {

	public SMTX1VI(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
	}
	
	// objective function and vars from super
	
	public void createConstraints() {
		super.createConstraints();
		//		// YVar
		try {
			// y_sum=1
			for (int s = 0; s < d; s ++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (j != s) {
						expr1.addTerm(1.0, y[s][j][s]);						
					}
				}
				cplex.addEq(expr1, 1.0);
			}
			// x to nondest => y from there 
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					IloLinearNumExpr expr2 = cplex.linearNumExpr();
					for (int i = 0; i < n; i++) {
						if (i != j) {
							expr1.addTerm(1.0, y[j][i][s]);
							expr2.addTerm(1.0, x[i][j][s]);
						}
					}
					cplex.addGe(expr1, expr2);
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
			// steiner_flow_cons
			for (int i = d; i < n; i++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				IloLinearNumExpr expr2 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						expr1.addTerm(1.0, x[j][i][0]);
						expr2.addTerm(1.0, x[i][j][0]);
					}
				}
				cplex.addLe(cplex.sum(expr1, cplex.negative(expr2)), 0.0);
			}
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}
	
	public void addValidInequalities() {
		try {
			super.addValidInequalities();
			// y_sum=1
			for (int s = 0; s < d; s ++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (j != s) {
						expr1.addTerm(1.0, y[s][j][s]);						
					}
				}
				cplex.addEq(expr1, 1.0);
			}
			
			// x to nondest => y from there 
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					IloLinearNumExpr expr2 = cplex.linearNumExpr();
					for (int i = 0; i < n; i++) {
						if (i != j) {
							expr1.addTerm(1.0, y[j][i][s]);
							expr2.addTerm(1.0, x[i][j][s]);
						}
					}
					cplex.addGe(expr1, expr2);
				}
			}			
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
