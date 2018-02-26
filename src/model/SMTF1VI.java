package model;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import graph.Graph;

public class SMTF1VI extends SMTF1 {

	public SMTF1VI(Graph graph, boolean isLP) {
		super(graph, isLP);
	}

	// variables from F1
	
	// objective from F1
	
	
	@Override
	public void createConstraints() {
		try {
			super.createConstraints();

			// steiner_flow_cons - must be included. WHY???
//			if (Constants.INCLUDE)
			for (int i = d; i < n; i++) {
				IloLinearNumExpr sumEnter = cplex.linearNumExpr();
				IloLinearNumExpr sumLeave = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						sumEnter.addTerm(1.0, pz[j][i]);
						sumLeave.addTerm(1.0, pz[i][j]);
					}
				}
				cplex.addLe(cplex.sum(sumEnter, cplex.negative(sumLeave)), 0.0);
			}
			
//			// y_sum=1 - there is one arc (s,j) transmitting message from s with power p_{sj} 

			for (int s = 0; s < d; s++) {
				IloLinearNumExpr sumLeave = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (j != s) {
						sumLeave.addTerm(1.0, y[s][j][s]);						
					}
				}
				cplex.addEq(sumLeave, 1.0);
			}
//			 x to non-destination => y from there 

			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					IloLinearNumExpr sumLHS = cplex.linearNumExpr();
					IloLinearNumExpr sumRHS = cplex.linearNumExpr();
					for (int i = 0; i < n; i++) {
						if (i != j) {
							if (i != s) {
								sumLHS.addTerm(1.0, y[j][i][s]);
							}
							sumRHS.addTerm(1.0, pz[i][j]);
							sumRHS.addTerm(-1.0, f3[i][j][s]);
							sumRHS.addTerm(1.0, f3[j][i][s]);
						}
					}
					cplex.addGe(sumLHS, sumRHS);
				}
			}
			
			// f imp y  --- not working
//			for (int j = 0; j < n; j++) {
//				for (int t = 0; t < d; t++) {
//					for (int k = 0; k < n; k++) {
//						if (j != k) {
//							IloLinearNumExpr expr1 = cplex.linearNumExpr();
//							IloLinearNumExpr expr2 = cplex.linearNumExpr();
//							for (int i = 0; i < n; i++) {
//								if (j != i && graph.getRequir(i, j) >= graph.getRequir(j, k)) {
//									expr1.addTerm(1.0, f3[j][i][t]);
//									expr2.addTerm(1.0, y[i][j][t]);
//								}
//							}
//							cplex.addLe(expr1, expr2);
//						}
//					}
//				}
//			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
	
	@Override
	public String toString() {
    	return "F1VI(" + n + "," + d + ")";
	}
}
