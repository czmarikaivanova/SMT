package algorithm;

import graph.Edge;
import graph.Graph;
import graph.Node;

import java.util.ArrayList;

public class MSTAlgorithm extends Algorithm {
    ArrayList<Node> alreadyConnected;
    
    public MSTAlgorithm(boolean onlyDests) {
        super(onlyDests);
    }
    
    @Override
    public Graph solve(Graph graph) {
        try {
            Graph resGraph = (Graph) graph.clone();
            alreadyConnected = new ArrayList<Node>();
            alreadyConnected.add(resGraph.getNode(0));
            int demandedVertexCount = onlyDests ? graph.getDstCount() : graph.getVertexCount();
            while (alreadyConnected.size() < demandedVertexCount) {
                Edge edgeToAdd = findClosest2(resGraph);      // will return new edge  
                resGraph.addEdge(edgeToAdd);         
                alreadyConnected.add(edgeToAdd.getV());
            }
            return resGraph;
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
    private Edge findClosest2(Graph g) {
        float minDst = Float.MAX_VALUE;
        Edge edge = new Edge(null, null, 0);
        for (Node u: alreadyConnected) {            
        	for (int i = 0; i < g.getVertexCount(); i++) {
        		Node v = g.getNode(i);
                if ((!u.equals(v)) && (!alreadyConnected.contains(v)) && (!onlyDests || v.isDestination())) { // do not consider distance to myself
                    float dst = g.getRequir(u, v);
                    if (dst < minDst) {
                        minDst = g.getRequir(u, v);
                        edge.setU(u);
                        edge.setV(v);
                        edge.setCost(dst);
                    }                      
                }
        	}
        }
        return edge;
    }    

    
//    private Edge findClosest2(Graph g) {
//        float minDst = Float.MAX_VALUE;
//        Edge edge = new Edge(null, null, 0);
//        for (int i = 0; i < g.getVertexCount(); i++) {
//            for (int j = 0; j < g.getVertexCount(); j++) {
//            	Node u = g.getNode(i);
//            	Node v = g.getNode(j);
//                if ((!u.equals(v)) && (!alreadyConnected.contains(v)) && (!onlyDests || v.isDestination())) { // do not consider distance to myself
//                    float dst = g.getRequir(u, v);
//                    if (dst < minDst) {
//                        minDst = g.getRequir(u, v);
//                        edge.setU(u);
//                        edge.setV(v);
//                        edge.setCost(dst);
//                    }                      
//                }
//            }
//        }
//        return edge;
//    }    

    @Override
    public String getName() {
        return "MST";
    }

}
