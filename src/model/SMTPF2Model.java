package model;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import graph.Graph;

public class SMTPF2Model extends SMTPF1Model {

	public SMTPF2Model(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
//		System.err.println(this.toString() + " CONSTRINT COUNT " + cplex.getNrows());
//		System.err.println(this.toString() + " VARIABLE COUNT " + cplex.getNcols());
	}

	protected IloNumVar[][][][] h;  // y hook
	
	
	protected void initVars() {
		try {
			super.initVars();
			h = new IloNumVar[n][n][d][];	
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						h[i][j][k] = cplex.numVarArray(d,0,1);	
					}
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	
	
	protected void createObjFunction() {
		try {
			// create model and solve it				
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
			super.createConstraints();

			// flow3
			for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
				for (int l = 0; l < d; l++) {
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
				for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
					for (int l = 0; l < d; l++) { // must not be zero OR CAN BE???
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
			for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
				for (int l = 0; l < d; l++) { // must not be zero OR CAN BE???
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
			for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
				for (int l = 0; l < d; l++) { // must not be zero OR CAN BE???
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
			for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
				for (int l = 0; l < d; l++) { // must not be zero OR CAN BE???
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
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
	
	public void addValidInequalities() {
		try {
			super.addValidInequalities();
//			 f imp y in nondest 
			for (int j = 0; j < n; j++) {
				for (int s = 0; s < d; s++) {
					for (int t = 0; t < d; t++) {
						for (int k = 0; k < n; k++) {
							if (j != k && s != t) {
								IloLinearNumExpr expr1 = cplex.linearNumExpr();
								IloLinearNumExpr expr2 = cplex.linearNumExpr();
								for (int i = 0; i < n; i++) {
									if (i != j) { 
										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
											expr1.addTerm(1.0, py[i][j][s]);
											expr1.addTerm(1.0, py[j][i][t]);
											expr1.addTerm(-1.0, h[i][j][s][t]);
											expr1.addTerm(-1.0, h[j][i][s][t]);
										}
										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
											expr2.addTerm(1.0, y[j][i][s]);										
										}
									}
								}
								cplex.addLe(expr1, expr2);
							}
						}
					}
				}
			}
//			// vi4
			for (int s = 0; s < d; s++) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						if (j != i) {
							IloLinearNumExpr expr1 = cplex.linearNumExpr();
							IloLinearNumExpr expr2 = cplex.linearNumExpr();
							for (int t = 0; t < d; t++) {
								if (s != t) {
									expr1.addTerm(1.0, py[i][j][t]);
									expr1.addTerm(1.0, py[j][i][s]);
									expr1.addTerm(-1.0, h[i][j][s][t]);
									expr1.addTerm(-1.0, h[j][i][s][t]);
								}
							}
							expr2.addTerm(1.0, pz[i][j]);
							expr2.addTerm(-1.0, py[i][j][s]);
							expr2.addTerm(1.0, py[j][i][s]);
							cplex.addLe(expr2, expr1);
						}
					}
				}
			}	
//			// vi10
			for (int s = 0; s < d; s++) {
				for (int t1 = 0; t1 < d; t1++) {
					if (s != t1) {
						for (int t2 = 0; t2 < d; t2++) {
							if (s != t2 && t1 != t2) {
								for (int i = 0; i < n; i++) {
									for (int j = 0; j < n; j++) {
										if (i != j) { // update
											cplex.addLe(
													cplex.sum(
															py[i][j][t2],
															py[j][i][s], 
															cplex.negative(h[i][j][s][t2]), 
															cplex.negative(h[j][i][s][t2])), 
													cplex.sum(
															cplex.sum(
																	py[i][j][t1], 
																	py[j][i][s], 
																	cplex.negative(h[i][j][s][t1]), 
																	cplex.negative(h[j][i][s][t1])), 
															cplex.sum(
																	py[i][j][t2], 
																	py[j][i][t1], 
																	cplex.negative(h[i][j][t1][t2]), 
																	cplex.negative(h[j][i][t1][t2]))));
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	
	public Double[][][][] getH() {
		try {
			Double[][][][] hval = new Double[n][n][d][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
							for (int l = 0; l < d; l++) {
								if (l != k) {
									hval[i][j][k][l] = cplex.getValue(h[i][j][k][l]);
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
