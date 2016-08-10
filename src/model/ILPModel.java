package model;

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
	protected boolean allowCrossing;
	
	public ILPModel(Graph graph, boolean allowCrossing) {
		this.graph = graph;
		this.allowCrossing = allowCrossing;		
//		populate();		
		createModel();		
	}
	
	public ILPModel() {
		this.graph = null;
		this.allowCrossing = true;		
	}
		
	protected abstract void initVars();
	protected abstract void createConstraints();
	public abstract Double[][] getZVar();
	
	protected void createModel() {
		initVars();
		createConstraints();
	}
	
	public boolean solve() {
		try {
			return cplex.solve();
		} catch (IloException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public IloCplex getModel() {
		return cplex;
	}

	public void end() {
		cplex.end();
	}
	
	public double getObjectiveValue() {
		try {
			return cplex.getObjValue();
		} catch (IloException e) {
			e.printStackTrace();			
			return 0;
		}
	}

}
