package model;

import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.MIPInfoCallback;

import java.util.ArrayList;

public abstract class ILPModel {
	protected IloCplex cplex;
	protected IloNumVar[][] z;
	protected Graph graph;
	protected boolean allowCrossing = true;
	private boolean isLP;
	protected int n;
	protected int d;
	
	public ILPModel(Graph graph, boolean isLP) {
		this.graph = graph;
		this.setLP(isLP);
		try {
			cplex = new IloCplex();
			cplex.setParam(IloCplex.Param.MIP.Display, 2);
			cplex.setParam(IloCplex.Param.MIP.Interval, 1);
			cplex.setParam(IloCplex.Param.Simplex.Display, 1);
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
	public Double[][] getTreeVar() {return null;}	// returns the variable that induces the solution
	public Double[][][] get3DVar() {return null;}  	// returns the variable with 3 indices - either x_{ij}^s (for X model) or f_{ij}^t (for  
	
	protected void createModel() {
		initVars();
		createObjFunction();
		createConstraints();
	}
	
	public boolean solve(boolean useTimeLimit, int seconds) {
		try {
			if (useTimeLimit) {
				cplex.setParam(IloCplex.DoubleParam.TiLim, seconds);
			}
			outputSettingsInfo();
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
	
	protected void outputSettingsInfo() {
		if (! (this instanceof MaxFlow)) {
			System.err.println("RUNNING MODEL: " + this.toString());
			System.err.println(this.toString() + " CONSTRINT COUNT " + cplex.getNrows());
			System.err.println(this.toString() + " VARIABLE COUNT " + cplex.getNcols());
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

	public double getGap() {
		try {
			return  cplex.getMIPRelativeGap();
		} catch (IloException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * @return the isLP
	 */
	public boolean isLP() {
		return isLP;
	}

	/**
	 * @param isLP the isLP to set
	 */
	public void setLP(boolean isLP) {
		this.isLP = isLP;
	}
	
	
}
