package model;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import graph.Graph;

public class SMTMultiFlowModelLP extends SMTMultiFlowModel {

	public SMTMultiFlowModelLP(Graph graph, boolean allowCrossing) {
		super(graph, allowCrossing);
		// TODO Auto-generated constructor stub
	}

	
	protected void initVars() {
		try {
			n = graph.getVertexCount();
			d = graph.getDstCount();
			cplex = new IloCplex();
			x = new IloNumVar[n][n][];
			y = new IloNumVar[n][n][];	
			f = new IloNumVar[n][n][d][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						f[i][j][k] = cplex.numVarArray(d,0,1);	
					}	
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
}
