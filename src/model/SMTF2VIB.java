package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import graph.Graph;

public class SMTF2VIB extends SMTF2B {

	public SMTF2VIB(Graph graph , boolean isLP) {
		super(graph, isLP);
	}
	
	// variables from F2B
	
	// objective from F1
	
	public void createConstraints() {
		try {
			super.createConstraints();  // all constraints from F2
			
			// f imp y in nondest  -- (2i) equivalent
			for (int j = 0; j < n; j++) {
				for (int t = 0; t < d; t++) {
					for (int s = 0; s < t; s++) {
						for (int k = 0; k < n; k++) {
							if (j != k) {
								IloLinearNumExpr sumLHS_ST = cplex.linearNumExpr();
								IloLinearNumExpr sumRHS_ST = cplex.linearNumExpr();
								IloLinearNumExpr sumLHS_TS = cplex.linearNumExpr();
								IloLinearNumExpr sumRHS_TS = cplex.linearNumExpr();
								for (int i = 0; i < n; i++) {
									if (i != j) { 
										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
											sumLHS_ST.addTerm(1.0, f3[i][j][s]);
											sumLHS_ST.addTerm(1.0, f3[j][i][t]);
											sumLHS_ST.addTerm(-1.0, h[i][j][s][t]);
											sumLHS_ST.addTerm(-1.0, h[j][i][s][t]);
											sumRHS_ST.addTerm(1.0, y[j][i][s]);	
											sumLHS_TS.addTerm(1.0, f3[j][i][s]);
											sumLHS_TS.addTerm(1.0, f3[i][j][t]);
											sumLHS_TS.addTerm(-1.0, h[i][j][s][t]);
											sumLHS_TS.addTerm(-1.0, h[j][i][s][t]);
											sumRHS_TS.addTerm(1.0, y[j][i][t]);										
										}
									}
								}
								cplex.addLe(sumLHS_ST, sumRHS_ST);
								cplex.addLe(sumLHS_TS, sumRHS_TS);
							}
						}
					}
				}
			}

//			// vi4 -- seems implied
//			for (int s = 0; s < d; s++) {
//				for (int i = 0; i < n; i++) {
//					for (int j = 0; j < n; j++) {
//						if (j != i) {
//							IloLinearNumExpr expr1 = cplex.linearNumExpr();
//							IloLinearNumExpr expr2 = cplex.linearNumExpr();
//							for (int t = 0; t < d; t++) {
//								if (s != t) {
//									expr1.addTerm(1.0, f3[i][j][t]);
//									expr1.addTerm(1.0, f3[j][i][s]);
//									expr1.addTerm(-1.0, h[i][j][s][t]);
//									expr1.addTerm(-1.0, h[j][i][s][t]);
//								}
//							}
//							expr2.addTerm(1.0, pz[i][j]);
//							expr2.addTerm(-1.0, f3[i][j][s]);
//							expr2.addTerm(1.0, f3[j][i][s]);
//							cplex.addLe(expr2, expr1);
//						}
//					}
//				}
//			}	
//			// vi10 -- seems implied
//			for (int s = 0; s < d; s++) {
//				for (int t1 = 0; t1 < d; t1++) {
//					if (s != t1) {
//						for (int t2 = 0; t2 < d; t2++) {
//							if (s != t2 && t1 != t2) {
//								for (int i = 0; i < n; i++) {
//									for (int j = 0; j < n; j++) {
//										if (i != j) { // update
//											cplex.addLe(
//													cplex.sum(
//															f3[i][j][t2],
//															f3[j][i][s], 
//															cplex.negative(h[i][j][s][t2]), 
//															cplex.negative(h[j][i][s][t2])), 
//													cplex.sum(
//															cplex.sum(
//																	f3[i][j][t1], 
//																	f3[j][i][s], 
//																	cplex.negative(h[i][j][s][t1]), 
//																	cplex.negative(h[j][i][s][t1])), 
//															cplex.sum(
//																	f3[i][j][t2], 
//																	f3[j][i][t1], 
//																	cplex.negative(h[i][j][t1][t2]), 
//																	cplex.negative(h[j][i][t1][t2]))));
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
}
