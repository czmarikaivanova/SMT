package cgstrategy;

import java.util.Comparator;
import java.util.PriorityQueue;
import graph.Graph;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import model.ILPModel;
import model.MaxFlow;
import model.STPair;

/**
 * Constraint generation strategy. It simply finds all s-t pairs where the max flow size is < 1 and return s all of them
 * (flow constraints associated with all the violated s-t pairs will be added).  Other strategies inherit from this class.
 * It is not suitable at all as after the first run, most of the s-t pairs are violated
 *
 */
public class CG_All extends CGStrategy {
	
	public CG_All(double tolerance, Graph graph, Comparator<STPair> comparator) {
		super(tolerance, graph, comparator);
	}

	/**
	 * run max flow for each s-t pair 
	 * @param addedPairsQueue
	 * @param xVar - capacity. Calculated by a SMT model that calls this procedure. This variable is used for modelling the constraints f_{ij}^{st}\leq x_{ij}^s
	 * @return true if all flow conservation constraint and symmetry are satisfied for each s-t pair
	 */
	public PriorityQueue<STPair> runSTMaxFlows(PriorityQueue<STPair> violatedPairsQueue, Double[][][] xVar, Double[][][] yVar) {
		restartCounters();
		return filterPairs(super.runSTMaxFlows(violatedPairsQueue, xVar, yVar));
	}
	
	/**
	 * Select all the pairs. 
	 * @param violatedPairsQ
	 */
	@Override
	public PriorityQueue<STPair> filterPairs(PriorityQueue<STPair> violatedPairsQ) {
		PriorityQueue<STPair> filteredPairsQ = new PriorityQueue<STPair>(11, this.getComparator());
		filteredPairsQ.addAll(violatedPairsQ);
		return filteredPairsQ;
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
	
	public Comparator<STPair> getComparator() {
		return comparator;
	}
}
