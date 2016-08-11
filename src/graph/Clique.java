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
	
	public String toString() {
		String s = new String("(");
		for (ExtendedNode en: this) {
			s += Integer.toString(en.getId()) + " ";
		}
		s += ")";
		return s;
	}
	
}
