package model;

import java.util.ArrayList;

import smt.App;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;
import graph.Node;

public class SMTX2VIB extends SMTX2B {

	public SMTX2VIB(Graph graph, boolean isLP) {
		super(graph, isLP);
	}
	
	// vars and objective from SMTX2

	public void createConstraints() {
		try {
			super.createConstraints();
			
			// f imp y k  (2i)
			for (int j = 0; j < n; j++) {
				for (int t = 0; t < d; t++) {
					for (int s = 0; s < t; s++) {
						for (int k = 0; k < n; k++) {
							if (j != k && s != t) {
								IloLinearNumExpr sumLHS_ST = cplex.linearNumExpr();
								IloLinearNumExpr sumRHS_ST = cplex.linearNumExpr();
								IloLinearNumExpr sumLHS_TS = cplex.linearNumExpr();
								IloLinearNumExpr sumRHS_TS = cplex.linearNumExpr();
								for (int i = 0; i < n; i++) {
									if (i != j) { 
										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
											sumLHS_ST.addTerm(1.0, f[j][i][s][t]);
											sumRHS_ST.addTerm(1.0, y[j][i][s]);										
											sumLHS_TS.addTerm(1.0, f[i][j][s][t]);
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
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
	
	public String toString() {
    	return "Steiner_MULTI_FLOW ";
	}



}
