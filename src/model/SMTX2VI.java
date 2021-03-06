package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import graph.Graph;

public class SMTX2VI extends SMTX2 {

	public SMTX2VI(Graph graph, boolean isLP) {
		super(graph, isLP);
	}
	
	// vars and objective from SMTX2

	public void createConstraints() {
		try {
			super.createConstraints();
	
			// f imp y k  (2i)
			for (int j = 0; j < n; j++) {
				for (int s = 0; s < d; s++) {
					for (int t = 0; t < d; t++) {
						for (int k = 0; k < n; k++) {
							if (j != k && s != t) {
								IloLinearNumExpr sumLHS = cplex.linearNumExpr();
								IloLinearNumExpr sumRHS = cplex.linearNumExpr();
								for (int i = 0; i < n; i++) {
									if (i != j) { 
										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
											sumLHS.addTerm(1.0, f[j][i][s][t]);
											sumRHS.addTerm(1.0, y[j][i][s]);										
										}
									}
								}
								cplex.addLe(sumLHS, sumRHS);
							}
						}
					}
				}
			}	

			// vi4 (x imp sum f in ampl) (2h) -- seems implied
//			for (int s = 0; s < d; s++) {
//				for (int i = 0; i < n; i++) {
//					for (int j = 0; j < n; j++) {
//						IloLinearNumExpr expr1 = cplex.linearNumExpr();
//						for (int t = 0; t < d; t++) {
//							if (s != t) {
//								expr1.addTerm(1.0, f[i][j][s][t]);
//							}
//						}
////						if (j > i) {
//							cplex.addLe(x[i][j][s], expr1);
////						}
//					}
//				}
//			}	

			// vi10 (vi11 in ampl) (2g) -- implied by symmetry of hooks
//			for (int s = 0; s < d; s++) {
//				for (int t1 = 0; t1 < d; t1++) {
//					if (s != t1) {
//						for (int t2 = 0; t2 < d; t2++) {
//							if (s != t2 && t1 != t2) {
//								for (int i = 0; i < n; i++) {
//									for (int j = 0; j < n; j++) {
//										if (i != j) {
//											cplex.addLe(f[i][j][s][t2],cplex.sum(f[i][j][s][t1], f[i][j][t1][t2]));
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
	
	public String toString() {
    	return "X2VI(" + n + "," + d + ")";
	}

}
