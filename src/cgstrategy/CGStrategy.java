package cgstrategy;

import java.util.PriorityQueue;

import graph.Graph;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import model.ILPModel;
import model.MaxSTFlowSYM;
import model.STPair;

/**
 * Constraint generation strategy. It simply finds all s-t pairs where the max flow size is < 1 and return s all of them
 * (flow constraints associated with all the violated s-t pairs will be added).  Other strategies inherit from this class.
 *
 */
public class CGStrategy {
	protected double tolerance;   		// we need to allow some tolerance from the size of max flow due to rounding errors.
	protected IloCplex singleFlowCplex;	// cplex object for the max flow problem
	protected int violatedCnt; 			// # violated s-t pairs
	protected int satisfiedCnt; 			// # s-t pairs that satisfy flow of size 1
	protected Graph graph;
	protected int d; 						// # destinations
	protected int k;
	
	public CGStrategy(double tolerance, Graph graph) {
		super();
		this.tolerance = tolerance;
		this.graph = graph;
		d = graph.getDstCount();
		k = 0; // assume k not applicable
		try {
			singleFlowCplex = new IloCplex();
		} catch (IloException e) {
			e.printStackTrace();
		}
	}

	/**
	 * run max flow for each s-t pair 
	 * @param addedPairsQueue
	 * @param xVar - capacity. Calculated by a SMT model that calls this procedure. This variable is used for modelling the constraints f_{ij}^{st}\leq x_{ij}^s
	 * @return true if all flow conservation constraint and symmetry are satisfied for each s-t pair
	 */
	public boolean runSTMaxFlows(PriorityQueue<STPair> violatedPairsQueue, PriorityQueue<STPair> addedPairsQueue, Double[][][] xVar, Double[][][] yVar) {
		boolean solved = true;
		MaxSTFlowSYM stFlowModel;
		for (int s = 0; s < d; s++) {
			for (int t = s + 1; t < d; t++) {
				if (s != t) {
					stFlowModel = new MaxSTFlowSYM(graph, xVar, yVar, s, t, singleFlowCplex);
					stFlowModel.solve(false, 3600);   // solve the max flow problem
					double stVal = stFlowModel.getObjectiveValue();
					STPair stPair = new STPair(s, t, stVal);
					if (stVal > tolerance) { // flow conservation not satisfied for {s,t}
						violatedCnt++;
						violatedPairsQueue.add(stPair);
						addedPairsQueue.add(stPair);
						solved = false;
					}
					else {
						satisfiedCnt++;
					}
				}
			}
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
	
	protected int getK() {
		return k;
	}
	
	public String toString() {
		return "All";
	}
}
