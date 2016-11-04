package algorithm;

import java.util.ArrayList;
import smt.Constants;
import graph.Edge;
import graph.Graph;
import graph.Node;

public class BIPAlgorithm extends Algorithm {
    public BIPAlgorithm(boolean onlyDests, boolean multiRoot) {
		super(onlyDests);
		this.multiRoot = multiRoot;
	}

	ArrayList<Node> alreadyConnected;
    boolean multiRoot;
    private int demandedSize;
    
    
    
    @Override
    public Graph solve(Graph graph) {
    	demandedSize = onlyDests ? graph.getDstCount() : graph.getVertexCount();
    	float minCost = Constants.MAX_COST;
        float currCost = Constants.MAX_COST;
        Graph bestGraph = null;
        int bestRootID = 0;
        int it = (multiRoot ? graph.getDstCount() : 1);
        for (int i = 0; i < it; i++) {
        	Node v = graph.getNode(i);
            Graph currGraph = solveFrom1Root(graph, v.getId());
            currCost = currGraph.evaluate(currGraph.getDstCount());
            if (currCost < minCost) {
                bestGraph = currGraph;
                minCost = currCost;
                bestRootID = v.getId();
            }
        }
        System.out.println("Best root: " + bestRootID);
        return bestGraph;
    }

    private Graph solveFrom1Root(Graph graph, int rootID) {
        try {
            Graph resGraph = (Graph) graph.clone();
            alreadyConnected = new ArrayList<Node>();
            alreadyConnected.add(resGraph.getNode(rootID));
            while (alreadyConnected.size() < demandedSize) {
                Edge edgeToAdd = findClosest2(resGraph, graph);      // will return new edge, must calculate from current graph
                edgeToAdd.setCost(graph.getRequir(edgeToAdd.getU(), edgeToAdd.getV()));  // we must calculate from the original graph
                resGraph.addEdge(edgeToAdd);         
                resGraph.updateDstMatrix(edgeToAdd, graph, false);
                alreadyConnected.add(edgeToAdd.getV());
            }
            if (!onlyDests) {
            	basicDeletion(resGraph, alreadyConnected);
        		deletion2(resGraph, graph);
            }
            return resGraph;
        } catch (CloneNotSupportedException ex) {
            return null;
        }        
    }
    
    private Edge findClosest2(Graph g, Graph origG) {
        float minDst = Float.MAX_VALUE;
        Edge edge = new Edge(null, null,0);
        for (Node u: alreadyConnected) {      
        	for (int i = 0 ; i <demandedSize; i++) {
        		Node v = g.getNode(i);
                if (!u.equals(v) && (!alreadyConnected.contains(v)) && (!onlyDests || v.isDestination())) { // do not consider distance to myself
                    float dst = g.getRequir(u.getId(), v.getId());
                    if (dst < minDst) {
                        minDst = dst;
                        edge.setU(u);
                        edge.setV(v);
                        edge.setCost(origG.getRequir(u, v));
                    }                      
                }
            }
        }
        return edge;
    }

	@Override
	public String toString() {
		return "BIP" + (multiRoot ? Constants.MULTIROOT_STRING : "") + (onlyDests ? "_ONLY_DESTS" : "_ALL");
	}  
}
