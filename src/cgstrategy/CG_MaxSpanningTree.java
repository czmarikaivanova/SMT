package cgstrategy;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import model.STPair;
import graph.Graph;

public class CG_MaxSpanningTree extends CGStrategy {

	public CG_MaxSpanningTree(double tolerance, Graph graph, Comparator<STPair> comparator) {
		super(tolerance, graph, comparator);
	}

	
	public PriorityQueue<STPair>  runSTMaxFlows(PriorityQueue<STPair> violatedPairsQueue, Double[][][] xVar, Double[][][] yVar) {
		restartCounters();
		return filterPairs(super.runSTMaxFlows(violatedPairsQueue, xVar, yVar));
	}


	/**
	 * Create a maximum spanning tree from the edges induced by violated s-t pairs
	 */
	@Override
	public PriorityQueue<STPair> filterPairs(PriorityQueue<STPair> stQueue) {
		PriorityQueue<STPair> filteredPairsQ = new PriorityQueue<STPair>(11, this.getComparator());
		Set<Set<Integer>> nodeSets = createSets(stQueue);
		Set<Integer> setS;
		Set<Integer> setT;
		for (STPair p: stQueue) {
			if (nodeSets.size() == 1) {  // if there is only one big set, we cannot add any new edge, so we have a tree.
				return filteredPairsQ;
			}
			setS = findSet(p.getS(), nodeSets);
			setT = findSet(p.getT(), nodeSets);
			if (setS != setT) { 
				mergeSets(nodeSets, setS, setT);
				filteredPairsQ.add(p);
			}
		}
		return filteredPairsQ;
	}

	private void mergeSets(Set<Set<Integer>> nodeSets, Set<Integer> setS, Set<Integer> setT) {
			nodeSets.remove(setS);
			nodeSets.remove(setT);
			setS.addAll(setT);
			nodeSets.add(setS);
	}

	private Set<Integer> findSet(int s, Set<Set<Integer>> nodeSets) {
		for (Set<Integer> set : nodeSets) {
			if (set.contains(s)) {
				return set;
			}
		}
		return null;
	}

	/**
	 * construct a union set for kruskal's algorithm
	 * @param stQueue violated S-T pairs
	 * @return union set
	 */
	private Set<Set<Integer>> createSets(PriorityQueue<STPair> stQueue) {
		HashSet<Integer> setOfNodes = new HashSet<>(); 
		Set<Set<Integer>> nodeSets = new HashSet<Set<Integer>>();
		for (STPair p: stQueue) {
			setOfNodes.add(p.getS());
			setOfNodes.add(p.getT());
		}
		for (Integer i : setOfNodes) {
			HashSet<Integer> oneSet = new HashSet<Integer>();
			oneSet.add(i);
			nodeSets.add(oneSet);
		}
		return nodeSets;
	
	}

	public String toString() {
		return "MaxSpanningTree, T=" + tolerance + " Comparator: " + comparator.toString();
	}
	
	
}
