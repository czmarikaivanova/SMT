package model;

import smt.App;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloRange;
import graph.Graph;

public class SMTX1VI extends SMTX1 {

	public SMTX1VI(Graph graph, boolean isLP, boolean lazy) {
		super(graph, isLP, lazy);
	}
	
	// objective function and vars from super
	
	public void createConstraints() {
		super.createConstraints();
		//		// YVar
		try {
			// y_sum=1 	//$$$$$$ COMMENT ONLY FOR A WHILE. REMOVE!!!
//			if (lazy) {
//			System.out.println("Lazy");
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
//					System.out.println("NO LAZY");
//				}
			
//			 x to nondest => y from there 
//			if (lazy) {
//				System.out.println("Lazy");
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
//				System.out.println("NO LAZY");
//			}
		
			// NonDestNoLeaf
//			if (lazy) {
//				System.out.println("LAZY YES");
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
//				System.out.println("LAZY NO");
//			}
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}
}
