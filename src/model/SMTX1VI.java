package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import graph.Graph;

public class SMTX1VI extends SMTX1 {

	public SMTX1VI(Graph graph, boolean isLP) {
		super(graph, isLP);
	}
	
	// Variables from X1
	 
	// Objective from X1
	
	public void createConstraints() {
		super.createConstraints();
		try {
			
			// NonDestNoLeaf (1i)
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					IloLinearNumExpr sumEnter = cplex.linearNumExpr();
					IloLinearNumExpr sumLeave = cplex.linearNumExpr();
					for (int i = 0; i < n; i++) {
						if (i != j) {
							sumEnter.addTerm(1.0, x[i][j][s]);
							sumLeave.addTerm(1.0, x[j][i][s]);
						}
					}
					cplex.addLe(sumEnter, sumLeave);
				}
			}			
			
			// y_sum=1 	(1j)
			for (int s = 0; s < d; s ++) {
				IloLinearNumExpr sumLeave = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (j != s) {
						sumLeave.addTerm(1.0, y[s][j][s]);						
					}
				}
				cplex.addEq(sumLeave, 1.0);
			}
			
			// x to nondest => y from there (1k)
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					IloLinearNumExpr sumLHS = cplex.linearNumExpr();
					IloLinearNumExpr sumRHS = cplex.linearNumExpr();
					for (int i = 0; i < n; i++) {
						if (i != j) {
							if (i != s) {
								sumLHS.addTerm(1.0, y[j][i][s]);
							}
							sumRHS.addTerm(1.0, x[i][j][s]);
						}
					}
					cplex.addGe(sumLHS, sumRHS);
				}
			}	
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}
}
