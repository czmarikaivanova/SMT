package graph;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.javatuples.Quartet;

import smt.Constants;
import smt.Miscellaneous;

public class Graph {
	
	protected int vertexCount;
	private int dstCount;	
	private int instId;
	private Node[] nodes;
	private float[][] requir;	
	private ArrayList<Quartet<Node, Node, Node, Node>> crossList;
	
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
    		nodes[i] = new Node(i);
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
	 						nodes[cnt] = new Node(i, new Point(Math.round(Float.parseFloat(twoParts[0])), Math.round(Float.parseFloat(twoParts[1]))), true);
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
    
    private String genCrossing() {
    	String crossStr = "";
    	for (int i = 0; i < getVertexCount(); i++) {
    		for (int j = i+1; j < getVertexCount(); j++) {
    	    	for (int k = i+1; k < getVertexCount(); k++) {
    	    		for (int l = k+1; l < getVertexCount(); l++) {
    	    			if (i!=k && i!=l && j!=k && j!=l) {
							if (Miscellaneous.edgesProperlyIntersect(getNode(i).getPoint(), 
									getNode(j).getPoint(), 
									getNode(k).getPoint(), 
									getNode(l).getPoint())) {
								crossStr += "(" + i + "," + j + "," + k + "," + l + ") ";
							}    	    				
    	    			}
    	    		}
    	    	}    			
    		}
    	}
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


}
