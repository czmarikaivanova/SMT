package smt;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import javax.swing.JFrame;
import model.ILPModel;
import model.MEBModel;
import model.SMTModel;
public class App {
	
    int vertexCount = 5;
    int dstCount = 5;
 //   int nodeCount =12;    
    private ILPModel model;
    Graph graph;
    
	public int run() {
		int iter = 1;
		ArrayList<Integer> crossList = new ArrayList<Integer>();
		boolean generate = false;
		boolean draw = true;
		for (int i = 0; i < iter; i++) {
			if (generate) {
				graph = new Graph(vertexCount, dstCount);			
			}
			else {
				graph = new Graph("instances/ugly.txt"); // from file, todo
			}	
			graph.saveInstance();
			model = new MEBModel(graph);
			model.solve();
			boolean[][] z = model.getZVar();
			if (hasCrossing(z)) {
				crossList.add(graph.getInstId());
			}
			if (draw) {
				if (model instanceof MEBModel) {
					draw(z, graph.getInstId(), true);						
				}
				else {
					draw(z, graph.getInstId(), false);
				}					
			}			
			System.err.println("Instances with crossing: ");
			for (Integer c: crossList) {
				System.err.println(c + "");	
			}	
		}			
		return 0;
	}
	
    private boolean hasCrossing(boolean[][] z) {
		for (int i = 0; i < z.length; i++) {
			for (int j = 0; j < z[i].length; j++) {
				if (z[i][j]) {
					for (int k = i + 1; k <z.length; k++) {
						for (int l = j + 1; l < z[k].length; l++) {
							if (z[k][l]) {
								if (Miscellaneous.edgesProperlyIntersect(graph.getNode(i).getPoint(), 
															graph.getNode(j).getPoint(), 
															graph.getNode(k).getPoint(), 
															graph.getNode(l).getPoint())) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private void draw(boolean[][] z, int instId, boolean useArrows) {
		Visualizer vis = new Visualizer(z, graph, dstCount, useArrows);
		//Visualizer vis = new Visualizer(new File("instance.txt"), z, null);			
        JFrame frame = new JFrame("ID: " + instId);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(vis);
        frame.setSize(Visualizer.WINDOW_SIZE, Visualizer.WINDOW_SIZE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }		
    



    	
}
