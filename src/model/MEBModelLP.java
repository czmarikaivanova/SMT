package model;

import smt.Constants;
import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class MEBModelLP extends MEBModel {
	
	public MEBModelLP(Graph graph, boolean allowCrossing) {
		super(graph, allowCrossing);
	}

	@Override
	protected void initVars() {
		n = graph.getVertexCount();
		d = n - 1;
		try {
			cplex = new IloCplex();
			x = new IloNumVar[n][n][];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					x[i][j] = cplex.numVarArray(d,0,1);
				}					
			}
			p = cplex.numVarArray(n, 0, 99999);
			z = new IloNumVar[n][];				
			for (int j = 0; j < n; j++) {
				z[j] = cplex.numVarArray(n,0,1);		
			}	
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return super.toString() + Constants.LP_STRING;
	}
}
