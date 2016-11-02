package model;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import graph.Graph;

public class SMTFlowModelLP extends SMTFlowModel {

	public SMTFlowModelLP(Graph graph, boolean allowCrossing) {
		super(graph, allowCrossing);
	}
	
	protected void initVars() {
		try {
			n = graph.getVertexCount();
			d = graph.getDstCount();
			cplex = new IloCplex();
			x = new IloNumVar[n][n][];
			y = new IloNumVar[n][n][];	
			f = new IloNumVar[n][n][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					f[i][j] = cplex.numVarArray(d,0,1);	
					x[i][j] = cplex.numVarArray(d,0,1);
					y[i][j] = cplex.numVarArray(d,0,1);							
				}					
			}
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
