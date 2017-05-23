package model;

import org.javatuples.Quartet;

import smt.Constants;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;
import graph.Node;

public class SMTFlowModel extends SMTX1 {

	public SMTFlowModel(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, isLP, lazy);
	}
	
	protected IloNumVar[][][] f;
	
	protected void initVars() {
		try {
			super.initVars();
			f = new IloNumVar[n][n][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (isLP) {
						f[i][j] = cplex.numVarArray(n, 0, 1);
					}
					else {
						f[i][j] = cplex.boolVarArray(n);						
					}
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	public void createConstraints() {
		try {
			
			super.createConstraints();
				
			// -------------------------------------- constraints							
		
	
			// Flow-balance-normal
			for (int k = 1; k < d; k++) { // from node one !					
				for (int i = 1; i < n; i++) {
					if (i != k) {
						IloLinearNumExpr expr1  = cplex.linearNumExpr();
						IloLinearNumExpr expr1b = cplex.linearNumExpr();
						for (int j = 0; j < n; j++) { 
							if (i != j && j != 0) {
								expr1.addTerm(1.0, f[i][j][k]);								
							}			
							if (i != j) {
								expr1b.addTerm(1.0, f[j][i][k]);
							}
						}
						cplex.addEq(0,cplex.sum(expr1, cplex.negative(expr1b)));
					}
				}	
			}		
			
						
			// Flow-balance-dest
			for (int k = 0; k < d; k++) { 					
				IloLinearNumExpr expr2a  = cplex.linearNumExpr();
				IloLinearNumExpr expr2b = cplex.linearNumExpr();
				for (int i = 0; i < n; i++) { 
					if (i != k) {
						expr2a.addTerm(1.0, f[k][i][k]);								
						expr2b.addTerm(1.0, f[i][k][k]);
					}
				}
				cplex.addEq(-1, cplex.sum(expr2a, cplex.negative(expr2b)));
			}

			// Flow-balance-source
			for (int k = 1; k < d; k++) { // from one 					
				IloLinearNumExpr expr3a  = cplex.linearNumExpr();
				IloLinearNumExpr expr3b = cplex.linearNumExpr();
				for (int j = 1; j < n; j++) { // also from one 
					expr3a.addTerm(1.0, f[0][j][k]);								
					expr3b.addTerm(1.0, f[j][0][k]);
				}
				cplex.addEq(1, cplex.sum(expr3a, cplex.negative(expr3b)));
			}
			
			// x to f relation
			for (int k = 1; k < d; k++) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						if (i != j) {
							cplex.addLe(f[i][j][k], x[j][i][k]);	
						}
					}
				}
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}				
	}
	public String toString() {
    	return Constants.SMT_FLOW_STRING + "(" + n + "," + d + ")";
	}
}
