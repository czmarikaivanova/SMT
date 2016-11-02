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
import algorithm.Algorithm;
import algorithm.BIPAlgorithm;
import algorithm.MSTAlgorithm;
import model.CliqueModel;
import model.ILPModel;
import model.MEBModel;
import model.MEBModelLP;
import model.MultiFlow;
import model.SMTFlowModel;
import model.SMTFlowModelLP;
import model.SMTModel;
import model.SMTModelLP;
import model.SMTMultiFlowModel;
import model.SMTMultiFlowModelLP;
import model.SMTMultiFlowModelVI;
import model.SMTMultiFlowModelVILP;
import model.SMTOnlyFlow;
import model.SMTOnlyFlowLP;

public class App {
	
    int vertexCount = 7;
    int dstCount = 7;
    Graph graph;
    private boolean draw = true;
    private boolean generate = true;
    private boolean allowCrossing = true;
    
	public int run() {
		int iter = 1;

		
		for (int i = 0; i < iter; i++) {
			ArrayList<Clique> cliqueList = new ArrayList<Clique>();
			if (generate) {
				graph = new Graph(vertexCount, dstCount);			
			}
			else {
				graph = new Graph("saved_inst/weird.txt"); // from file, todo
			}	
			graph.saveInstance();
			graph.generateAMPLData();
			ILPModel smt = new SMTModel(graph, allowCrossing);
			ILPModel smtlp = new SMTModelLP(graph, allowCrossing);
			ILPModel smtViLp = new SMTMultiFlowModelVILP(graph, allowCrossing);
			
			Algorithm mst = new MSTAlgorithm(true);
			Algorithm bip = new BIPAlgorithm(true);
			
			double c_smt= runModel(smt,  true);
			double c_smtlp= runModel(smtlp,  true);
			double c_smtvilp= runModel(smtViLp,  true);
			double ca1 = runAlg(new MSTAlgorithm(true));
			double ca2 = runAlg(new BIPAlgorithm(true));
			System.out.println("Cost of " + smt.toString() + " is: " + c_smt);
			System.out.println("Cost of " + smtlp.toString() + " is: " + c_smtlp);
			System.out.println("Cost of " + smtViLp.toString() + " is: " + c_smtvilp);
			System.out.println("Cost of " + mst.toString() + " is: " + ca1);
			System.out.println("Cost of " + bip.toString() + " is: " + ca2);
		}			
		return 0;
	}
	
	private double runAlg(Algorithm alg) {
		Graph tree = alg.solve(graph);
		draw(tree, graph.getInstId(), alg.toString(), false);
		return tree.evaluate(dstCount);
	}
	
	private double runModel(ILPModel model, boolean newline) {
		ArrayList<Integer> crossList = new ArrayList<Integer>();
		model.solveAndLogTime(true, false, newline, graph.getInstId()); // obtain z value
		double lpCost1 = model.getObjectiveValue();
		Double[][] z = (Double[][]) model.getZVar();
//		if (hasCrossing(z)) {
//			crossList.add(graph.getInstId());
//		}
		draw(z, graph.getInstId(), model.toString(), model instanceof MEBModel);
//		CliqueModel cliqueModel = new CliqueModel(graph, z);
//		cliqueModel.getExtGraph().generateAMPLData();  // create AMPL model for clique in the extended graph
//		Clique clique;
//		do {
//			cliqueModel.solveAndLogTime();
//			Boolean[] clVar = cliqueModel.getCliqueVar();
//			clique = cliqueModel.getExtGraph().getSelectedExtendedNodes(clVar, cliqueModel.getObjectiveValue()); 
//			cliqueList.add(clique);  // add new clique to the list of cliques
//			cliqueModel.addClConstraint(clique);
//		} while (clique.size() > 1);
//		cliqueModel.end();
//		listCliques(cliqueList);
//		
//		model = new SMTModel(graph, allowCrossing);
//		model.addCrossCliqueConstraints(cliqueList);
//		model.solveAndLogTime();
		model.end();
		System.err.println("Instances with crossing: ");
		for (Integer c: crossList) {
			System.err.println(c + "");	
		}	
		return lpCost1;
	}
	
	private void compareVars(Double[][][][] xv1, Double[][][][] xv2) {
		for (int i = 0; i < xv2.length; i++) {
			for (int j = 0; j < xv2.length; j++) {
				if (i != j) {
					for (int k = 0; k < xv2.length; k++) {
						for (int l = 0; l < xv2.length; l++) {
							if (k != l) {
								Double v1 = Miscellaneous.round(xv1[i][j][k][l],2);
								Double v2 = Miscellaneous.round(xv2[i][j][k][l],2);
								if (!v1.equals(v2)) {
									System.out.println("" + i + " " + j + " " + k + " " + l + ": ");
									System.err.println("val1: " + xv1[i][j][k][l]);
									System.err.println("val2: " + xv2[i][j][k][l]);
//									System.exit(0);
								}		
							}
						}
					}
				}
			}
		}
		
	}

	
	private void compareVarsX(Double[][][] xv1, Double[][][] xv2) {
		for (int i = 0; i < xv2.length; i++) {
			for (int j = 0; j < xv2.length; j++) {
				if (i != j) {
					for (int k = 0; k < xv2.length; k++) {
						Double v1 = Miscellaneous.round(xv1[i][j][k],2);
						Double v2 = Miscellaneous.round(xv2[i][j][k],2);
						if (!v1.equals(v2)) {
							System.out.println("" + i + " " + j + " " + k + " " + ": ");
							System.err.println("val1: " + xv1[i][j][k]);
							System.err.println("val2: " + xv2[i][j][k]);
//									System.exit(0);
						}		
					}
				}
			}
		}
		
	}	
	private void listCliques(ArrayList<Clique> cliqueList) {
		System.out.println("--------------CLIQUES--------------");
		for (Clique clique: cliqueList) {
			System.out.print("(");
			System.out.print(clique.toString());
			System.out.println(" ) weight: " + clique.getWeight());
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
    
	private void draw(Object solution, int instId, String methodName, boolean useArrows) {
		if (draw) {
			Visualizer vis = new Visualizer(solution, graph, useArrows);
			//Visualizer vis = new Visualizer(new File("instance.txt"), z, null);			
	        JFrame frame = new JFrame(methodName + " ID: " + instId);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.add(vis);
	        frame.setSize(Constants.WINDOW_SIZE, Constants.WINDOW_SIZE);
	        frame.setLocationRelativeTo(null);
	        frame.setVisible(true);
		}
    }		
	
	private void logObjectives(double lpCost1, double lpCost2, double ipCost1, double ipCost2, ArrayList<Clique> cliqueList, ILPModel model) {
        try	{
        	File datafile = new File("logs/log.txt");
        	FileWriter fw = new FileWriter(datafile,true); //the true will append the new data
            fw.write(Miscellaneous.round(lpCost2 - lpCost1,2) + "\t" + 
            		 Miscellaneous.round(lpCost1,2) + "\t" + 
            		 Miscellaneous.round(lpCost2,2) + "\t "+ 
            		 Miscellaneous.round(ipCost1, 2) + "\t" + 
            		 Miscellaneous.round(ipCost2,2) + "\t | " + model.toString() + "|  Cliques: ");
            for (Clique c: cliqueList) {
            	fw.write(c.toString() + " ");
            }
            fw.write("\n");
            fw.close();
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
	}
	
	private void logHead(File datafile, FileWriter fw, ILPModel model) {
		try {
			fw.write(model.toString() + ": " + graph.getVertexCount() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
}
