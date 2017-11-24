package cgstrategy;

import java.util.PriorityQueue;

import model.STFlow;
import model.STPair;
import graph.Graph;

public class CG_AddFirstK extends CGStrategy {

	private int k;
	
	public CG_AddFirstK(double tolerance, Graph graph, int k) {
		super(tolerance, graph);
		this.k = k;
	}
	
	public boolean runSTMaxFlows(PriorityQueue<STPair> pairQueue, Double[][][] xVar) {
		restartCounters();
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
						break;
					}
					else {
						satisfiedCnt++;
					}
				}
			}
			if (pairQueue.size() > k) {
				break;
			}
		}
		return solved;
	}

	public String toString() {
		return " AddFirstK, k = " + k;
	}
	
}
