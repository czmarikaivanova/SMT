package model;

import java.util.ArrayList;

import smt.App;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;
import graph.Node;

public class SMTX2VIB extends SMTX2B {

	public SMTX2VIB(Graph graph, boolean isLP, boolean includeC) {
		super(graph, isLP, includeC);
	}
	
	// vars and objective from SMTX2

	public void createConstraints() {
		try {
			super.createConstraints();
			// f imp y k  (2i)
//			if (includeC) {
//			System.out.println("Include C");
//			A
			for (int j = 0; j < n; j++) {
				for (int t = 0; t < d; t++) {
					for (int s = 0; s < t; s++) {
						for (int k = 0; k < n; k++) {
							if (j != k && s != t) {
								IloLinearNumExpr expr1 = cplex.linearNumExpr();
								IloLinearNumExpr expr2 = cplex.linearNumExpr();
								IloLinearNumExpr expr3 = cplex.linearNumExpr();
								IloLinearNumExpr expr4 = cplex.linearNumExpr();
								for (int i = 0; i < n; i++) {
									if (i != j) { 
										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
											expr1.addTerm(1.0, f[j][i][s][t]);
											expr2.addTerm(1.0, y[j][i][s]);										
											expr3.addTerm(1.0, f[i][j][s][t]);
											expr4.addTerm(1.0, y[j][i][t]);										

										}
									}
								}
								cplex.addLe(expr1, expr2);
								cplex.addLe(expr3, expr4);
							}
						}
					}
				}
			}	

//			B
//			for (int j = 0; j < n; j++) {
//				for (int t = 0; t < d; t++) {
//					for (int s = 0; s < t; s++) {
//						for (int k = 0; k < n; k++) {
//							if (j != k && s != t) {
//								IloLinearNumExpr expr1 = cplex.linearNumExpr();
//								IloLinearNumExpr expr2 = cplex.linearNumExpr();
//								for (int i = 0; i < n; i++) {
//									if (i != j) { 
//										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
//											expr1.addTerm(1.0, f[i][j][s][t]);
//											expr2.addTerm(1.0, y[j][i][t]);										
//										}
//									}
//								}
//								cplex.addLe(expr1, expr2);
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
	public Double[][] getTreeVar() {
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
