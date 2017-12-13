package cgstrategy;

import java.util.Comparator;
import java.util.PriorityQueue;

import model.STPair;
import graph.Graph;

public class CG_MaxSpanningTree extends CGStrategy {

	public CG_MaxSpanningTree(double tolerance, Graph graph, Comparator<STPair> comparator) {
		super(tolerance, graph, comparator);
	}

	
	public boolean runSTMaxFlows(PriorityQueue<STPair> violatedPairsQueue,PriorityQueue<STPair> addedPairsQueue, Double[][][] xVar, Double[][][] yVar) {
		restartCounters();
		boolean ret = super.runSTMaxFlows(violatedPairsQueue, addedPairsQueue, xVar, yVar);
		if (violatedCnt > minViolatedCnt) {
		//	leaveMaxSpanTree(addedPairsQueue);
		}
		return ret;
	}


	private void leaveMaxSpanTree(PriorityQueue<STPair> addedPairsQueue) {
		// TODO Auto-generated method stub
		
	}
	
}
