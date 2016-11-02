package algorithm;

import graph.Graph;
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
    
    public abstract String getName();
    
}