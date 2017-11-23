package model;

import javax.swing.SpringLayout.Constraints;

import smt.Constants;

import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class STFlow extends ILPModel {

	private int s;
	private int t;
	private IloNumVar[][][][] f;
	private Double[][][] xvar;
	
	public STFlow(Graph graph, Double[][][] xvar, int s, int t, IloCplex cplex) {
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
		this.xvar = xvar;
		n = graph.getVertexCount();
		d = graph.getDstCount();
		this.s = s;
		this.t = t;
		createModel();	
		addCapacityConstraints(xvar);
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
						cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b)));			// Flow conservation - normal
					}
				}
			}
			
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
						cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b)));			// Flow conservation - normal
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
