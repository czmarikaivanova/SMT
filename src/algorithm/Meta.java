package algorithm;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import smt.Constants;
import smt.Visualizer;
import graph.Edge;
import graph.Graph;
import graph.Node;

public class Meta extends Algorithm {
	
	private Algorithm constrAlg;
	private Graph[] pool;
	private int poolCnt;
	
	private static int iterM = 100;
	private static int poolCap = (int) Math.ceil(Math.sqrt(iterM/2));
	
	public Meta(boolean onlyDests) {
		super(onlyDests);
		this.onlyDests = onlyDests;
		poolCnt = 0;
		pool = new Graph[poolCap];
//		constrAlg = new MSTAlgorithm(onlyDests);
		constrAlg = new BIPAlgorithm(onlyDests, true);
		
	}

	@Override
	public Graph solve(Graph graph) {
		this.origGraph = graph;
		constrAlg.setOrigGraph(origGraph);
		for (int i = 0; i < iterM; i++) {
			Graph pertG = null;
			try {
				pertG = (Graph) graph.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			
			pertG.perturbDM();
			Graph resGraph = constrAlg.solve(pertG);			
			System.out.println("cost before perturbation: " + resGraph.evaluate(origGraph.getDstCount()));
//			Visualizer vis1 = new Visualizer(resGraph, resGraph, false, false, resGraph.toString());
//	        JFrame frame = new JFrame(i + " before");
//	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	        frame.add(vis1);
//	        frame.setSize(Constants.WINDOW_SIZE, Constants.WINDOW_SIZE);
//	        frame.setLocationRelativeTo(null);
//	        frame.setVisible(true);
			resGraph.restoreDM(origGraph);
			System.out.println("cost after perturbation: " + resGraph.evaluate(origGraph.getVertexCount()));
			tryToAddtoPool(resGraph);
			
		}
		writePoolCosts();
		return findBest();
	}

	private void tryToAddtoPool(Graph resGraph) {
		if (poolCnt < poolCap) {  // pool capacity is not filled
			System.out.println("adding because of free capacity");
			pool[poolCnt] = resGraph;			
			writePoolCosts();
			poolCnt++;
		} else  { 
			int wpei = worsePoolEntryIndex();
			float wpc = pool[wpei].evaluate(origGraph.getDstCount());
			if (wpc > resGraph.evaluate(resGraph.getDstCount())) { // the worst pool entry is worse than proposed solution
				System.out.println("New is better than the worse");	
				pool[wpei] = resGraph;
				writePoolCosts();
			}
			else {
				System.out.println("New is bad, try to combine");
				Graph rndPoolG = pool[new Random().nextInt(pool.length)];
				Graph combGraph = combine(resGraph, rndPoolG);
				resGraph.restoreDM(combGraph);
				if (combGraph.evaluate(origGraph.getDstCount()) < wpc) {
					System.out.println("Combined is better than the worse");
					pool[wpei] = combGraph;
					writePoolCosts();
				}
				else {
					System.out.println("Not even combined makes it");					
				}
				
			}
		}
		
	}

	private Graph combine(Graph resGraph, Graph rndPoolG) {
		Graph combinedG = null;
		try {
			combinedG = (Graph) origGraph.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < resGraph.getVertexCount(); i++) {
			for (int j = 0; j < resGraph.getVertexCount(); j++) {
				if (resGraph.containsEdge(i, j) && rndPoolG.containsEdge(i, j)) { // both graphs have the edge
					// do nothing, the cost remains unchanged
				} else if (resGraph.containsEdge(i, j) && !rndPoolG.containsEdge(i, j) || !resGraph.containsEdge(i, j) && rndPoolG.containsEdge(j, i)) {
					Random rnd = new Random();
					combinedG.setRequir(i, j, origGraph.getRequir(i, j) * (100 + rnd.nextInt(400)));
				} else { // none of the graphs has the edge
					combinedG.setRequir(i, j, origGraph.getRequir(i, j) * 1000);
				}
			}
		}		
		return constrAlg.solve(combinedG);
	}

	private int worsePoolEntryIndex() {
		float worstCost = 0;
		int idx = -1;
		for (int i = 0; i < pool.length; i++) {
			Graph g = pool[i];
			float costG = g.evaluate(origGraph.getDstCount());
			if (costG > worstCost) {
				worstCost = costG;
				idx = i;
			}
		}
		return idx;
	}

	private Graph findBest() {
		float bestCost = 100000000;
		
		Graph bestGraph = null;
		for (Graph g: pool) {

			float costG = g.evaluate(origGraph.getDstCount());
			if (costG < bestCost) {
				bestCost = costG;
				bestGraph = g;
			}			
		}
		System.out.println("best cost: " + bestCost + "graph: " + bestGraph.toString());
		return bestGraph;
	}

	@Override
	public String toString() {
		return "Meta";
	}

	private void writePoolCosts() {
		int i = 0;
		for (Graph g: pool) {
			if (g == null) return; 
			System.out.println("pool" + i + ": " + g.evaluate(origGraph.getDstCount()) + "graph " + g.toString());
			i++;
		}
	}
	
}
