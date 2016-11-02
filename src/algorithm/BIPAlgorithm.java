package algorithm;

import java.util.ArrayList;

import smt.Constants;
import graph.Edge;
import graph.Graph;
import graph.Node;

public class BIPAlgorithm extends Algorithm {
    public BIPAlgorithm(boolean onlyDests) {
		super(onlyDests);
	}

	ArrayList<Node> alreadyConnected;
    int rootID;
    
    @Override
    public Graph solve(Graph graph) {
        float minCost = Constants.MAX_COST;
        float currCost = Constants.MAX_COST;
        Graph bestGraph = null;
//        for (Vertex v: graph.getVertices()) {
        Node v = graph.getNode(0);
            Graph currGraph = solveFrom1Root(graph, v.getId());
            currCost = currGraph.evaluate(currGraph.getDstCount());
            if (currCost < minCost) {
                bestGraph = currGraph;
                minCost = currCost;
                rootID = v.getId();
            }
//        }
        return bestGraph;
    }

   
    private Graph solveFrom1Root(Graph graph, int rootID) {
        try {
            Graph resGraph = (Graph) graph.clone();
            alreadyConnected = new ArrayList<Node>();
            alreadyConnected.add(resGraph.getNode(rootID)
                    );
            int demandedVertexCount = onlyDests ? graph.getDstCount() : graph.getVertexCount();              
            while (alreadyConnected.size() < demandedVertexCount) {
                Edge edgeToAdd = findClosest2(resGraph, graph);      // will return new edge, must calculate from current graph
                float cost = graph.getRequir(edgeToAdd.getU(), edgeToAdd.getV()); 
                edgeToAdd.setCost(graph.getRequir(edgeToAdd.getU(), edgeToAdd.getV()));  // we must calculate from the original graph
                resGraph.addEdge(edgeToAdd);         
                resGraph.updateDstMatrix(edgeToAdd, graph, false);
                alreadyConnected.add(edgeToAdd.getV());
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
        	for (int i = 0 ; i <g.getDstCount(); i++) {
        		Node v = g.getNode(i);
                if (!u.equals(v) && (!alreadyConnected.contains(v)) && (!onlyDests || v.isDestination())) { // do not consider distance to myself
                    float dst = g.getRequir(u.getId(), v.getId());
                    if (dst < minDst) {
                        minDst = dst;
                        edge.setU(u);
                        edge.setV(v);
                        float cost = origG.getRequir(u, v); 
                        edge.setCost(origG.getRequir(u, v));
                    }                      
                }
            }
        }
        return edge;
    }


	@Override
	public String getName() {
		return "BIP";
	}  
}
