package model;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import graph.Graph;

public class SMTF2B extends SMTF1VI {

	public SMTF2B(Graph graph, boolean isLP, boolean includeC) {
		super(graph, isLP, includeC);
//		System.err.println(this.toString() + " CONSTRINT COUNT " + cplex.getNrows());
//		System.err.println(this.toString() + " VARIABLE COUNT " + cplex.getNcols());
	}

	protected IloNumVar[][][][] h;  // y hook
	
	
	protected void initVars() {
		try {
			super.initVars();
			h = new IloNumVar[n][n][d][d];	
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						for (int l = 0; l < k; l++) {
							h[i][j][l][k] = cplex.numVar(0,1);
						}
					}
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	
	// objecive from SMTF1
	
	@Override
	public void createConstraints() {
		try {
			super.createConstraints();
			// flow3
			for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
				for (int l = 0; l < k; l++) {
					if (k != l) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();
						IloLinearNumExpr expr2 = cplex.linearNumExpr();
						for (int j = 1; j < n; j++) { // must not be zero
							expr1.addTerm(1.0, h[j][0][l][k]);									
							expr2.addTerm(1.0, h[0][j][l][k]);
						}
						cplex.addGe(cplex.sum(expr1, cplex.negative(expr2)), -1.0);
					}
				}
			}		
			// flow4
			for (int i = 1; i < n; i++) { // must not be zero
				for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
					for (int l = 0; l < k; l++) { // must not be zero OR CAN BE???
						if (k != l) {
							IloLinearNumExpr expr1 = cplex.linearNumExpr();
							IloLinearNumExpr expr2 = cplex.linearNumExpr();
							for (int j = 0; j < n; j++) {
								if (i != j) {
									expr1.addTerm(1.0, h[j][i][l][k]);									
									expr2.addTerm(1.0, h[i][j][l][k]);
								}								
							}
							cplex.addGe(cplex.sum(expr1, cplex.negative(expr2)), 0.0);
						}
					}
				}				
			}
			// h_py1
			for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
				for (int l = 0; l < k; l++) { // must not be zero OR CAN BE???
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(h[i][j][l][k], py[i][j][l]);
								}
							}
						}
					}
				}
			}
			// h_py2
			for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
				for (int l = 0; l < k; l++) { // must not be zero OR CAN BE???
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(h[i][j][l][k], py[i][j][k]);
								}
							}
						}
					}
				}
			}			
			// h_x_stronger
			for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
				for (int l = 0; l < k; l++) { // must not be zero OR CAN BE???
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(cplex.sum(py[i][j][l], py[i][j][k], cplex.negative(h[i][j][l][k])), pz[i][j]);
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
	
	public Double[][][][] getH() {
		try {
			Double[][][][] hval = new Double[n][n][d][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
							for (int l = 0; l < k; l++) {
								if (l != k) {
									hval[i][j][l][k] = cplex.getValue(h[i][j][l][k]);
								}
							}
						}
					}
				}
			}
			return hval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}		
	
	public Double[][][] getPY() {
		try {
			Double[][][] xval = new Double[n][n][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
							xval[i][j][k] = cplex.getValue(py[i][j][k]);
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
	
	public Double[][] getPZ() {
		try {
			Double[][] zval = new Double[pz.length][pz.length];
			for (int i = 0 ; i < pz.length; i++) {
				for (int j = 0; j < pz.length; j++) {
					if (i != j) {
						zval[i][j] = cplex.getValue(pz[i][j]);
//						if (j == 2 && cplex.getValue(pz[i][j]) > 0) {
							System.out.print(i + " " + j + " " +" :" + cplex.getValue(pz[i][j]) + " --");	
//						}
					}
				}
				System.out.println();
			}
			return zval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}

	@Override
	public Double[][] getZVar() {
		return getPZ();
	}

	@Override
	public Double[][][] getXVar() {
		try {
			Double[][][] xval = new Double[n][n][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
//							if (j == 2 &&  cplex.getValue(py[i][j][k]) > 0 || k == 5 && cplex.getValue(py[i][j][k])> 0) {
							if (k == 2 && cplex.getValue(py[i][j][k]) > 0  ) {
								System.out.print(i + " " + j + " " + k +" :" + cplex.getValue(py[i][j][k]) + " --");	
							}
							xval[i][j][k] = cplex.getValue(py[i][j][k]);
						}
					}
				}
				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
			return xval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	
	
	public Double[][][] getYVar() {
		try {
			Double[][][] yval = new Double[n][n][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
//							if (j == 2 &&  cplex.getValue(py[i][j][k]) > 0 || k == 5 && cplex.getValue(py[i][j][k])> 0) {
//							if (k == 2 && cplex.getValue(py[i][j][k]) > 0  ) {
//								System.out.print(i + " " + j + " " + k +" :" + cplex.getValue(py[i][j][k]) + " --");	
//							}
							yval[i][j][k] = cplex.getValue(y[i][j][k]);
							System.out.print(i + " " + j + " " + k +" :" + cplex.getValue(y[i][j][k]) + " --");
						}
					}
				}
				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
			return yval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	

	@Override
	public String toString() {
    	return Constants.SMT_PF2_STRING + "(" + n + "," + d + ")";
	}
}
