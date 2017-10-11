package model;

import java.util.ArrayList;

import org.javatuples.Quartet;

import smt.App;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;
import graph.Node;

public class SMTX2VI extends SMTX2 {

	public SMTX2VI(Graph graph, boolean isLP, boolean lazy) {
		super(graph, isLP, lazy);
	}
	
	// vars and objective from SMTX2

	public void createConstraints() {
		try {
			super.createConstraints();
			// f imp y k 
//			if (lazy) {
//			System.out.println("Lazy");
//			for (int j = 0; j < n; j++) {
//				for (int s = 0; s < d; s++) {
//					for (int t = 0; t < d; t++) {
//						for (int k = 0; k < n; k++) {
//							if (j != k && s != t) {
//								IloLinearNumExpr expr1 = cplex.linearNumExpr();
//								IloLinearNumExpr expr2 = cplex.linearNumExpr();
//								for (int i = 0; i < n; i++) {
//									if (i != j) { 
//										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
//											expr1.addTerm(1.0, f[j][i][s][t]);
//										}
//										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
//											expr2.addTerm(1.0, y[j][i][s]);										
//										}
//									}
//								}
//								cplex.addLe(expr1, expr2);
//							}
//						}
//					}
//				}
//			}	
//			}
//			else {
//				System.out.println("NO LAZY");
//			}	
			// vi4 (x imp sum f in ampl) 
//			if (lazy) {
//			System.out.println("Lazy");
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
//			}
//			else {
//				System.out.println("NO LAZY");
//			}	
//			// vi10 (vi11 in ampl)
//			if (lazy) {
//			System.out.println("Lazy");
			for (int s = 0; s < d; s++) {
				for (int t1 = 0; t1 < d; t1++) {
					if (s != t1) {
						for (int t2 = 0; t2 < d; t2++) {
							if (s != t2 && t1 != t2) {
								for (int i = 0; i < n; i++) {
									for (int j = 0; j < n; j++) {
										if (i != j) {
											cplex.addLe(f[i][j][s][t2],cplex.sum(f[i][j][s][t1], f[i][j][t1][t2]));
										}
									}
								}
							}
						}
					}
				}
			}
//			}
//			else {
//				System.out.println("NO LAZY");
//			}
//			
//			--------------------------
//			for (int i = 0; i < n; i++) {
//				for (int j = 0; j < n; j++) {
//					if (i != j) {
//						for (int s = 0; s < d; s++) {
//							for (int t = 0; t < d; t++) {
//								if (s != t) {
//									cplex.addEq(cplex.sum(f[i][j][0][t], f[j][i][0][s], f[i][j][t][s]), cplex.sum(f[i][j][0][s], f[j][i][0][t], f[i][j][s][t]));
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
    	return "Steiner_MULTI_FLOW ";
	}


	@Override
	public Double[][] getZVar() {
		try {
			Double[][] zval = new Double[z.length][z.length];
			for (int i = 0 ; i < z.length; i++) {
				for (int j = i+1; j < z.length; j++) {
					System.out.print(cplex.getValue(z[i][j]) + " ");						
					zval[i][j] = cplex.getValue(z[i][j]);						
				}
				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
			return zval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}


		


}
