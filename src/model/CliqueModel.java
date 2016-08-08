package model;

import org.javatuples.Quartet;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import graph.ExtendedGraph;
import graph.Graph;
import graph.Node;

public class CliqueModel extends ILPModel {

	protected ExtendedGraph extGraph;
	protected int n;
	protected IloNumVar[] z;	
	
	public CliqueModel(Graph graph, Double[][] zLP) {
		super();
		extGraph = new ExtendedGraph(graph, zLP);
		extGraph.writeDebug();
		createModel();
	}

	@Override
	protected void initVars() {
		n = extGraph.getVertexCount();
		try {
			cplex = new IloCplex();
			z = cplex.boolVarArray(n);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void createConstraints() {
		try {
			// create model and solve it				
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				double weight = extGraph.getNode(i).getWeight();
				obj.addTerm(weight ,z[i]);
			}
			cplex.addMaximize(obj);				
			// -------------------------------------- constraints							
			
			// Connection
			for (int i = 0; i < n; i++) {					
				for (int j = 0; j < n; j++) {
					if (i != j && !extGraph.containsEdge(i, j)) {
						cplex.addLe(cplex.sum(z[i], z[j]), 1);	
					}
				}	
			}			
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}	
	}

	@Override
	public Double[][] getZVar() {
		return null;
	}
	
	public Boolean[] getCliqueVar() {
		try {
			Boolean[] zval = new Boolean[z.length];
			for (int i = 0 ; i < z.length; i++) {
				double val = cplex.getValue(z[i]);
				if (val > 0.5) {
					System.out.println(i +": " + cplex.getValue(z[i]) + " "); // node i was selected
				}
				zval[i] = cplex.getValue(z[i]) < 0.5 ? false: true;						
			}
			System.out.println();
			System.out.println("Objective: " + cplex.getObjValue());
			cplex.end();
			return zval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}		
	
	public ExtendedGraph getExtGraph() 	{
		return extGraph;
	}
}
