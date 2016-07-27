package smt;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;

public class Node {
	Point p;
	private boolean isDest;
	public ArrayList<Node> orderedNeighbours;
	public final int id;
	
	
	public Node(int id, Point p, boolean isDest) {
		super();
		this.p = p;
		this.isDest = isDest;
		orderedNeighbours = new ArrayList<Node>();
		this.id = id;
	}
	
	public Node(int id) {
		super();
		Random rnd = new Random();		
		this.p = new Point(rnd.nextInt(Visualizer.MAX_COORD), rnd.nextInt(Visualizer.MAX_COORD));
		this.isDest = true;
		orderedNeighbours = new ArrayList<Node>();
		this.id = id;
	}
		
	
	public Point getPoint() {
		return p;
	}
	
	public void orderNeighbours(Node[] allNodes) {
		orderedNeighbours = new ArrayList<>(allNodes.length - 1);
		for (Node node: allNodes) {
			if (!node.equals(this)) {				
				float dst = Main.dst(this.p, node.getPoint());
				boolean inserted = false;
				for (Node nb: orderedNeighbours) {
					if (dst < Main.dst(this.p, nb.getPoint())) {
						orderedNeighbours.add(orderedNeighbours.indexOf(nb), node);
						inserted = true;
						break;
					}
				}
				if (!inserted) {
					orderedNeighbours.add(node);
				}
			}			
		}
	}
}
