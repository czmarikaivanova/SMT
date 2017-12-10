package model;

import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.ArrayList;

public abstract class ILPModel {
	protected IloCplex cplex;
	protected IloNumVar[][] z;
	protected Graph graph;
	protected boolean allowCrossing = true;
	protected boolean isLP;
	protected int n;
	protected int d;
	protected boolean includeC;
	
	public ILPModel(Graph graph, boolean isLP, boolean includeC) {
		this.graph = graph;
		this.isLP = isLP;
		this.includeC = includeC;
		try {
			cplex = new IloCplex();
//			cplex.setOut(null);
		} catch (IloException e) {
			e.printStackTrace();
		}
		n = graph.getVertexCount();
		d = graph.getDstCount();
		createModel();		
	}
	
	public ILPModel() {
		this.graph = null;
		this.allowCrossing = true;		
	}
		
	protected abstract void initVars();
	protected abstract void createObjFunction();
	protected abstract void createConstraints();
	public Double[][] getZVar() {return null;}
	public Double[][][] getXVar() {return null;} 
	
	protected void createModel() {
		initVars();
		createObjFunction();
		createConstraints();
	}
	
	public boolean solve(boolean useTimeLimit, int seconds) {
		try {
			if (useTimeLimit) {
				System.err.println(this.toString() + " CONSTRINT COUNT " + cplex.getNrows());
				System.err.println(this.toString() + " VARIABLE COUNT " + cplex.getNcols());
				cplex.setParam(IloCplex.DoubleParam.TiLim, seconds);
			}
			return cplex.solve();
		} catch (IloException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public void end() {
		cplex.end();
	}
	
	public double getObjectiveValue() {
		try {
			return cplex.getObjValue();
		} catch (IloException e) {
			System.err.println("No solution exists, return value 0");		
			return 0;
		}
	}
	public abstract String toString();

	public double getCplexTime() {
		try {
			return cplex.getCplexTime();
		} catch (IloException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	
}
