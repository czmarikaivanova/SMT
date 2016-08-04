package graph;

import java.util.ArrayList;
import org.javatuples.Pair;
import smt.Miscellaneous;

public class ExtendedGraph extends Graph {

	private Graph origGraph;
	private Double[][] z;
	
	ExtendedNode[] nodes;
	ArrayList<Pair<ExtendedNode, ExtendedNode>> edges;
	int nodeCount;

	public ExtendedGraph(Graph origGraph, Double[][] z) {
		super();
		this.origGraph = origGraph;
		this.z = z;
		createNodes();
		createEdges();
	}
	
	private void createNodes() {
		nodeCount = (int) (0.5 * origGraph.getVertexCount() * (origGraph.getVertexCount() - 1)); // always int as n(n-1) is always even
		nodes = new ExtendedNode[nodeCount];
		int cnt = 0;
		for (int i = 0; i < origGraph.getVertexCount(); i++) {
			for (int j = i + 1; j < origGraph.getVertexCount(); j++) {
				nodes[cnt] = new ExtendedNode(origGraph.getNode(i), origGraph.getNode(j), z[i][j], cnt);
				cnt++;
			}
		}
		System.err.println("cnt: " + cnt);
	}
	
	private void createEdges() {
		edges = new ArrayList<Pair<ExtendedNode, ExtendedNode>>();
		for (ExtendedNode exU: nodes) {
			for (ExtendedNode exV: nodes) {
				if (!exU.equals(exV)) {
					if (Miscellaneous.edgesProperlyIntersect(exU.getOrigU().getPoint(), 
															 exU.getOrigV().getPoint(), 
															 exV.getOrigU().getPoint(), 
															 exV.getOrigV().getPoint())) {
						edges.add(new Pair<ExtendedNode, ExtendedNode>(exU, exV));
					}
				}
			}
		}
	}
	
	public void writeDebug() {
		System.out.println("----------------");
		System.out.println("Vertices created: ");
		for (ExtendedNode node: nodes) {
			System.out.println("ID: " + node.getId() + " weight: " + node.getWeight());
		}
		System.out.println("----------------");
		System.out.println("Edges created: ");
		for (Pair<ExtendedNode, ExtendedNode> pair : edges) {
			System.out.println("(" + pair.getValue0().getId() + ", " + pair.getValue1().getId() + ")");
			
		}
	}

}
