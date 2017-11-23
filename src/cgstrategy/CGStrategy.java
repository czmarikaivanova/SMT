package cgstrategy;

import model.STFlow;
import model.STPair;

public class CGStrategy {
	
	public void runSTMaxFlows() {
		for (int s = 0; s < d; s++) {
			for (int t = s + 1; t < d; t++) {
				if (s != t) {
					stFlowModel = new STFlow(graph, xVar, s, t, singleFlowCplex);
					stFlowModel.solve(false, 3600);   // solve the max flow problem
					double stVal = stFlowModel.getObjectiveValue();
					STPair stPair = new STPair(s, t, stVal);
					if (stVal > tolerance) { // flow conservation not satisfied for {s,t}
						volatedCnt++;
						pairQueue.add(stPair);
						solved = false;
//						break;
					}
					else {
						satisfiedCnt++;
					}
				}
			}
//			if (pairQueue.size() > 5) break;
//			if (!solved) break;
		}
	}
	
	
}
