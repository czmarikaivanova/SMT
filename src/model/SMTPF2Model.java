package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;

public class SMTPF2Model extends SMTModel {

	public SMTPF2Model(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
	}

	protected IloNumVar[][] pz;
	protected IloNumVar[][][] py;
	protected IloNumVar[][][][] h;  // y hook
	
	protected void initVars() {
		try {
			super.initVars();
			py = new IloNumVar[n][n][];
			h = new IloNumVar[n][n][d][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						h[i][j][k] = cplex.numVarArray(d,0,1);	
					}	
					py[i][j] = cplex.numVarArray(d,0,1);
				}					
			}
			pz = new IloNumVar[n][];				
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
	
	@Override
	public void createConstraints() {
		try {
			super.createConstraints();
			// flow1
			for (int t = 1; t < d; t++) { // must not be zero
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				IloLinearNumExpr expr2 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (t != j) {
						expr1.addTerm(1.0, py[j][t][t]);									
						expr2.addTerm(1.0, py[t][j][t]);
					}								
				}
				cplex.addEq(cplex.sum(expr1, cplex.negative(expr2)), 1.0);
			}		

			// flow2
			for (int i = 1; i < n; i++) { // must not be zero
				for (int t = 1; t < d; t++) { // must not be zero
					if (i != t) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();
						IloLinearNumExpr expr2 = cplex.linearNumExpr();
						for (int j = 0; j < n; j++) {
							if (i != j) {
								expr1.addTerm(1.0, py[j][i][t]);									
								expr2.addTerm(1.0, py[i][j][t]);
							}								
						}
						cplex.addEq(cplex.sum(expr1, cplex.negative(expr2)), 0.0);
					}
				}				
			}
			
			// flow3
			for (int k = 1; k < d; k++) { // must not be zero
				for (int l = 1; l < d; l++) {
					if (k != l) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();
						IloLinearNumExpr expr2 = cplex.linearNumExpr();
						for (int j = 1; j < n; j++) { // must not be zero
							expr1.addTerm(1.0, h[j][0][k][l]);									
							expr2.addTerm(1.0, h[0][j][k][l]);
						}
						cplex.addGe(cplex.sum(expr1, cplex.negative(expr2)), -1.0);
					}
				}
			}		

			// flow4
			for (int i = 1; i < n; i++) { // must not be zero
				for (int k = 1; k < d; k++) { // must not be zero
					for (int l = 1; l < d; l++) { // must not be zero
						if (k != l) {
							IloLinearNumExpr expr1 = cplex.linearNumExpr();
							IloLinearNumExpr expr2 = cplex.linearNumExpr();
							for (int j = 0; j < n; j++) {
								if (i != j) {
									expr1.addTerm(1.0, h[j][i][k][l]);									
									expr2.addTerm(1.0, h[i][j][k][l]);
								}								
							}
							cplex.addGe(cplex.sum(expr1, cplex.negative(expr2)), 0.0);
						}
					}
				}				
			}
			
			// h_py1
			for (int k = 1; k < d; k++) { // must not be zero
				for (int l = 1; l < d; l++) { // must not be zero
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(h[i][j][k][l], py[i][j][k]);
								}
							}
						}
					}
				}
			}
			
			// h_py2
			for (int k = 1; k < d; k++) { // must not be zero
				for (int l = 1; l < d; l++) { // must not be zero
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(h[i][j][k][l], py[i][j][l]);
								}
							}
						}
					}
				}
			}			
			
			// h_x_stronger
			for (int k = 1; k < d; k++) { // must not be zero
				for (int l = 1; l < d; l++) { // must not be zero
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(cplex.sum(py[i][j][k], py[i][j][l], cplex.negative(h[i][j][k][l])), pz[i][j]);
								}
							}
						}
					}
				}
			}
			
			// steiner_flow_cons
			for (int i = d; i < n; i++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				IloLinearNumExpr expr2 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						expr1.addTerm(1.0, pz[j][i]);
						expr2.addTerm(1.0, pz[i][j]);
					}
				}
				cplex.addLe(cplex.sum(expr1, cplex.negative(expr2)), 0.0);
			}
			
			// pf smt relation 1
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int t = 0; t < d; t++) {
							cplex.addLe(py[i][j][t], x[j][i][t]);
						}
					}
				}
			}
			
			// pf smt relation 2
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 1; k < d; k++) {
							for (int l = 1; l < d; l++) {
								if (k != l) {
									cplex.addLe(h[i][j][k][l], x[j][i][k]);
								}
							}
						}
					}
				}
			}
			// pf smt relation 2
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 1; k < d; k++) {
							for (int l = 1; l < d; l++) {
								if (k != l) {
									cplex.addLe(h[i][j][k][l], x[j][i][l]);
								}
							}
						}
					}
				}
			}			
			
			for (int i = 0; i < n ; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						cplex.addLe(cplex.sum(pz[i][j], pz[j][i]), 1.0);
					}
				}
			}
			
			
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
	
	public Double[][][] getPY() {
		try {
			Double[][][] xval = new Double[py.length][py.length][py.length];
			for (int i = 0 ; i < py.length; i++) {
				for (int j = 0; j < py.length; j++) {
					if (i != j) {
						for (int k = 0; k < py.length; k++) {
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
						System.out.print(cplex.getValue(pz[i][j]) + " ");	
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
}
