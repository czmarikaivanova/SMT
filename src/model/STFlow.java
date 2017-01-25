package model;

import javax.swing.SpringLayout.Constraints;

import smt.Constants;

import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;

public class STFlow extends ILPModel {

	private int s;
	private int t;
	private double[][] cap;
	private IloNumVar[][] f_st;

	public STFlow(Graph graph, Double[][][] xvar) {
		super(graph, false, Constants.LP, false);
		cap = new double[n][n];
		for (int i = 0; i < n; i ++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
//					this.cap[i][j] = xvar[i][j][s];
					this.cap[i][j] = 1.0;
				}
			}
		}
	}
	
	@Override
	protected void initVars() {
		try {
			f_st = new IloNumVar[n][];
			for (int i = 0; i < n; i++) {
				if (isLP) {
					f_st[i] = cplex.numVarArray(n, 0, 1);
				}
				else {
					f_st[i] = cplex.boolVarArray(n);						
				}
			}
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}

	protected void createObjFunction() {
		try {
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				if (i != s) {
					obj.addTerm(1.0, f_st[s][i]);
				}
			}
			cplex.addMaximize(obj);				
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createConstraints() {
		try {
			// Flow conservation - normal
			for (int i = 0; i < n; i++) {
				if (t != i && s != i) {
					IloLinearNumExpr expr1a = cplex.linearNumExpr();
					IloLinearNumExpr expr1b = cplex.linearNumExpr();	
					for (int j = 0; j < n; j++) {
						if (i != j && j != s) {
							expr1a.addTerm(1.0, f_st[i][j]);									
						}								
					}
					for (int j = 0; j < n; j++) {
						if (i != j && j != t) {								
							expr1b.addTerm(1.0, f_st[j][i]);
						}								
					}						
					cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b)));
				}
			}
			
			// Flow conservation - dest
			IloLinearNumExpr expr2a = cplex.linearNumExpr();
			IloLinearNumExpr expr2b = cplex.linearNumExpr();	
			for (int i = 0; i < n; i++) {
				if (i != t) {
					expr2a.addTerm(1.0, f_st[t][i]);									
					expr2b.addTerm(1.0, f_st[i][t]);									
				}								
			}
			cplex.addEq(-1,cplex.sum(expr2a, cplex.negative(expr2b)));

			// Flow conservation - source
			IloLinearNumExpr expr3a = cplex.linearNumExpr();
			IloLinearNumExpr expr3b = cplex.linearNumExpr();	
			for (int i = 0; i < n; i++) {
				if (i != s) {
					expr3a.addTerm(1.0, f_st[s][i]);									
					expr3b.addTerm(1.0, f_st[i][s]);									
				}								
			}
			cplex.addEq(1,cplex.sum(expr3a, cplex.negative(expr3b)));
			
			// capacity
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (j != i && s != t) {
//						cplex.addLe(f_st[i][j], cap[i][j]);
						cplex.addLe(f_st[i][j], 1.0);
					}
				}
			}
			
			// f sym
//			for (int i = 0; i < n; i++) {
//				for (int j = 0; j < n; j++) {
//					if (j != i && s != t) {
//						cplex.addEq(f_st[i][j], f_st[j][i]);
//					}
//				}
//			}			

		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
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
