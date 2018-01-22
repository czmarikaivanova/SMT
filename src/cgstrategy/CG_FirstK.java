package cgstrategy;

import java.util.Comparator;
import java.util.PriorityQueue;
import model.STPair;
import graph.Graph;

public class CG_FirstK extends CGStrategy {

	public CG_FirstK(double tolerance, Graph graph, Comparator<STPair> comparator, int k) {
		super(tolerance, graph, comparator);
		this.k = k;
	}
	
	public PriorityQueue<STPair> runSTMaxFlows(PriorityQueue<STPair> violatedPairsQueue, Double[][][] xVar, Double[][][] yVar) {
		restartCounters();
		return filterPairs(super.runSTMaxFlows(violatedPairsQueue, xVar, yVar));
	}

	public String toString() {
		return "FirstK("+ k + ") T=" + tolerance + " Comparator: " + comparator.toString();
	}

	/**
	 * Select first K found pairs
	 * @param violatedPairsQ
	 */
	@Override
	public PriorityQueue<STPair> filterPairs(PriorityQueue<STPair> violatedPairsQ) {
		PriorityQueue<STPair> filteredPairsQ = new PriorityQueue<STPair>(11, this.getComparator());
		int i = 0;
		for (STPair p : violatedPairsQ) {
			if (i >= k) {
				break;
			}
			filteredPairsQ.add(p);
			i++;
		}
		
		return filteredPairsQ;
	}
	
}
