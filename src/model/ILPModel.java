package model;

import graph.Clique;
import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.javatuples.Quartet;
import smt.Constants;
import smt.Miscellaneous;

public abstract class ILPModel {
	protected IloCplex cplex;
	protected IloNumVar[][] z;
	protected Graph graph;
	protected boolean allowCrossing = true;
	protected boolean willAddVIs;
	protected boolean isLP;
	protected int n;
	protected int d;
	protected boolean lazy;
	
	public ILPModel(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		this.graph = graph;
		this.willAddVIs = willAddVIs;
		this.isLP = isLP;
		this.lazy = lazy;
		try {
			cplex = new IloCplex();
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
	public void addCrossCliqueConstraints(ArrayList<Clique> cliqueList) {}
	public abstract Double[][] getZVar();
	public abstract Double[][][] getXVar();
	
	protected void addValidInequalities() {}
	
	protected void createModel() {
		initVars();
		createObjFunction();
		createConstraints();
		if (willAddVIs) {
			addValidInequalities();
		}
	}
	
	public boolean solve(boolean useTimeLimit, int seconds) {
		try {
			if (useTimeLimit) {
				cplex.setParam(IloCplex.DoubleParam.TiLim, seconds);
			}
			return cplex.solve();
		} catch (IloException e) {
			e.printStackTrace();
			return false;
		}
	}
	
//	public IloCplex getModel() {
//		return cplex;
//	}

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
