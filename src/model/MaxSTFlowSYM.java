package model;

import smt.Constants;
import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 * Maximum flow model.
 *
 */
public class MaxSTFlowSYM extends ILPModel {

	private int s;
	private int t;
	private IloNumVar[][][][] f;
	
	public MaxSTFlowSYM(Graph graph, Double[][][] xvar, Double[][][] yvar, int s, int t, IloCplex cplex) {
		this.graph = graph;
		this.n = graph.getVertexCount(); // # nodes
		this.d = graph.getDstCount();	  // # destinations
		this.s = s; 					  // source
		this.t = t;						  // target
		this.isLP = Constants.LP;		  // we will solve an LP relaxation of the max flow problem
		try {
			cplex.clearModel();
		} catch (IloException e) {
			e.printStackTrace();
		}
		this.cplex = cplex;
		cplex.setOut(null);  // do not output anything while solving max flow


		createModel();	
		addCapacityConstraints(xvar);
		addFimpYConstraints(yvar);
		cplex.setOut(null); 
	}
	
	@Override
	protected void initVars() {
		try {
			f = new IloNumVar[n][n][d][d];		
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					f[i][j][s][t] = cplex.numVar(0, 1);
					f[j][i][s][t] = cplex.numVar(0, 1);
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}

	
	/**
	 * Objective function for the max flow problem
	 * We want a flow of size as close as possible to 1
	 * The reason why we want a negative objective value (and so we minimize instead of maximize) 
	 * is that we want to emphasize that a smaller flow means bigger violation. 
	 * 
	 */
	protected void createObjFunction() {
		try {
			IloLinearNumExpr obj = cplex.linearNumExpr();	
			for (int i = 0; i < n; i++) {
				if (i != t) {
					obj.addTerm(1.0, f[i][t][s][t]);									
				}	
			}
			cplex.addMinimize(cplex.negative(obj));
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void createConstraints() {
		try {
			// Flow conservation - normal. s-t flow
			for (int i = 0; i < n; i++) {		
				if (i != t && i != s) {
					IloLinearNumExpr sumLeaveST = cplex.linearNumExpr();
					IloLinearNumExpr sumEnterST = cplex.linearNumExpr();	
					IloLinearNumExpr sumEnterTS = cplex.linearNumExpr();
					IloLinearNumExpr sumLeaveTS = cplex.linearNumExpr();	
					for (int j = 0; j < n; j++) {
						if (i != j) {
							if (j != s) {
								sumLeaveST.addTerm(1.0, f[i][j][s][t]);  // s-t
								sumLeaveTS.addTerm(1.0, f[i][j][s][t]);  // t-s
							}								
							if (j != t) {								
								sumEnterST.addTerm(1.0, f[j][i][s][t]);  // s-t
								sumEnterTS.addTerm(1.0, f[j][i][s][t]);	 // t-s
							}			
						}
					}						
					cplex.addEq(0,cplex.sum(sumLeaveST, cplex.negative(sumEnterST)));		
					cplex.addEq(0,cplex.sum(sumEnterTS, cplex.negative(sumLeaveTS)));		
				}
			}
			
//			Flow conservation - normal. t-s flow 
//			for (int i = 0; i < n; i++) {		
//				if (i != s && i != t) {
//					IloLinearNumExpr sumEnterTS = cplex.linearNumExpr();
//					IloLinearNumExpr sumLeaveTS = cplex.linearNumExpr();	
//					for (int j = 0; j < n; j++) {
//						if (i != j && j != t) {
//							sumEnterTS.addTerm(1.0, f[j][i][s][t]);									
//						}								
//						if (i != j && j != s) {								
//							sumLeaveTS.addTerm(1.0, f[i][j][s][t]);
//						}								
//					}						
//					cplex.addEq(0,cplex.sum(sumEnterTS, cplex.negative(sumLeaveTS)));		
//				}
//			}			
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
									ysum1 += yvar[j][i][s];
									ysum2 += yvar[j][i][t];
									expr1.addTerm(1.0, f[j][i][s][t]);
									expr2.addTerm(1.0, f[i][j][s][t]);
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
