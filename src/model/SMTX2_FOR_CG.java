package model;

import smt.App;
import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import graph.Graph;
import graph.Node;

public class SMTX2_FOR_CG extends SMTX1VI {

	public SMTX2_FOR_CG(Graph graph, boolean isLP, boolean includeC) {
		super(graph, isLP, includeC);
	}
	
	protected IloNumVar[][][][] f;
	
	protected void initVars() {
		try {
			super.initVars();
			f = new IloNumVar[n][n][d][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						f[i][j][k] = cplex.numVarArray(d,0,1);	
					}	
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	
	
	//objective from SMTX1VI
	
	public void createConstraints() {
		try{
			super.createConstraints();
			// capacity
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
								cplex.addLe(f[i][j][s][t], x[i][j][s]);
							}
						}
					}
				}
			}					
			// f sym
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
								cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
							}
						}
					}
				}
			}					
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
	

}
