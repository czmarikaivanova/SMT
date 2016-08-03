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
	protected ArrayList<Quartet<Integer, Integer, Integer, Integer>> crossList;
	protected boolean allowCrossing;
	
	
	public ILPModel(Graph graph, boolean allowCrossing) {
		this.graph = graph;
		this.allowCrossing = allowCrossing;		
//		populate();		
		createModel();		
	}
		
	protected abstract void initVars();
	protected abstract void createConstraints();
	public abstract boolean[][] getZVar();
	
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
	
	/**
	 *  Feed data structures
	 */
//	public void populate() {
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(amplDataFile));
//			String line;
//			while ((line = br.readLine()) != null) {
//				if (line.matches("set CROSS.*")) {
//					// TODO !!
//					ArrayList<String> crossStrList = new ArrayList<String>(Arrays.asList(line.split(" ")));
//					crossStrList.remove(0); // remove the mess at the beginning and end
//					crossStrList.remove(0);
//					crossStrList.remove(0);
//					crossStrList.remove(crossStrList.size() - 1);
//					System.out.println("CROSSING::");
//					System.out.println(Arrays.toString(crossStrList.toArray()));
//					crossList = new ArrayList<Quartet<Integer, Integer, Integer, Integer>>();
//					String[] crossPair;
//					for (String crossStr: crossStrList) {
//						crossStr = crossStr.substring(1, crossStr.length() - 1);
//						crossPair = crossStr.split(",");
//						crossList.add(new Quartet<Integer, Integer, Integer, Integer>(
//								Integer.parseInt(crossPair[0]), 
//								Integer.parseInt(crossPair[1]), 
//								Integer.parseInt(crossPair[2]), 
//								Integer.parseInt(crossPair[3])));
//					}
//					
//				}
//				else if (line.matches("param.*")) {
//					requir = new double[graph.getVertexCount()][graph.getVertexCount()];
//					String parLine;
//					while ((parLine = br.readLine()) != null) {	
//						if (parLine.length() > 2) { // skip the last line
//							String[] entries = parLine.split("\t");
//							for (String s: entries) {
//								s = s.trim();
//								String[] entry = s.split(" ");
//								requir[Integer.parseInt(entry[0])][Integer.parseInt(entry[1])] = Double.parseDouble(entry[2]);								
//							}
//						}
//					}
//				}					
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {				
//			e.printStackTrace();
//		}
//	}    
}
