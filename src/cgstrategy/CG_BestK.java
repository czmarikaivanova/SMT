package cgstrategy;

import java.util.Comparator;
import java.util.PriorityQueue;

import model.STPair;
import graph.Graph;

public class CG_BestK extends CGStrategy {

	public CG_BestK(double tolerance, Graph graph, Comparator<STPair> comparator,  int k) {
		super(tolerance, graph, comparator);
		this.k = k;
	}
	
	public boolean runSTMaxFlows(PriorityQueue<STPair> violatedPairsQueue,PriorityQueue<STPair> addedPairsQueue, Double[][][] xVar, Double[][][] yVar) {
		restartCounters();
		boolean ret = super.runSTMaxFlows(violatedPairsQueue, addedPairsQueue, xVar, yVar);
		leaveKBest(addedPairsQueue);
		return ret;
	}

	private void leaveKBest(PriorityQueue<STPair> addedPairsQueue) {
		PriorityQueue<STPair> tmpQ = new PriorityQueue<STPair>();
		int i = 0;
		for (STPair p: addedPairsQueue) {
			if (i < k) {
				tmpQ.add(p);
			}
			else {
				break;
			}
			i++;
		}
		addedPairsQueue.clear();
		addedPairsQueue.addAll(tmpQ);
	}
	
	public String toString() {
		return "Best("+ k + ")";
	}
}
