package model;

import smt.Constants;
import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 * Extended maximum flow model. It finds a maximum flow from s to t and from t
 * to s (the should be equal), while enforcing capacities x_{ij}^s and x_{ij}^t
 * calculated from X1VI. Furthermore it enforces (2i) for both directions. The
 * two directions are necessary, because if we consider only say s->t, then we
 * cannot say whether the symmetry can also be satisfied
 * 
 */
public class MaxFlow extends ILPModel {

	private int s;
	private int t;
	private IloNumVar[][] f; // f_{ij} = flow carried by arc (i,j) \in A

	public MaxFlow(Graph graph, Double[][][] xvar, Double[][][] yvar, int s, int t, IloCplex cplex) {
		System.err.println("NORMAL FLOW"); // delete this
		this.graph = graph;
		this.n = graph.getVertexCount(); // # nodes
		this.d = graph.getDstCount(); // # destinations
		this.s = s; // source
		this.t = t; // target
		this.isLP = Constants.LP; // we will solve an LP relaxation of the max flow problem
		try {
			cplex.clearModel();
		} catch (IloException e) {
			e.printStackTrace();
		}
		this.cplex = cplex;
		cplex.setOut(null); // do not output anything while solving max flow
		createModel();
		addCapacityConstraints(xvar);
		addFimpYConstraints(yvar);
		cplex.setOut(null);
	}

	protected void initVars() {
		try {
			f = new IloNumVar[n][n];
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					f[i][j] = cplex.numVar(0, 1);
					f[j][i] = cplex.numVar(0, 1);
				}
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Objective function for the max flow problem We want a flow of size as
	 * close as possible to 1 The reason why we want a negative objective value
	 * (and so we minimize instead of maximize) is that we want to emphasize
	 * that a smaller flow means bigger violation.
	 * 
	 */
	protected void createObjFunction() {
		try {
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				if (i != t) {
					obj.addTerm(1.0, f[i][t]);
				}
			}
			cplex.addMinimize(cplex.negative(obj));
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 *  This method adds only flow conservation constraints for i \in V \ {s,t}. 
	 *  Capacity and  sum f implies sum y (2i) are added  separately, because
	 *  they need an input parameter x and y
	 */
	@Override
	protected void createConstraints() {
		try {
			// Flow conservation - i \in V \ {s,t}
			for (int i = 0; i < n; i++) {
				if (i != t && i != s) {
					IloLinearNumExpr sumLeaveST = cplex.linearNumExpr();
					IloLinearNumExpr sumEnterST = cplex.linearNumExpr();
					IloLinearNumExpr sumEnterTS = cplex.linearNumExpr();
					IloLinearNumExpr sumLeaveTS = cplex.linearNumExpr();
					for (int j = 0; j < n; j++) {
						if (i != j) {
							if (j != s) {
								sumLeaveST.addTerm(1.0, f[i][j]); // s-t
								sumLeaveTS.addTerm(1.0, f[i][j]); // t-s
							}
							if (j != t) {
								sumEnterST.addTerm(1.0, f[j][i]); // s-t
								sumEnterTS.addTerm(1.0, f[j][i]); // t-s
							}
						}
					}
					cplex.addEq(0,
							cplex.sum(sumLeaveST, cplex.negative(sumEnterST)));
					cplex.addEq(0,
							cplex.sum(sumEnterTS, cplex.negative(sumLeaveTS)));
				}
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}

	/**
	 * (2d)
	 * 
	 * @param xvar - capacities
	 */
	private void addCapacityConstraints(Double[][][] xvar) {
		try {
			for (int i = 0; i < n; i++) {
				for (int j = i + 1; j < n; j++) {
					cplex.addLe(f[i][j], xvar[i][j][s]);
					cplex.addLe(f[j][i], xvar[j][i][s]);
				}
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * (2i)
	 * 
	 * @param yvar
	 */
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
								if (graph.getRequir(j, i) >= graph.getRequir(j,
										k)) {
									ysum1 += yvar[j][i][s];
									ysum2 += yvar[j][i][t];
									expr1.addTerm(1.0, f[j][i]);
									expr2.addTerm(1.0, f[i][j]);
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
	public String toString() {
		return "MaxFlow";
	}

}
