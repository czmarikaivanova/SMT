package model;

import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import graph.ExtendedGraph;
import graph.Graph;

public class CliqueModel extends ILPModel {

	protected ExtendedGraph extGraph;
	protected int n;
	protected IloNumVar[] z;	
	
	public CliqueModel(Graph graph, Double[][] zLP) {
		super();
		extGraph = new ExtendedGraph(graph, zLP);
	}

	@Override
	protected void initVars() {
		n = graph.getVertexCount();
		try {
			cplex = new IloCplex();
			z = cplex.boolVarArray(n);
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void createConstraints() {

	}

	@Override
	public Double[][] getZVar() {
		// TODO Auto-generated method stub
		return null;
	}

}
