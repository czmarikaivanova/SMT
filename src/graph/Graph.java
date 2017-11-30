package graph;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.javatuples.Quartet;

import smt.Constants;
import smt.Miscellaneous;

public class Graph implements Cloneable  {
	
	protected int vertexCount;
	private int dstCount;	
	private int instId;
	protected Node[] nodes;
	private float[][] requir;	
	private ArrayList<Quartet<Node, Node, Node, Node>> crossList;
	private float cost;
	
	public Graph(int vertexCount, int dstCount) {
		this.vertexCount = vertexCount;
		this.dstCount = dstCount;
		Random rnd = new Random();
		instId = rnd.nextInt(100000);
		generatePoints();		
		calculateDistances();
	}
	

	public Graph(String instanceFilePath) { // todo
		instId = -1;
		createPoints(instanceFilePath);
		calculateDistances();
	}
	
	// just for the sake of the inherited ExtendedGraph
    public Graph() {
    	
	}

	private void generatePoints() {
    	nodes = new Node[vertexCount];
    	for (int i = 0; i < vertexCount; i++) {
    		nodes[i] = new Node(i, i < dstCount, nodes);
    	}
    	orderNeighbours();
    }    
    
	private void createPoints(String instanceFilePath) {
    	try {
			BufferedReader br = new BufferedReader(new FileReader(instanceFilePath));
			String line;
			int cnt = 0;
			try {
				while ((line = br.readLine()) != null) {
					if (line.equals(Constants.DELIMITER)) {
						vertexCount = Integer.parseInt(br.readLine());
						dstCount = Integer.parseInt(br.readLine());
						nodes = new Node[vertexCount];
						String[] twoParts;
						for (int i = 0; i < vertexCount; i++) {
							twoParts = br.readLine().split(" ");	 																	
	 						nodes[cnt] = new Node(i, new Point(Math.round(Float.parseFloat(twoParts[0])), Math.round(Float.parseFloat(twoParts[1]))), i < dstCount);
	 						cnt++; // WHY?
						}
					}
				}	
				orderNeighbours();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }	
    
	private void calculateDistances() {
		requir = new float[vertexCount][vertexCount];
		for (int i = 0; i < vertexCount; i++) {
			for (int j = 0; j < vertexCount; j++) {
				float dst = Miscellaneous.dst(nodes[i].getPoint(), nodes[j].getPoint());
				requir[i][j] = dst;
			}
		}
		
	}    
    private void orderNeighbours() {
    	for (Node n: nodes) {
    		n.orderNeighbours(nodes);
    	}
//    	writeDebug();    	
    }
    
    /**
     * Add poth arcs
     * @param edge Edge to add
     */
    public void addEdge(Edge edge) {
        edge.getU().addAdjacentEdge(edge);
        Edge edge2 = new Edge(edge.getV(), edge.getU(), edge.getCost());
        edge.getV().addAdjacentEdge(edge2);
    } 
    
    private void writeDebug() {
		for (int i = 0; i < nodes.length; i++) {
			System.out.print(i + ": (");
			for (Node nb : nodes[i].orderedNeighbours) {
				System.out.print(" " + nb.getId());
			}
			System.out.println();
		}		
	}    
    
	public void saveInstance(		) {
		File instFile = new File("instances/instance" +  new File("instances/").list().length + ".txt");
        try
        {
            System.out.println("Saving: instance");
            FileWriter fw = new FileWriter(instFile,true); //the true will append the new data
            fw.write("\n");
            fw.write(Constants.INST_ID + instId + "\n");
            fw.write(Constants.DELIMITER + "\n");//appends the string to the file
            fw.write(vertexCount + "\n");//appends the string to the file
            fw.write(dstCount + "\n");//appends the string to the file
            for (Node v: nodes) {
                fw.write(v.getPoint().getX() + " " + v.getPoint().getY());
                fw.write("\n");
            }
            fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }   					
	}    
	
	// also creates the file !
    public File generateAMPLData() {
        try
        {
        	File datafile = new File("amplfiles/ampl" +  new File("amplfiles/").list().length + ".dat");
            System.out.println("Saving: AMPL input");
            FileWriter fw = new FileWriter(datafile,false); //the true will append the new data
            fw.write(Constants.INST_ID + getInstId() + "\n");
            String head = genAmplHead();
            String crossStr = genCrossing();
            String paramStr = "param requir :=\n";
            String distancesStr = "";
            for (int i = 0; i < getVertexCount(); i++) {
                for (int j = 0; j < getVertexCount(); j++) {
                    distancesStr += " " +i + " " + j + " " + Miscellaneous.dst(getNode(i).getPoint(), getNode(j).getPoint()) + "\t"; 
                }
                distancesStr += "\n";
            }
            fw.write(head);
            fw.write(crossStr);
            fw.write(paramStr);
            fw.write(distancesStr + ";");
            fw.close();
            return datafile;
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
            return null;
        } 
    }	 
    
    // get a string list of crossing edges + generate DS for crossing
    private String genCrossing() {
    	String crossStr = "";
//    	crossList = new ArrayList<Quartet<Node,Node,Node,Node>>();
//    	for (int i = 0; i < getVertexCount(); i++) {
//    		for (int j = i+1; j < getVertexCount(); j++) {
//    	    	for (int k = i+1; k < getVertexCount(); k++) {
//    	    		for (int l = k+1; l < getVertexCount(); l++) {
//    	    			if (i!=k && i!=l && j!=k && j!=l) {
//							if (Miscellaneous.edgesProperlyIntersect(getNode(i).getPoint(), 
//									getNode(j).getPoint(), 
//									getNode(k).getPoint(), 
//									getNode(l).getPoint())) {
//								crossStr += "(" + i + "," + j + "," + k + "," + l + ") ";
//								crossList.add(new Quartet<Node, Node, Node, Node>(getNode(i), getNode(j), getNode(k), getNode(l)));
//							}    	    
//							
//    	    			}
//    	    		}
//    	    	}    			
//    		}
//    	}
    	return  "set CROSS := " + crossStr + ";\n"; 
    }

	protected String genAmplHead() {
        String dstStr = "set DESTS :=";
        String nonDstStr = "set NONDESTS :=";
        for (int i = 0; i < getVertexCount(); i++) {
            if (i < getDstCount()) {
                dstStr += " " + i ;
            } else {
                nonDstStr += " " + i;
            }
        }
        dstStr += " ;\n";
        nonDstStr += " ;\n";
        return dstStr + nonDstStr;
    }	
	
	public int getInstId() {
		return instId;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public int getDstCount() {
		return dstCount;
	}

	public Node getNode(int i) {
		return nodes[i];
	}

	public float getRequir(int i, int j) {
		return requir[i][j];
	}
	
	public float getRequir(Node i, Node j) {
		return requir[i.getId()][j.getId()];
	}

	public ArrayList<Quartet<Node, Node, Node, Node>> getCrossList() {
		return crossList;
	}	
	
    //T(r1/r2)
    private int calcSubTrees(Edge e, int nod) {
        if (e == null) {
            System.out.println("e is null");
        }
        Node r1 = e.getU();
        Node r2 = e.getV();        
        int stSize = 0;
        if (r2.isDestination()) {
            stSize = 1;            
        }
        Edge opositeDir = r2.getLinkToNeighbour(r1);
        if (r2.getIncidentEdges().size() == 1) { // r2 has only its parent!
            opositeDir.setSubTreeSize(stSize);
            e.setSubTreeSize(nod - stSize);
            return stSize;
        } 
        for (Edge f: r2.getIncidentEdges()) {
            if (!f.getV().equals(r1)) {
                stSize += calcSubTrees(f, nod);
            }
        }
        opositeDir.setSubTreeSize(stSize);
        e.setSubTreeSize(nod - stSize);
        return stSize;
    }
    
    
    public float evaluate(int nod) {
        float myCost = 0;
        calcSubTrees(findSomeLeaf(), nod);
//        if (alreadyConnected != null) { // only GSMT
//            for (Vertex w: vertices) {
//                if (!alreadyConnected.contains(w)) {
//                    int i = 0;
//                    Vertex closestToW = w.getPotentialNeighbours().get(i).getV();
//                    while (!alreadyConnected.contains(closestToW)) {
//                        i++;
//                        closestToW = w.getPotentialNeighbours().get(i).getV();
//                    }
//
//                }
//            }
//        }
        for (Node v: nodes) {
            if (v.getJ1() != null) {
                myCost += v.getCost(nod);                
            }
        }
        this.cost = myCost;
        return myCost; 
    }
    
    // so far only with deg(v) = 2
    public float getCostWithout(Node v, Graph origGraph) {
        Node u = v.getNeighbour(0);
        Node w = v.getNeighbour(1);
        float dstUW = origGraph.getRequir(u, w);
        float w_l1 = w.getLevel1();
        float w_l2 = w.getLevel2();
        float u_l1 = u.getLevel1();
        float u_l2 = u.getLevel2();

        float orig_w = w.getCost(dstCount);
        float orig_u = u.getCost(dstCount);
        float orig_v = v.getCost(dstCount);
        
        float pot_w;
        float pot_u;
        
        if (dstUW >= w_l1) {
            int sts = u.getLinkToNeighbour(v).getSubtreeSize();
            if (w.getJ1().equals(w.getLinkToNeighbour(v))) {// j1 is being replaced 
                pot_w = sts * w_l2 + (dstCount - sts) * dstUW;                
            }
            else {
                pot_w = sts * w_l1 + (dstCount - sts) * dstUW;                
            }
        }
        else if ((dstUW < w_l1) && (dstUW > w_l2)) {
            int sts = w.getJ1().getSubtreeSize();
            pot_w = sts * w_l1 + (dstCount - sts) * dstUW;

        }
        else { // dstVW <w_12
            pot_w = orig_w;            
        }

        if (dstUW >= u_l1) {
            int sts = w.getLinkToNeighbour(v).getSubtreeSize();
            if (u.getJ1().equals(u.getLinkToNeighbour(v))) {
                pot_u = sts * u_l2 + (dstCount - sts) * dstUW;
            }
            else {
                pot_u = sts * u_l1 + (dstCount - sts) * dstUW;                
            }

        }
        else if ((dstUW < u_l1) && (dstUW > u_l2)) {
            int sts = u.getJ1().getSubtreeSize();
            pot_u = sts * u_l1 + (dstCount - sts) * dstUW;            
        }
        else { // dstVW <w_12
            pot_u = orig_u;            
        }
        
        return this.cost - orig_v - orig_u - orig_w + pot_u + pot_w;
    }    
    
    public Edge findSomeLeaf() {
        for (Node v: nodes) {
            if (v.getIncidentEdges().size() == 1) {
                return v.getIncidentEdges().get(0);
            }
        }
        return null;
    }	
	
    /**
     * Update the distance matrix after additin of an ege
     * 
     * @param edge added edge
     * @param origG original graph
     * @param sym should it be updated symmetrically (for v as well)
     */
    public void updateDstMatrix(Edge edge, Graph origG, boolean sym) {
        for (int i = 0; i < vertexCount; i++) {
            float newcost = origG.getRequir(edge.getU().getId(), i) - edge.getCost();                
            if (newcost < requir[edge.getU().getId()][i]) {
                requir[edge.getU().getId()][i] = Math.max(0, newcost);
            }
        }
        if (sym) {
            for (int i = 0; i < dstCount; i++) {
                if (edge.getV().getJ1().getCost() == edge.getCost()) {
                    float newcost = origG.getRequir(edge.getV().getId(), i) - edge.getCost();
                    requir[edge.getV().getId()][i] = Math.max(0, newcost);
                }
                else {
                    System.err.println("why am I here?");
                }
            }
        }
    }    
    
    @Override
    /**
     * Clone only points, not links. We do not need it.
     */
    public Object clone() throws CloneNotSupportedException {
        Graph gr = (Graph) super.clone();
        gr.requir = new float[vertexCount][vertexCount];
        gr.vertexCount = this.vertexCount;
        gr.dstCount = this.dstCount;
        for (int i = 0; i < vertexCount; i++) {
            System.arraycopy(requir[i], 0, gr.requir[i], 0, vertexCount);
        }
        gr.nodes = new Node[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
        	gr.nodes[i] = (Node) nodes[i].clone();
        }
//        for (Node v: nodes) {
//        	Node cv = gr.getNode(v.getId());
//            for (Edge e: v.getIncidentEdges()) {
//                Edge edgeToAdd = new Edge(gr.getNode(e.getU().getId()), gr.getNode(e.getV().getId()), e.getCost());
//                cv.addNeighbour(edgeToAdd);
//            }
//        }
        return gr;
    }


    /**
     * 
     * @return  graph's xml representation
     */
	public String getXMLString() {
		String xmlstr = "";
		xmlstr += "<graph>\n";
		for (Node n: nodes) {
			if (n.isDestination()) {
				xmlstr += "\t<dest id=\"" + n.getId() +"\" x=\"" + n.getPoint().x + "\" y=\"" +n.getPoint().y +"\"></dest>\n";
			}
		}
		xmlstr += "</graph>\n";
		return xmlstr;
	}
}
