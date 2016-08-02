package model;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.File;

import smt.Graph;

public class SMTModelLP extends SMTModel {

	public SMTModelLP(Graph graph, boolean allowCrossing) {
		super(graph, allowCrossing);
	}
	
	public void createModel() {
		initVars();					
		createConstraints();		
	}
	
	protected void initVars() {
		try {
			n = graph.getVertexCount();
			d = graph.getDstCount();
			cplex = new IloCplex();
			x = new IloNumVar[n][n][];
			y = new IloNumVar[n][n][];				
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					x[i][j] = cplex.numVarArray(d,0,1);
					y[i][j] = cplex.numVarArray(d,0,1);							
				}					
			}
			z = new IloNumVar[n][];				
			for (int j = 0; j < n; j++) {
				z[j] = cplex.numVarArray(n,0,1);					
			}	
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

}
