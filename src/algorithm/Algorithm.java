package algorithm;

import java.util.ArrayList;

import graph.Edge;
import graph.Graph;
import graph.Node;
/**
 *
 * @author miv022
 */
public abstract class Algorithm {
    
    protected boolean onlyDests; // construct a tree only from destinations
	private final int LEAF_DEGREE = 1;
	
    public Algorithm(boolean onlyDests) {
        this.onlyDests = onlyDests;
    }
    
    public abstract Graph solve(Graph graph);
    
    public abstract String toString();
    
    protected void basicDeletion(Graph graph, ArrayList<Node> alreadyCon) { // do we need alreadyCon ???
        Node v = findNonDestDeg(graph, LEAF_DEGREE);
        while (v != null) {
            while ((v.getDegree() == LEAF_DEGREE) && !v.isDestination()) {
                Node u = v.getNeighbour(0);
                removeVertex(v);
                alreadyCon.remove(v);
                u.removeNeighbour(v);
                v = u;
            }
            v = findNonDestDeg(graph, LEAF_DEGREE);
        }
    }
    
    protected ArrayList<Edge> removeVertex(Node u) {
        for (Edge e: u.getIncidentEdges()) { // remove from neighbour's
            e.getV().removeNeighbour(u);
        }
        ArrayList<Edge> incidentEdges = new ArrayList<Edge>(u.getIncidentEdges());
        u.getIncidentEdges().clear(); // disconnect
        u.clearLevels();
        return incidentEdges;
    }    
    
    private Node findNonDestDeg(Graph g, int deg) {
    	
    	for (int i = 0; i < g.getVertexCount(); i++) {
    		Node v = g.getNode(i);
    		if (!v.isDestination() && (v.getDegree() == deg)) {
                return v;
            }
        }
        return null;
    }    
    
    /**
     * Try to delete non-destinations with deg=2
     * @param resGraph  TODO
     */
    protected void deletion2(Graph resGraph, Graph origGraph) {
        float potCost = 0;
        float currCost = resGraph.evaluate(resGraph.getDstCount());
        for (int i = 0; i < resGraph.getVertexCount(); i++) {
        	Node v = resGraph.getNode(i);
        	if ((v.getDegree() == 2) && !v.isDestination()) {
                potCost = resGraph.getCostWithout(v, origGraph);
                if (potCost < currCost) {
                    connectNeighbours(v, resGraph, origGraph);
                    removeVertex(v);
                    currCost = resGraph.evaluate(resGraph.getDstCount());       
                    if ((currCost - potCost) > 2 ) {
                        System.err.println("That's a serious problem!");
                        resGraph.evaluate(resGraph.getDstCount());
                    }
                }
            }
        }
    }    
    
    // for deg(v) = 2 only !!
    private void connectNeighbours(Node v, Graph g, Graph origGraph) {
        Node u = v.getNeighbour(0);
        Node w = v.getNeighbour(1);
        Edge e = new Edge(u,w, origGraph.getRequir(u, w));
        g.addEdge(e);
    }    
}