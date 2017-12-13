package cgstrategy;

import java.util.PriorityQueue;

import model.STPair;
import graph.Graph;

public class CG_MaxSpanningTree extends CGStrategy {

	public CG_MaxSpanningTree(double tolerance, Graph graph) {
		super(tolerance, graph);
	}

	
	public boolean runSTMaxFlows(PriorityQueue<STPair> violatedPairsQueue,PriorityQueue<STPair> addedPairsQueue, Double[][][] xVar, Double[][][] yVar) {
		restartCounters();
		boolean ret = super.runSTMaxFlows(violatedPairsQueue, addedPairsQueue, xVar, yVar);
		if (violatedCnt > minViolatedCnt) {
			leaveMatching(addedPairsQueue);
		}
		return ret;
	}
	
}
