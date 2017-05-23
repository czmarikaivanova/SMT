package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import graph.Graph;

public class SMTF2VI extends SMTF2 {

	public SMTF2VI(Graph graph , boolean isLP, boolean lazy) {
		super(graph, isLP, lazy);
	}
	public void createConstraints() {
		try {
			super.createConstraints();
//			 f imp y in nondest 
			for (int j = 0; j < n; j++) {
				for (int s = 0; s < d; s++) {
					for (int t = 0; t < d; t++) {
						for (int k = 0; k < n; k++) {
							if (j != k && s != t) {
								IloLinearNumExpr expr1 = cplex.linearNumExpr();
								IloLinearNumExpr expr2 = cplex.linearNumExpr();
								for (int i = 0; i < n; i++) {
									if (i != j) { 
										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
											expr1.addTerm(1.0, py[i][j][s]);
											expr1.addTerm(1.0, py[j][i][t]);
											expr1.addTerm(-1.0, h[i][j][s][t]);
											expr1.addTerm(-1.0, h[j][i][s][t]);
										}
										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
											expr2.addTerm(1.0, y[j][i][s]);										
										}
									}
								}
								cplex.addLe(expr1, expr2);
							}
						}
					}
				}
			}
//			// vi4
			for (int s = 0; s < d; s++) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						if (j != i) {
							IloLinearNumExpr expr1 = cplex.linearNumExpr();
							IloLinearNumExpr expr2 = cplex.linearNumExpr();
							for (int t = 0; t < d; t++) {
								if (s != t) {
									expr1.addTerm(1.0, py[i][j][t]);
									expr1.addTerm(1.0, py[j][i][s]);
									expr1.addTerm(-1.0, h[i][j][s][t]);
									expr1.addTerm(-1.0, h[j][i][s][t]);
								}
							}
							expr2.addTerm(1.0, pz[i][j]);
							expr2.addTerm(-1.0, py[i][j][s]);
							expr2.addTerm(1.0, py[j][i][s]);
							cplex.addLe(expr2, expr1);
						}
					}
				}
			}	
//			// vi10
			for (int s = 0; s < d; s++) {
				for (int t1 = 0; t1 < d; t1++) {
					if (s != t1) {
						for (int t2 = 0; t2 < d; t2++) {
							if (s != t2 && t1 != t2) {
								for (int i = 0; i < n; i++) {
									for (int j = 0; j < n; j++) {
										if (i != j) { // update
											cplex.addLe(
													cplex.sum(
															py[i][j][t2],
															py[j][i][s], 
															cplex.negative(h[i][j][s][t2]), 
															cplex.negative(h[j][i][s][t2])), 
													cplex.sum(
															cplex.sum(
																	py[i][j][t1], 
																	py[j][i][s], 
																	cplex.negative(h[i][j][s][t1]), 
																	cplex.negative(h[j][i][s][t1])), 
															cplex.sum(
																	py[i][j][t2], 
																	py[j][i][t1], 
																	cplex.negative(h[i][j][t1][t2]), 
																	cplex.negative(h[j][i][t1][t2]))));
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
}
