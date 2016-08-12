package graph;
import java.util.ArrayList;

import smt.Miscellaneous;

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
		s += ")=" + Miscellaneous.round(weight, 2);
		return s;
	}
	
}
