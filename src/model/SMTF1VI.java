package model;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import graph.Graph;

public class SMTF1VI extends SMTF1 {

	public SMTF1VI(Graph graph, boolean isLP, boolean lazy) {
		super(graph, isLP, lazy);
	}

	@Override
	public void createConstraints() {
		try {
			super.createConstraints();
			//no_flow_back - must be included in the basic model
			// steiner_flow_cons - must be included
			// obvious - must be included
//			// y_sum=1
			for (int s = 0; s < d; s++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (j != s) {
						expr1.addTerm(1.0, y[s][j][s]);						
					}
				}
				cplex.addEq(expr1, 1.0);
			}
//			 x to nondest => y from there 
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					IloLinearNumExpr expr2 = cplex.linearNumExpr();
					for (int i = 0; i < n; i++) {
						if (i != j) {
							expr1.addTerm(1.0, y[j][i][s]);
							expr2.addTerm(1.0, pz[i][j]);
							expr2.addTerm(-1.0, py[i][j][s]);
							expr2.addTerm(1.0, py[j][i][s]);
						}
					}
					cplex.addGe(expr1, expr2);
				}
			}	
			
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
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