package graph;

import java.util.ArrayList;

import smt.Main;

/**
 *
 * @author miv022
 */
public class Edge {
    private Node u;
    private Node v;
    private int subtreeSize;
    private float cost;
    private ArrayList<Node> potentialSubtree;
    
    
    public Edge(Node u, Node v, float cost) {
        this.u = u;
        this.v = v;
        this.cost = cost;
    }

    public Node getU() {
        return u;
    }

    public void setU(Node u) {
        this.u = u;
    }

    public Node getV() {
        return v;
    }

    public void setV(Node v) {
        this.v = v;
    }
    
    public int getSubtreeSize() {
        return subtreeSize;
    }
    
    @Override
    public String toString() {
        return "(" + u.getId() + ", " + v.getId() + ")";
    }

    void setSubTreeSize(int i) {
        this.subtreeSize = i;
    }
    
    public float getCost() {
        return this.cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public void addToPotentialSubtree(Node w) {
        if (potentialSubtree == null) {
            potentialSubtree = new ArrayList<Node>();
        }
        potentialSubtree.add(w);
    }
    
    
    public int getPotentialSubtreeSize() {
        if (potentialSubtree == null) {
            return 0;
        }
        return potentialSubtree.size();
    }
    

//    public int compareTo(Object t) {
//        Edge compareEdge = (Edge) t;
//        double length = Main.distance(getU(), getV());
//        double clength = Main.distance(compareEdge.getU(), compareEdge.getV());
//        if (length < clength) {
//            return -1;
//        }
//        else if (length > clength){
//            return 1;
//        }
//        return 0;        
//    }

}