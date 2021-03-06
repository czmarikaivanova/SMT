package graph;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;


import smt.Constants;
import smt.Miscellaneous;

public class Node {
	private Point p;
	private boolean isDest;
	public ArrayList<Node> orderedNeighbours;  // neighbours = all nodes in a graph. We want to order them
	public ArrayList<Edge> incidentEdges;      // adjacent nodes = nodes connected to me by an edge
    private Edge j1;
    private Edge j2;
	
	protected final int id;
	
	public Node(int id, Point p, boolean isDest) {
		super();
		this.p = p;
		this.isDest = isDest;
		orderedNeighbours = new ArrayList<Node>();
		incidentEdges = new ArrayList<Edge>();
		this.id = id;  
	}
	
	public Node(int id, boolean isDest, Node[] nodes) {
		super();
		Random rnd = new Random();
		boolean nodeAccepted;
		do {
			nodeAccepted = true;
			this.p = new Point(rnd.nextInt(Constants.MAX_COORD), rnd.nextInt(Constants.MAX_COORD));
			if (nodes != null && nodes.length > 0) {
				for (Node node: nodes) {
					if (node != null && node.getPoint().getX() == p.getX() && node.getPoint().getY() == p.getY()) {
						nodeAccepted = false;
						break;
					}
				}
			}
		} while (!nodeAccepted);
		this.isDest = isDest;
		orderedNeighbours = new ArrayList<Node>();
		incidentEdges = new ArrayList<Edge>();
		this.id = id;
	}
	
    public ArrayList<Edge> getIncidentEdges() {
        return incidentEdges;
    }

    public Edge getLinkToNeighbour(Node neighbour) {
        for (Edge e: incidentEdges) {
            if (e.getV().equals(neighbour)) {
                return e;
            }
        }
        return null;
    }    
	
	public int getId() {
		return id;
	}
		
	public boolean isDestination() {
		return isDest;
	}
	
	public Point getPoint() {
		return p;
	}
	
    public void addAdjacentEdge(Edge e) {  //maybe delete (one of the two)
        if (e.getCost() == 0) {
            System.err.println("No cost defined!");
        }
        if (!e.getU().equals(this)) {
            System.err.println("Addition of an incorrect edge");
        }
        incidentEdges.add(e);
        determineLevels();
    }
    
    public Node getNeighbour(int i) {
        return incidentEdges.get(i).getV();
    }
	
    public void removeNeighbour(Node v) {
        for (Iterator<Edge> e_iter = incidentEdges.iterator(); e_iter.hasNext(); ) {
            Edge e = e_iter.next();
            if (e.getV().equals(v)) {
                e_iter.remove();
                determineLevels();
                return;
            }
        }
    }
    
    public int getDegree() {
        return incidentEdges.size();
    }
    
    public Edge getJ1() {
        return j1;
    }

    public Edge getJ2() {
        return j2;
    }    
    
    public float getLevel1() {
        return j1.getCost();
    }

    public float getLevel2() {
        return j2 == null ? 0 : j2.getCost();
    }    
    
    public void determineLevels() {
        if (incidentEdges == null || incidentEdges.isEmpty()) {
            j1 = null;
            return;
        }
        j1 = incidentEdges.get(0);
        j2 = incidentEdges.get(0);
        for (Edge e: incidentEdges) {
            if (e.getCost() >= j1.getCost()) {
                j1 = e;
            }
        }
        if (incidentEdges.size() > 1) {
            if (j1.equals(j2)) {
                j2 = incidentEdges.get(1);
            }
            for (Edge e: incidentEdges) {
                if (e.getCost() >= j2.getCost() && e.getCost() <= j1.getCost() && !j1.equals(e)) {
                    j2 = e;
                }
            }
        }
        else {
            j2 = null;
        }
    }    
    
    public void clearLevels() {
        j1 = null;
        j2 = null;
    }
    
    public float getCost(int numOfDests) {
        int subTreeSize = getJ1().getSubtreeSize();
        float val = getJ1().getSubtreeSize() * getLevel1() + (numOfDests - subTreeSize) * getLevel2();
        return val;
    }
    
	public void orderNeighbours(Node[] allNodes) {
		orderedNeighbours = new ArrayList<>(allNodes.length - 1);
		for (Node node: allNodes) {
			if (!node.equals(this)) {				
				float dst = Miscellaneous.dst(this.p, node.getPoint());
				boolean inserted = false;
				for (Node nb: orderedNeighbours) {
					if (dst < Miscellaneous.dst(this.p, nb.getPoint())) {
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
    @Override
    public Object clone() throws CloneNotSupportedException {
        return new Node(id, p, isDest);
    }


}
