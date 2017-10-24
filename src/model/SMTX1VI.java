package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import graph.Graph;

public class SMTX1VI extends SMTX1 {

	public SMTX1VI(Graph graph, boolean isLP, boolean includeC) {
		super(graph, isLP, includeC);
	}
	
	// objective function and vars from super
	
	public void createConstraints() {
		super.createConstraints();
		try {
			// y_sum=1 	(1j)
//			if (includeC) {
//			System.out.println("Include C");
				for (int s = 0; s < d; s ++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					for (int j = 0; j < n; j++) {
						if (j != s) {
							expr1.addTerm(1.0, y[s][j][s]);						
						}
					}
					cplex.addEq(expr1, 1.0);
				}
//				}
//				else {
//					System.out.println("Exclude C");
//				}
			
//			 x to nondest => y from there (1k)
//			if (includeC) {
//				System.out.println("Include C");
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
//			}
//			else {
//				System.out.println("Exclude C");
//			}
		
			// NonDestNoLeaf (1i)
//			if (includeC) {
//				System.out.println("include C");
				for (int j = d; j < n; j++) {
					for (int s = 0; s < d; s++) {
						IloLinearNumExpr expr4 = cplex.linearNumExpr();
						IloLinearNumExpr expr5 = cplex.linearNumExpr();
						for (int i = 0; i < n; i++) {
							if (i != j) {
								expr4.addTerm(1.0, x[i][j][s]);
								expr5.addTerm(1.0, x[j][i][s]);
							}
						}
						cplex.addLe(expr4, expr5);
					}
				}
//			}
//			else {
//				System.out.println("Exclude C");
//			}
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}
}
