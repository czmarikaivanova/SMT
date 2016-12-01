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
	
	public ILPModel(Graph graph, boolean willAddVIs, boolean isLP) {
		this.graph = graph;
		this.willAddVIs = willAddVIs;
		this.isLP = isLP;
		createModel();		
	}
	
	public ILPModel() {
		this.graph = null;
		this.allowCrossing = true;		
	}
		
	protected abstract void initVars();
	protected abstract void createConstraints();
	public void addCrossCliqueConstraints(ArrayList<Clique> cliqueList) {} ;
	public abstract Double[][] getZVar();
	public abstract Double[][][] getXVar();
	
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


}
