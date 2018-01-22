package model;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;
import graph.Graph;

public class SMTF1 extends ILPModel {

	public SMTF1(Graph graph , boolean isLP) {
		super(graph, isLP);
		if (isLP) {
			try {	// control how often should a new line be displayed
				cplex.setParam(IloCplex.Param.Simplex.Display, 0);
			} catch (IloException e) {
				e.printStackTrace();
			}
		}
	}

	protected IloNumVar[][] pz; 		// Polzin's x-variable
	protected IloNumVar[][][] f3;		// Polzin's y-variable, our f_{ij}^t
	protected IloNumVar[][][] y;		// Power variable y
	
	
	protected void initVars() {
		try {
			f3 = new IloNumVar[n][n][];
			y = new IloNumVar[n][n][];		
			pz = new IloNumVar[n][];	
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (isLP) {
						y[i][j] = cplex.numVarArray(d, 0, 1);
					}
					else {
						y[i][j] = cplex.boolVarArray(d);
					}					
					f3[i][j] = cplex.numVarArray(d,0,1);
				}					
			}
			
			for (int j = 0; j < n; j++) {
				if (isLP) {
					pz[j] = cplex.numVarArray(n, 0, 1);					
				}
				else {
					pz[j] = cplex.boolVarArray(n);					
				}
			}	
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	
	
	
	/**
	 * Objective function same as in X1
	 */
	protected void createObjFunction() {
		try {
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
	
	@Override
	public void createConstraints() {
		try {
//			 obvious constraints, necessary
			for (int i = 0; i < n; i++) {
				for (int t = 1; t < d; t++) {
					if (i != t) {
						cplex.addEq(f3[i][t][t], pz[i][t]);  // 3l
						cplex.addEq(f3[t][i][t], 0.0);  // 3k
					}
				}
			}	
			
			// No flow to the source
			for (int i = 0; i < n; i++) {
				cplex.addEq(pz[i][0], 0.0);
				for (int j = 0; j < n; j++) {
					if (i != j) {
						cplex.addEq(pz[i][0], 0.0);		// x
					}
				}
			}

			// flow conservation at a target t \in D
			for (int t = 1; t < d; t++) { // must not be zero
				IloLinearNumExpr sumEnter = cplex.linearNumExpr();
				IloLinearNumExpr sumLeave = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (t != j) {
						sumEnter.addTerm(1.0, f3[j][t][t]);									
						sumLeave.addTerm(1.0, f3[t][j][t]);
					}								
				}
				cplex.addEq(cplex.sum(sumEnter, cplex.negative(sumLeave)), 1.0);
			}		

			// flow conservation at a node i \in V \ {t, 0}
			for (int i = 1; i < n; i++) { // must not be zero
				for (int t = 0; t < d; t++) { // must not be zero OR CAN BE???
					if (i != t) {
						IloLinearNumExpr sumEnter = cplex.linearNumExpr();
						IloLinearNumExpr sumLeave = cplex.linearNumExpr();
						for (int j = 0; j < n; j++) {
							if (i != j) {
								sumEnter.addTerm(1.0, f3[j][i][t]);									
								sumLeave.addTerm(1.0, f3[i][j][t]);
							}								
						}
						cplex.addEq(cplex.sum(sumEnter, cplex.negative(sumLeave)), 0.0);
					}
				}				
			}
			
			// relation between f_{ij}^t and x_{ij}. In F2 is replaced by a stronger version that includes also (hook) f_{ij}^{st}
			for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						if (j != i) {
							cplex.addLe(f3[i][j][k], pz[i][j]);
						}
					}
				}
			}

			// yvar
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							IloLinearNumExpr sumY = cplex.linearNumExpr();
							for (int k = 0; k < n; k++) {
								if ((graph.getRequir(i,k) >= graph.getRequir(i,j)) && (i != k)) {
									sumY.addTerm(1.0, y[i][k][s]);
								}
							}
							cplex.addLe(cplex.sum(pz[i][j], f3[j][i][s], cplex.negative(f3[i][j][s])), sumY);								
						}			
					}
				}					
			}

			// yvar -- alt -- not working. WHY??
//			for (int i = 0; i < n; i++) {
//				for (int j = 0; j < n; j++) {
//					if (i != j) {
//						for (int s = 0; s < d; s++) {
//							IloLinearNumExpr expr7 = cplex.linearNumExpr();
//							for (int k = 0; k < n; k++) {
//								if ((graph.getRequir(j,k) >= graph.getRequir(i,j)) && (j != k)) {
//									expr7.addTerm(1.0, y[j][k][s]);
//								}
//							}
//							cplex.addLe(f3[i][j][s], expr7);								
//						}			
//					}
//				}					
//			}			
//
			// Sum of arcs entering a destination is <= 1
			// It is necessary, see instance in ../pictures
			for (int i = d; i < n; i++) {
				IloLinearNumExpr sumEnter = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						sumEnter.addTerm(1.0, pz[j][i]);
					}
				}
				cplex.addLe(sumEnter, 1.0);  
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
	
	@Override
	/**
	 * @return variable that induces the solution. In this case x_{ij} (here denoted as pz)
	 */
	public Double[][] getTreeVar() {
		try {
			Double[][] zval = new Double[pz.length][pz.length];
			for (int i = 0 ; i < pz.length; i++) {
				for (int j = 0; j < pz.length; j++) {
					if (i != j) {
						zval[i][j] = cplex.getValue(pz[i][j]);
						System.out.print(cplex.getValue(pz[i][j]) + " ");			
					}
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

	
	/**
	 * @return f_{ij}^t variables
	 */
	@Override
	public Double[][][] get3DVar() {
		try {
			Double[][][] xval = new Double[n][n][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
							xval[i][j][k] = cplex.getValue(f3[i][j][k]);
						}
					}
				}
			}
			return xval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	
	
	/**
	 * 
	 * @return power variable y_{ij}^s
	 */
	public Double[][][] getYVar() {
		try {
			Double[][][] yval = new Double[n][n][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
							yval[i][j][k] = cplex.getValue(y[i][j][k]);
						}
					}
				}
			}
			return yval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	

	@Override
	public String toString() {
    	return "F1-" + n + "-" + d + "";
	}
}
