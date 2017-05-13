package model;

import smt.App;
import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;

public class SMTMultiFlowModel extends SteinerMultiFlowModel {

	public SMTMultiFlowModel(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
//		System.err.println(this.toString() + " CONSTRINT COUNT " + cplex.getNrows());
//		System.err.println(this.toString() + " VARIABLE COUNT " + cplex.getNcols());
	}
	
	protected IloNumVar[][][] y;
	
	@Override
	protected void initVars() {
		try {
			super.initVars();
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
	
	public void createConstraints() {
		super.createConstraints();
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
							cplex.addLe(x[i][j][s], expr7);
						}			
					}
				}					
			}
			
			if (App.stronger) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						if( i != j) {
							for (int s = 1; s < d; s++) {
								for (int t = 1; t < d; t++) {
									if (s != t) {
										
//											cplex.addEq(cplex.sum(f[i][j][0][t], f[j][i][0][s], f[i][j][t][s]), cplex.sum(f[i][j][0][s], f[j][i][0][t], f[i][j][s][t]));
											for (int u = 0; u < d; u++) {
												if (App.stronger2) {
													cplex.addGe(f[i][j][s][t], cplex.sum(f[i][j][u][t], cplex.negative(f[i][j][u][s])));
												}
												cplex.addEq(cplex.sum(f[i][j][u][t], f[j][i][u][s], f[i][j][t][s]), cplex.sum(f[i][j][u][s], f[j][i][u][t], f[i][j][s][t]));

											}																												
										
//										cplex.addGe(f[i][j][s][t], cplex.sum(f[j][i][0][s], cplex.negative(f[j][i][0][t])));
//										if (i < j) {
//											cplex.addLe(cplex.sum(f[i][j][0][s], f[j][i][0][t], f[i][j][s][t]),z[i][j]);
//											cplex.addLe(cplex.sum(f[j][i][0][s], f[i][j][0][t], f[j][i][s][t]),z[i][j]);
//										}

									}
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
							expr1.addTerm(1.0, x[j][i][0]);
							expr2.addTerm(1.0, x[i][j][0]);
						}
					}
					cplex.addLe(cplex.sum(expr1, cplex.negative(expr2)), 0.0);
				}

			// remove it is a VI down there for more j
//			for (int j = d; j < n; j ++) {
//				for (int s = 0; s < d; s++) {
//					for (int t = 0; t < d; t++) {
//						for (int k = 0; k < n; k++) {
//							if (j != k && s != t) {
//								IloLinearNumExpr expr1 = cplex.linearNumExpr();
//								IloLinearNumExpr expr2 = cplex.linearNumExpr();
//								for (int i = 0; i < n; i++) {
//									if (i != j) { 
//										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
//											expr1.addTerm(1.0, f[j][i][s][t]);
//										}
//										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
//											expr2.addTerm(1.0, y[j][i][s]);										
//										}
//									}
//								}
//								cplex.addLe(expr1, expr2);
//							}
//						}
//					}
//				}
//			}
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}
	
	public Double[][][][] getFVar() {
		try {
			Double[][][][] fval = new Double[z.length][z.length][z.length][z.length];
			for (int i = 0 ; i < f.length; i++) {
				for (int j = 0; j < f.length; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							for (int t = 0; t < d; t++) {
								if (t != s) {
									 fval[i][j][s][t] = cplex.getValue(f[i][j][s][t]);						
								}
							}
						}
					}
				}
			}
			System.out.println("Objective: " + cplex.getObjValue());
			return fval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	
	
	public String toString() {
    	return Constants.SMT_MULTI_FLOW_STRING + "(" + n + "," + d + ")";
	}
	
	public void addValidInequalities() {
		try {
			super.addValidInequalities();
			// y_sum=1
			for (int s = 0; s < d; s ++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (j != s) {
						expr1.addTerm(1.0, y[s][j][s]);						
					}
				}
				cplex.addEq(expr1, 1.0);
			}
			
			// x to nondest => y from there 
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
			// f imp y in nondest 
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
											expr1.addTerm(1.0, f[j][i][s][t]);
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
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		


}
