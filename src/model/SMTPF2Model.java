package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;

public class SMTPF2Model extends SMTModel {

	public SMTPF2Model(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
	}

	protected IloNumVar[][] zz;
	protected IloNumVar[][][] y;
	protected IloNumVar[][][][] h;
	
	protected void initVars() {
		try {
			n = graph.getVertexCount();
			d = graph.getDstCount();
			y = new IloNumVar[n][n][];
			h = new IloNumVar[n][n][d][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						h[i][j][k] = cplex.numVarArray(d,0,1);	
					}	
					y[i][j] = cplex.numVarArray(d,0,1);
				}					
			}
			zz = new IloNumVar[n][];				
			for (int j = 0; j < n; j++) {
				if (isLP) {
					zz[j] = cplex.numVarArray(n, 0, 1);					
				}
				else {
					zz[j] = cplex.boolVarArray(n);					
				}
			}	
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	
	
	@Override
	public void createConstraints() {
		try {
	
			// flow1
			for (int t = 1; t < d; t++) { // must not be zero
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				IloLinearNumExpr expr2 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (t != j) {
						expr1.addTerm(1.0, x[j][t][t]);									
						expr2.addTerm(1.0, x[t][j][t]);
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
								expr1.addTerm(1.0, x[j][i][t]);									
								expr2.addTerm(1.0, x[i][j][t]);
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
			
			// h_x1
			for (int k = 1; k < d; k++) { // must not be zero
				for (int l = 1; l < d; l++) { // must not be zero
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(h[i][j][k][l], x[i][j][k]);
								}
							}
						}
					}
				}
			}
			
			// h_x2
			for (int k = 1; k < d; k++) { // must not be zero
				for (int l = 1; l < d; l++) { // must not be zero
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(h[i][j][k][l], x[i][j][l]);
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
									cplex.addLe(cplex.sum(x[i][j][k], x[i][j][l], cplex.negative(h[i][j][k][l])), z[i][j]);
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
						expr1.addTerm(1.0, z[j][i]);
						expr2.addTerm(1.0, z[i][j]);
					}
				}
				cplex.addLe(cplex.sum(expr1, cplex.negative(expr2)), 0.0);
			}
			
			
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
		
	}

}
