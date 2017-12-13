package cgstrategy;

import java.util.PriorityQueue;
import model.MaxFlow;
import model.STPair;
import graph.Graph;

public class CG_AddFirstK extends CGStrategy {

	private boolean includeFY;
	
	public CG_AddFirstK(double tolerance, Graph graph, int k) {
		super(tolerance, graph);
		this.k = k;
	}
	
	public boolean runSTMaxFlows(PriorityQueue<STPair> violatedPairsQueue, PriorityQueue<STPair> addedPairQueue, Double[][][] xVar, Double[][][] yVar) {
		restartCounters();
		boolean solved = true;
		MaxFlow stFlowModel;
		for (int s = 0; s < d; s++) {
			for (int t = s + 1; t < d; t++) {
				if (s != t) {
					stFlowModel = new MaxFlow(graph, xVar, yVar, s, t, singleFlowCplex);
					stFlowModel.solve(false, 3600);   // solve the max flow problem
					double stVal = stFlowModel.getObjectiveValue();
					STPair stPair = new STPair(s, t, stVal);
					if (stVal > tolerance) { // flow conservation not satisfied for {s,t}
						violatedCnt++;
						if (violatedPairsQueue.size() <  k) {
							addedPairQueue.add(stPair);
						}
						violatedPairsQueue.add(stPair);
						solved = false;
//						break;
					}
					else {
						satisfiedCnt++;
					}
				}
			}
//			if (addedPairQueue.size() > k) {
//				break;
//			}
		}
		return solved;
	}

	public String toString() {
		return "First("+ k + ")";
	}
	
}
