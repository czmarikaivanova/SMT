package model;

import javax.swing.SpringLayout.Constraints;

import smt.Constants;

import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class MaxSTFlow extends ILPModel {

	private int s;
	private int t;
	private IloNumVar[][][][] f;
	
	public MaxSTFlow(Graph graph, Double[][][] xvar, Double[][][] yvar, int s, int t, IloCplex cplex, boolean includeFYConstr) {
		this.graph = graph;
		this.isLP = Constants.LP;
		try {
			cplex.clearModel();
			this.cplex = cplex;
//			cplex = new IloCplex();
			cplex.setOut(null);
		} catch (IloException e) {
			e.printStackTrace();
		}
		n = graph.getVertexCount();
		d = graph.getDstCount();
		this.s = s;
		this.t = t;
		createModel();	
		addCapacityConstraints(xvar);
		if (includeFYConstr) {
			addFimpYConstraints(yvar);
		}
//		try {
//			cplex.exportModel("stFlowModel.lp");
			cplex.setOut(null);
//		} catch (IloException e) {
//			e.printStackTrace();
//		}
	}
	
	@Override
	protected void initVars() {
		try {
			f = new IloNumVar[n][n][d][d];		
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					f[i][j][s][t] = cplex.numVar(0, 1);
					f[j][i][s][t] = cplex.numVar(0, 1);
					f[i][j][t][s] = cplex.numVar(0, 1);
					f[j][i][t][s] = cplex.numVar(0, 1);
//					for (int k = 0; k < d; k++) {
//						f[i][j][k] = cplex.numVarArray(d,0,1);	
//					}	
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}

	protected void createObjFunction() {
		try {
			IloLinearNumExpr obj1 = cplex.linearNumExpr();
			IloLinearNumExpr obj2 = cplex.linearNumExpr();	
			IloLinearNumExpr obj3 = cplex.linearNumExpr();
			IloLinearNumExpr obj4 = cplex.linearNumExpr();	
			for (int i = 0; i < n; i++) {
				if (i != t) {
					obj1.addTerm(1.0, f[t][i][s][t]);									
					obj2.addTerm(1.0, f[i][t][s][t]);									
				}	
				if (i != s) {
					obj3.addTerm(1.0, f[s][i][t][s]);									
					obj4.addTerm(1.0, f[i][s][t][s]);	
				}
			}
			cplex.addMinimize(cplex.sum(obj1, cplex.negative(obj2), obj3,cplex.negative(obj4)));
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createConstraints() {
		try {
			// Flow conservation - normal. s-t flow
			IloLinearNumExpr expr2a = cplex.linearNumExpr();
			IloLinearNumExpr expr2b = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {		
				if (i != t) {
					expr2a.addTerm(1.0, f[t][i][s][t]);									
					expr2b.addTerm(1.0, f[i][t][s][t]);									
					if (s != i) {
						IloLinearNumExpr expr1a = cplex.linearNumExpr();
						IloLinearNumExpr expr1b = cplex.linearNumExpr();	
						for (int j = 0; j < n; j++) {
							if (i != j && j != s) {
								expr1a.addTerm(1.0, f[i][j][s][t]);									
							}								
							if (i != j && j != t) {								
								expr1b.addTerm(1.0, f[j][i][s][t]);
							}								
						}						
						cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b)));			
					}
				}
			}
			
//			Flow conservation - normal. t-s flow 
			IloLinearNumExpr expr3a = cplex.linearNumExpr();
			IloLinearNumExpr expr3b = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {		
				if (i != s) {
					expr3a.addTerm(1.0, f[s][i][t][s]);									
					expr3b.addTerm(1.0, f[i][s][t][s]);									
					if (t != i) {
						IloLinearNumExpr expr1a = cplex.linearNumExpr();
						IloLinearNumExpr expr1b = cplex.linearNumExpr();	
						for (int j = 0; j < n; j++) {
							if (i != j && j != t) {
								expr1a.addTerm(1.0, f[i][j][t][s]);									
							}								
							if (i != j && j != s) {								
								expr1b.addTerm(1.0, f[j][i][t][s]);
							}								
						}						
						cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b)));		
					}
				}
			}			
//			cplex.addEq(-1,cplex.sum(expr2a, cplex.negative(expr2b))); 			// Flow conservation - dest
	
			
			// f sym
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
					}
				}
			}

		
		
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
	
	private void addCapacityConstraints(Double[][][] xvar) {
		try {
			for (int i = 0; i < n; i++) {
				for (int j = i+1; j < n; j++) {
					cplex.addLe(f[i][j][s][t], xvar[i][j][s]);
					cplex.addLe(f[j][i][s][t], xvar[j][i][s]);
					cplex.addLe(f[i][j][t][s], xvar[i][j][t]);
					cplex.addLe(f[j][i][t][s], xvar[j][i][t]);
				}
			}
			
		
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	private void addFimpYConstraints(Double[][][] yvar) {
		try {
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < n; k++) {
					if (j != k) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();
						IloLinearNumExpr expr2 = cplex.linearNumExpr();
						double ysum1 = 0;
						double ysum2 = 0;
						for (int i = 0; i < n; i++) {
							if (i != j) {
								if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
									if (yvar[j][i][s] > 0 ) { 
//										System.out.println("Y: " + yvar[j][i][s]);
//										System.out.println("Y: " + yvar[j][i][s]);
									}
									ysum1 += yvar[j][i][s];
									ysum2 += yvar[j][i][t];
									expr1.addTerm(1.0, f[j][i][s][t]);
									expr2.addTerm(1.0, f[j][i][t][s]);
								}
							}
						}
						cplex.addLe(expr1, ysum1); // s-t 
						cplex.addLe(expr2, ysum2); // t-s
					}
				}
			}	
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}
	
	@Override
	public Double[][] getZVar() {
		return null;
	}

	@Override
	public Double[][][] getXVar() {
		return null;
	}

	@Override
	public String toString() {
		return "ST-FLOW";
	}
	
}
