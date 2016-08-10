package graph;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.javatuples.Pair;

import smt.Constants;
import smt.Miscellaneous;

public class ExtendedGraph extends Graph {

	private Graph origGraph;
	private Double[][] z;
	
	ExtendedNode[] nodes;
	ArrayList<Pair<ExtendedNode, ExtendedNode>> edges;
	

	public ExtendedGraph(Graph origGraph, Double[][] z) {
		super();
		this.origGraph = origGraph;
		this.z = z;
		createNodes();
		createEdges();
	}
	
	private void createNodes() {
		vertexCount = (int) (0.5 * origGraph.getVertexCount() * (origGraph.getVertexCount() - 1)); // always int as n(n-1) is always even
		nodes = new ExtendedNode[vertexCount];
		int cnt = 0;
		for (int i = 0; i < origGraph.getVertexCount(); i++) {
			for (int j = i + 1; j < origGraph.getVertexCount(); j++) {
				if (z[j][i] != null) {
					nodes[cnt] = new ExtendedNode(origGraph.getNode(i), origGraph.getNode(j), z[i][j] + z[j][i], cnt);
				}
				else {
					nodes[cnt] = new ExtendedNode(origGraph.getNode(i), origGraph.getNode(j), z[i][j], cnt);
				}
				cnt++;
			}
		}
		System.err.println("cnt: " + cnt);
	}
	
	private void createEdges() {
		edges = new ArrayList<Pair<ExtendedNode, ExtendedNode>>();
		for (int i = 0; i < nodes.length; i++) {
			for (int j = i+1; j < nodes.length; j++) {
				if (Miscellaneous.edgesProperlyIntersect(nodes[i].getOrigU().getPoint(), 
						 nodes[i].getOrigV().getPoint(), 
						 nodes[j].getOrigU().getPoint(), 
						 nodes[j].getOrigV().getPoint())) {
					edges.add(new Pair<ExtendedNode, ExtendedNode>(nodes[i], nodes[j]));
					}	
			}			
		}
	}
	
	public ExtendedNode getNode(int i) {
		return nodes[i];
	}
	
	public boolean containsEdge(int i, int j) {
		for (Pair<ExtendedNode, ExtendedNode> e: edges) {
			if ((e.getValue0().getId() == i) && (e.getValue1().getId() == j)) {
				return true;
			}
		}
		return false;
	}
	
	public void writeDebug() {
		System.out.println("----------------");
		System.out.println("Vertices created: ");
		for (ExtendedNode node: nodes) {
			System.out.println("ID: " + node.getId() + " Originals: (" + node.getOrigU().getId() +", " + node.getOrigV().getId() + " ) " +" weight: " + node.getWeight());
		}
		System.out.println("----------------");
//		System.out.println("Edges created: ");
//		for (Pair<ExtendedNode, ExtendedNode> pair : edges) {
//			System.out.println("(" + pair.getValue0().getId() + ", " + pair.getValue1().getId() + ")");
//		}
	}
	
	public Clique getSelectedExtendedNodes(Boolean[] boolArr, double obj) {
		Clique extNodeList = new Clique(obj);
		if (boolArr != null) {
			for (int i = 0; i < boolArr.length; i++) {
				if (boolArr[i]) {
					extNodeList.add(this.getNode(i));
				}
			}
		}
		return extNodeList;
	}
	
	// also creates the file !
    public File generateAMPLData() {
        try
        {
        	File datafile = new File("amplfiles/cl_ampl" +  new File("amplfiles/").list().length + ".dat");
            System.out.println("Saving: AMPL input");
            FileWriter fw = new FileWriter(datafile,false); //the true will append the new data
            fw.write(Constants.INST_ID + getInstId() + "\n");
            String head = genAmplHead();
            String paramStr = "param weights :=\n";
            String weightStr = "";
            for (int i = 0; i < getVertexCount(); i++) {
                    weightStr += " " +i + " " + getNode(i).getWeight() + "\n"; 
            }
            fw.write(head);
            fw.write(paramStr);
            fw.write(weightStr + ";");
            fw.close();
            return datafile;
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
            return null;
        } 
    }		
    
	protected String genAmplHead() {
        String dstStr = "set VERTICES :=";
        String edgeStr = "set EDGES :=";
        for (int i = 0; i < getVertexCount(); i++) {
            dstStr += " " + i ;
        }
        for (Pair<ExtendedNode, ExtendedNode> edge: edges) {
        	edgeStr += " (" + edge.getValue0().getId() + "," + edge.getValue1().getId() + ")";
        }
        dstStr += " ;\n";
        edgeStr += " ;\n";
        return dstStr + edgeStr;
    }	

}
