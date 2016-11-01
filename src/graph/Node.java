package graph;

import java.awt.Point;
import java.util.ArrayList;
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
	
	public Node(int id) {
		super();
		Random rnd = new Random();		
		this.p = new Point(rnd.nextInt(Constants.MAX_COORD), rnd.nextInt(Constants.MAX_COORD));
		this.isDest = true;
		orderedNeighbours = new ArrayList<Node>();
		incidentEdges = new ArrayList<Edge>();
		this.id = id;
	}
	
	public void addAdjacent(Node n, Graph g) {  //maybe delete (one of the two)
		incidentEdges.add(new Edge(this, n, g.getRequir(this, n)));
        determineLevels();
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
	
    public void addNeighbour(Edge e) {  //maybe delete (one of the two)
        if (e.getCost() == 0) {
            System.err.println("No cost defined!");
        }
        if (!e.getU().equals(this)) {
            System.err.println("Addition of an incorrect edge");
        }
        incidentEdges.add(e);
        determineLevels();

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
