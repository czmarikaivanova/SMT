package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import graph.Graph;

public class SMTX1 extends SteinerX {
	public SMTX1 (Graph graph, boolean isLP, boolean lazy) {
		super(graph, isLP, lazy);
	}
	
	protected IloNumVar[][][] y;		
	
	protected void initVars() {
		super.initVars();
		try{
			y = new IloNumVar[n][n][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {			
					if (isLP) {
						y[i][j] = cplex.numVarArray(d, 0, 1);
					}
					else {
						y[i][j] = cplex.boolVarArray(d);						
					}
	
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	protected void createObjFunction() {
		try {
			// objective function for SMT		
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							obj.addTerm(graph.getRequir(i,j), y[i][j][s]);
						}
					}
				}
			}
			cplex.addMinimize(obj);	
		} catch (IloException e) {
			e.printStackTrace();
		}			
	}	
	
	public void createConstraints() {
		super.createConstraints();  // Steiner tree
		//		// YVar
		try {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							IloLinearNumExpr expr7 = cplex.linearNumExpr();
							for (int k = 0; k < n; k++) {
								if ((graph.getRequir(i,k) >= graph.getRequir(i,j)) && (i != k)) {
									expr7.addTerm(1.0, y[i][k][s]);
								}
							}
							if (lazy) {
								cplex.addLazyConstraint((IloRange) cplex.le(x[i][j][s], expr7));								
							}
							else {
								cplex.addLe(x[i][j][s], expr7);								
							}
						}			
					}
				}					
			}
			
			// NonDestNoLEaf
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
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}
}