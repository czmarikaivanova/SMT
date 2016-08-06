package smt;

import graph.ExtendedGraph;
import graph.Graph;

import java.util.ArrayList;
import javax.swing.JFrame;

import model.CliqueModel;
import model.ILPModel;
import model.MEBModel;
import model.SMTModel;
import model.SMTModelLP;
public class App {
	
    int vertexCount = 15;
    int dstCount = 15;
 //   int nodeCount =12;    
    private ILPModel model;
    Graph graph;
    
	public int run() {
		int iter = 1;
		ArrayList<Integer> crossList = new ArrayList<Integer>();
		boolean generate = true;
		boolean draw = true;
		for (int i = 0; i < iter; i++) {
			if (generate) {
				graph = new Graph(vertexCount, dstCount);			
			}
			else {
				graph = new Graph("instances/crossing.txt"); // from file, todo
			}	
			graph.saveInstance();
			graph.generateAMPLData();
			
			
			model = new MEBModel(graph, false);
			model.solve();
			Double[][] z = (Double[][]) model.getZVar();
			if (hasCrossing(z)) {
				crossList.add(graph.getInstId());
			}
			if (draw) {
				if (model instanceof MEBModel) {
					draw(z, graph, true);						
				}
				else {
					draw(z, graph, false);
				}					
			}			
			CliqueModel cliqueModel = new CliqueModel(graph, z);
			cliqueModel.solve();
			Boolean[] clVar = cliqueModel.getCliqueVar();
			System.err.println("Instances with crossing: ");
			for (Integer c: crossList) {
				System.err.println(c + "");	
			}	
		}			
		return 0;
	}
	
    private boolean hasCrossing(Double[][] z) {
		for (int i = 0; i < z.length; i++) {
			for (int j = 0; j < z[i].length; j++) {
				if ((z[i][j] != null) && (z[i][j] > 0)) {
					for (int k = i + 1; k <z.length; k++) {
						for (int l = k + 1; l < z[k].length; l++) {
							if (z[k][l] > 0) {
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

	private void draw(Double[][] z, Graph graph, boolean useArrows) {
		Visualizer vis = new Visualizer(z, graph, useArrows);
		//Visualizer vis = new Visualizer(new File("instance.txt"), z, null);			
        JFrame frame = new JFrame("ID: " + graph.getInstId());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(vis);
        frame.setSize(Constants.WINDOW_SIZE, Constants.WINDOW_SIZE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }		
    



    	
}
