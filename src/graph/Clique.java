package graph;
import java.util.ArrayList;

public class Clique extends ArrayList<ExtendedNode> {
	private double weight;
	
	public Clique(double weight) {
		super();
		this.weight = weight;
	}
	
	public double getWeight() {
		return weight;
	}
	
}
