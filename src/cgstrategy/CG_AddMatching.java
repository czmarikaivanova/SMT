package cgstrategy;

import java.util.ArrayList;
import java.util.PriorityQueue;

import model.STFlow;
import model.STPair;
import graph.Graph;

public class CG_AddMatching extends CGStrategy {

	private int minViolatedCnt;
	
	/**
	 * 
	 * @param tolerance
	 * @param graph
	 * @param minViolatedCnt minimum # of violated s-t pairs necessary for applying the matching strategy. If there are less pairs, add all constraints.
	 */
	public CG_AddMatching(double tolerance, Graph graph, int minViolatedCnt, boolean includefimp) {
		super(tolerance, graph, includefimp);
		this.minViolatedCnt = minViolatedCnt;
	}

	public boolean runSTMaxFlows(PriorityQueue<STPair> violatedPairsQueue,PriorityQueue<STPair> addedPairsQueue, Double[][][] xVar, Double[][][] yVar) {
		restartCounters();
		boolean ret = super.runSTMaxFlows(violatedPairsQueue, addedPairsQueue, xVar, yVar);
		if (violatedCnt > minViolatedCnt) {
			leaveMatching(addedPairsQueue);
		}
		return ret;
	}
	
	private void leaveMatching(PriorityQueue<STPair> stQueue) {
//		System.out.println("---------------------------QUEUE--------------");
//		System.out.println(stQueue.toString());
		if (stQueue.size() == 0) return;
		STPair[] inArray = stQueue.toArray(new STPair[stQueue.size()]);
		stQueue.clear();
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
		stQueue.addAll(outArray);
//		System.out.println(outArray.toString());
	}
	public String toString() {
		return "AddMatching";
	}
}