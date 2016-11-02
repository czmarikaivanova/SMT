package graph;

public class ExtendedNode extends Node {

	private Node origU;
	private Node origV;
	private double weight;	
	
	public ExtendedNode(Node origU, Node origV, double weight, int id) {
		super(id, true);
		this.origU = origU;
		this.origV = origV;
		this.weight = weight;
	}
	
	/**
	 * @return the origU
	 */
	public Node getOrigU() {
		return origU;
	}

	/**
	 * @return the origV
	 */
	public Node getOrigV() {
		return origV;
	}

	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
}
