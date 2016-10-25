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
	public abstract void addCrossCliqueConstraints(ArrayList<Clique> cliqueList);
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
	
	public boolean solveAndLogTime() {
		try {
			cplex.setParam(IloCplex.Param.ClockType, 1);
			cplex.getCplexTime();
 			File datafile = new File("logs/runtime_log.txt");
 			File datafile2 = new File("logs/cost_log.txt");
			FileWriter fw;
			FileWriter fw2;
			fw = new FileWriter(datafile,true);
			fw2 = new FileWriter(datafile2,true);			
			long start = System.currentTimeMillis();
			boolean ret = cplex.solve();
			long end = System.currentTimeMillis();
			fw.write(this.toString() + " Time: " + (end - start)/1000. + " seconds\n");
			fw2.write(this.toString() +  " ID: " + this.graph.getInstId() + "\n Cost: " + cplex.getObjValue() + "\n ------------------\n");
			fw.close();
			fw2.close();
			return ret;
		} catch (IloException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
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
			System.err.println("No solution exists, return value 0");		
			return 0;
		}
	}



}
