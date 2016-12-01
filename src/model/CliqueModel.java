package model;

import java.util.ArrayList;

import org.javatuples.Quartet;

import smt.Constants;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import graph.Clique;
import graph.ExtendedGraph;
import graph.ExtendedNode;
import graph.Graph;
import graph.Node;

public class CliqueModel extends ILPModel {

	protected ExtendedGraph extGraph;
	protected int n;
	protected IloNumVar[] z;	
	
	public CliqueModel(Graph graph, Double[][] zLP) {
		super();
		extGraph = new ExtendedGraph(graph, zLP);
//		extGraph.writeDebug();
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
	protected void createObjFunction() {
		try {
		IloLinearNumExpr obj = cplex.linearNumExpr();
		for (int i = 0; i < n; i++) {
			double weight = extGraph.getNode(i).getWeight();
			obj.addTerm(weight ,z[i]);
		}
		cplex.addMaximize(obj);	
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}
	}
	
	@Override
	protected void createConstraints() {
		try {
			// create model and solve it				
			
			// Connection
			for (int i = 0; i < n; i++) {					
				for (int j = i+1; j < n; j++) {
					if (i != j && !extGraph.containsEdge(i, j) && !extGraph.containsEdge(j, i)) {
						cplex.addLe(cplex.sum(z[i], z[j]), 1);	
					}
				}	
			}	
			
			// At least weight one
			IloLinearNumExpr expr = cplex.linearNumExpr();	
			for (int i = 0; i < n; i++) {					
				expr.addTerm(extGraph.getNode(i).getWeight(), z[i]);
			}					
			cplex.addLe(1, expr);
			
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
			return zval;		
		} catch (IloException e) {		
			System.err.println("No clique with weight > 1 exists.");
//			e.printStackTrace();
			return null;
		}		
	}		
	
	public ExtendedGraph getExtGraph() 	{
		return extGraph;
	}

	public void addClConstraint(Clique clique) {
		// Flow conservation - dest
		IloLinearNumExpr expr;
		try {
			expr = cplex.linearNumExpr();
			double w = 0;
			for (ExtendedNode en: clique) {
				int id = en.getId();
//				w = extGraph.getNode(id).getWeight();
				expr.addTerm(1.0, z[id]);									
			}
			cplex.addLe(expr, clique.size()-1);
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void addCrossCliqueConstraints(ArrayList<Clique> cliqueList) {
		System.err.println("Unsupported operation adding cliques to the CliqueModel");
		System.exit(1);
	}
	
	public String toString() {
    	return Constants.CLIQUE_STRING + "(" + Integer.toString(extGraph.getVertexCount()) + ")";
	}

	@Override
	public Double[][][] getXVar() {
		// TODO Auto-generated method stub
		return null;
	}


	
}
