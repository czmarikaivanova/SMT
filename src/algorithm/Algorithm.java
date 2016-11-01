package algorithm;

import graph.Graph;
import graph.Tree;
/**
 *
 * @author miv022
 */
public abstract class Algorithm {
    
    protected String name = "GENERIC";
    protected boolean onlyDests; // construct a tree only from destinations
    
    public Algorithm(boolean onlyDests) {
        this.onlyDests = onlyDests;
    }
    
    public abstract Graph solve(Graph graph);
    
    public String getName() {
        return null;
    }    
    
}