package cgstrategy;

import java.util.PriorityQueue;

import graph.Graph;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import model.STFlow;
import model.STPair;

public class CGStrategy {
	protected double tolerance;
	protected IloCplex singleFlowCplex;
	protected int violatedCnt;
	protected int satisfiedCnt;
	protected Graph graph;
	protected int d;
	
	public CGStrategy(double tolerance, Graph graph) {
		super();
		this.tolerance = tolerance;
		this.graph = graph;
		d = graph.getDstCount();
		try {
			singleFlowCplex = new IloCplex();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * run max flow for each s-t pair 
	 * @param pairQueue
	 * @param xVar - capacity. Calculated by a SMT model that calls this procedure. This variable is used for modelling the constraints f_{ij}^{st}\leq x_{ij}^s
	 * @return true if all flow conservation constraint and symmetry are satisfied for each s-t pair
	 */
	public boolean runSTMaxFlows(PriorityQueue<STPair> pairQueue, Double[][][] xVar) {
		boolean solved = true;
		STFlow stFlowModel;
		for (int s = 0; s < d; s++) {
			for (int t = s + 1; t < d; t++) {
				if (s != t) {
					stFlowModel = new STFlow(graph, xVar, s, t, singleFlowCplex);
					stFlowModel.solve(false, 3600);   // solve the max flow problem
					double stVal = stFlowModel.getObjectiveValue();
					STPair stPair = new STPair(s, t, stVal);
					if (stVal > tolerance) { // flow conservation not satisfied for {s,t}
						violatedCnt++;
						pairQueue.add(stPair);
						solved = false;
//						break;
					}
					else {
						satisfiedCnt++;
					}
				}
			}
//			if (pairQueue.size() > 5) break;
//			if (!solved) break;
		}
		return solved;
	}
	
	/**
	 * 
	 * @return # of s-t pairs that satisfy flow constraints
	 */
	public int getSatisfiedCnt() {
		return satisfiedCnt;
	}

	/**
	 * 
	 * @return # s-t pairs that violate flow constraints
	 */
	public int getViolatedCnt() {
		return violatedCnt;
	}
	
	protected void restartCounters() {
		violatedCnt = 0;
		satisfiedCnt = 0;
	}
	
	public double getTolerance() {
		return tolerance;
	}
	
	public String toString() {
		return "AddAll";
	}
}
