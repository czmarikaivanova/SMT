package cgstrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import model.STPair;
import graph.Graph;

public class CG_AddMatching extends CGStrategy {
	
	/**
	 * 
	 * @param tolerance
	 * @param graph
	 * @param minViolatedCnt minimum # of violated s-t pairs necessary for applying the matching strategy. If there are less pairs, add all constraints.
	 */
	public CG_AddMatching(double tolerance, Graph graph, Comparator<STPair> comparator) {
		super(tolerance, graph, comparator);
	}

	public PriorityQueue<STPair> runSTMaxFlows(PriorityQueue<STPair> violatedPairsQueue, Double[][][] xVar, Double[][][] yVar) {
		restartCounters();
		return filterPairs(super.runSTMaxFlows(violatedPairsQueue, xVar, yVar));
	}

	@Override
	public String toString() {
		return "Matching_" + comparator.toString();
	}
	
	/**
	 * construct a maximum matching from the set of violated s-t pairs. 
	 * @param violatedPairsQ
	 */
	@Override
	public PriorityQueue<STPair> filterPairs(PriorityQueue<STPair> violatedPairsQ) {
		PriorityQueue<STPair> filteredPairsQ = new PriorityQueue<STPair>(11, this.getComparator());
		if (violatedPairsQ.size() <= minViolatedCnt) {
			filteredPairsQ.addAll(violatedPairsQ);
			return filteredPairsQ;
		}
		STPair[] inArray = violatedPairsQ.toArray(new STPair[violatedPairsQ.size()]);
//		violatedPairsQ.clear();
		ArrayList<STPair> outArray = new ArrayList<STPair>();

		outArray.add(inArray[0]);
		for (int i = 1; i < inArray.length; i++) {
			STPair currPair = inArray[i];
			boolean add = true;
			for (STPair pair : outArray) {
				if (pair.getS() == currPair.getS() || pair.getS() == currPair.getT() || pair.getT() == currPair.getS() || pair.getT() == currPair.getT()) {
					add = false;
					break;
				}
			}
			if(add) {
				outArray.add(currPair);
			}
		}
		filteredPairsQ.addAll(outArray);
		return filteredPairsQ;
	}
}
