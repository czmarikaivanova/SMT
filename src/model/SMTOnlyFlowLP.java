package model;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import graph.Graph;

public class SMTOnlyFlowLP extends SMTOnlyFlow {

	public SMTOnlyFlowLP(Graph graph, boolean allowCrossing) {
		super(graph, allowCrossing);
	}

	protected void initVars() {
		try {
			n = graph.getVertexCount();
			d = graph.getDstCount();
			cplex = new IloCplex();
			f = new IloNumVar[n][n][d][];
			y = new IloNumVar[n][n][];				
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {			
					for (int s = 0; s < d; s++) {
						f[i][j][s] = cplex.numVarArray(d,0,1);	
					}
					
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
