package graph;

import java.util.ArrayList;

public class Tree extends Graph {
	
	
	public Tree(Graph g) {
		super(g.getVertexCount(), g.getDstCount());
	}
	

    //T(r1/r2)
    private int calcSubTrees(Edge e, int nod) {
        if (e == null) {
            System.out.println("e is null");
        }
        Node r1 = e.getU();
        Node r2 = e.getV();        
        int stSize = 0;
        if (r2.isDestination()) {
            stSize = 1;            
        }
        Edge opositeDir = r2.getLinkToNeighbour(r1);
        if (r2.getIncidentEdges().size() == 1) { // r2 has only its parent!
            opositeDir.setSubTreeSize(stSize);
            e.setSubTreeSize(nod - stSize);
            return stSize;
        } 
        for (Edge f: r2.getIncidentEdges()) {
            if (!f.getV().equals(r1)) {
                stSize += calcSubTrees(f, nod);
            }
        }
        opositeDir.setSubTreeSize(stSize);
        e.setSubTreeSize(nod - stSize);
        return stSize;
    }
    
    
    public float evaluate(int nod, ArrayList<Node> alreadyConnected, boolean potCost) {
        float myCost = 0;
        calcSubTrees(findSomeLeaf(), nod);
//        if (alreadyConnected != null) { // only GSMT
//            for (Vertex w: vertices) {
//                if (!alreadyConnected.contains(w)) {
//                    int i = 0;
//                    Vertex closestToW = w.getPotentialNeighbours().get(i).getV();
//                    while (!alreadyConnected.contains(closestToW)) {
//                        i++;
//                        closestToW = w.getPotentialNeighbours().get(i).getV();
//                    }
//
//                }
//            }
//        }
        for (Node v: nodes) {
            if (v.getJ1() != null) {
                myCost += v.getCost(nod);                
            }
        }
//        this.cost = myCost;
        return myCost; 
    }
    
    public Edge findSomeLeaf() {
        for (Node v: nodes) {
            if (v.getIncidentEdges().size() == 1) {
                return v.getIncidentEdges().get(0);
            }
        }
        return null;
    }	
	
}
