package smt;

import graph.Clique;
import graph.ExtendedGraph;
import graph.ExtendedNode;
import graph.Graph;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFrame;
import org.javatuples.Pair;
import model.CliqueModel;
import model.ILPModel;
import model.MEBModel;
import model.MEBModelLP;
import model.SMTModel;
import model.SMTModelLP;

public class App {
	
    int vertexCount = 15;
    int dstCount = 15;
    private ILPModel model;
    Graph graph;
    private boolean draw = false;
    
    private boolean generate = true;
    private boolean allowCrossing = true;
    
	public int run() {
		int iter = 10;
		ArrayList<Integer> crossList = new ArrayList<Integer>();
		
		for (int i = 0; i < iter; i++) {
			ArrayList<Clique> cliqueList = new ArrayList<Clique>();
			if (generate) {
				graph = new Graph(vertexCount, dstCount);			
			}
			else {
				graph = new Graph("saved_inst/cyclic2.txt"); // from file, todo
			}	
			graph.saveInstance();
			graph.generateAMPLData();
			model = new SMTModelLP(graph, allowCrossing);
			model.solve(); // obtain z value
			double lpCost1 = model.getObjectiveValue();
			Double[][] z = (Double[][]) model.getZVar();
			if (hasCrossing(z)) {
				crossList.add(graph.getInstId());
			}
			drawSolution(z);
			CliqueModel cliqueModel = new CliqueModel(graph, z);
			cliqueModel.getExtGraph().generateAMPLData();  // create AMPL model for clique in the extended graph
			Clique clique;
			do {
				cliqueModel.solve();
				Boolean[] clVar = cliqueModel.getCliqueVar();
				clique = cliqueModel.getExtGraph().getSelectedExtendedNodes(clVar, cliqueModel.getObjectiveValue()); 
				cliqueList.add(clique);  // add new clique to the list of cliques
				cliqueModel.addClConstraint(clique);
			} while (clique.size() > 1);
			cliqueModel.end();
			listCliques(cliqueList);
			model.addCrossCliqueConstraints(cliqueList);
			model.solve();
			double lpCost2 = model.getObjectiveValue();
			logObjectives(lpCost1, lpCost2, cliqueList);
			model.end();
			System.err.println("Instances with crossing: ");
			for (Integer c: crossList) {
				System.err.println(c + "");	
			}	
		}			
		return 0;
	}
	
	private void listCliques(ArrayList<Clique> cliqueList) {
		System.out.println("--------------CLIQUES--------------");
		for (Clique clique: cliqueList) {
			System.out.print("(");
			for (ExtendedNode en: clique) {
				System.out.print(" " + en.getId());
			}
			System.out.println(" ) weight: " + clique.getWeight());
		}
	}

	private void drawSolution(Double[][] z) {
		if (draw) {
			if (model instanceof MEBModelLP) {
				draw(z, graph, true);						
			}
			else {
				draw(z, graph, false);
			}					
		}
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
	
	private void logObjectives(double lpCost1, double lpCost2, ArrayList<Clique> cliqueList) {
        try	{
        	File datafile = new File("logs/log.txt");
        	FileWriter fw = new FileWriter(datafile,true); //the true will append the new data
            fw.write(Math.round(lpCost2 - lpCost1) + "\t" +lpCost1 + "\t" + lpCost2 + "\t |"+ model.toString() + "|  Cliques: ");
            for (Clique c: cliqueList) {
            	fw.write(c.toString() + " ");
            }
            fw.write("\n");
            fw.close();
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
	}
	
	private void logHead(File datafile, FileWriter fw) {
		try {
			fw.write(model.toString() + ": " + graph.getVertexCount() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
}
