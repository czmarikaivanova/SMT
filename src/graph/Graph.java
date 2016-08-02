package graph;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import smt.Constants;

public class Graph {
	
	private int vertexCount;
	private int dstCount;	
	private int instId;
	private Node[] nodes;
	
	public Graph(int vertexCount, int dstCount) {
		this.vertexCount = vertexCount;
		this.dstCount = dstCount;
		Random rnd = new Random();
		instId = rnd.nextInt(100000);
		generatePoints();		
	}
	
	public Graph(String instanceFilePath) { // todo
		instId = -1;
		createPoints(instanceFilePath);
		
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
    
    
    
    private void orderNeighbours() {
    	for (Node n: nodes) {
    		n.orderNeighbours(nodes);
    	}
    	writeDebug();    	
    }
    
    private void writeDebug() {
		for (int i = 0; i < nodes.length; i++) {
			System.out.print(i + ": (");
			for (Node nb : nodes[i].orderedNeighbours) {
				System.out.print(" " + nb.id);
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
}
