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
import model.MultiFlow;
import model.SMTFlowModel;
import model.SMTModel;
import model.SMTMultiFlowModel;
import model.SMTMultiFlowModelVI;
import model.SMTOnlyFlow;
import model.SteinerModel;
import model.SteinerMultiFlowModel;
import model.SteinerPF2Model;
import model.SteinerPF2Relaxed;

public class App {
	
    int vertexCount = 10;
    int dstCount = 5;
    Graph graph;
    private boolean draw = false;
    private boolean generate = false;
    private boolean allowCrossing = true;
    
	public int run() {
		int iter = 1;
//		for (dstCount = 4; dstCount < 11; dstCount++) {
//			for (int vertexCount = dstCount + 1; vertexCount < 21; vertexCount++) {
				double avgLP1Cost;
				double avgLP2Cost;	
				double avgCost;
				double avgAlgCost;
				double sumLP1Cost = 0;
				double sumLP2Cost = 0;
				double sumCost = 0;
				double sumAlgCost = 0;
				for (int i = 0; i < iter; i++) {
					ArrayList<Clique> cliqueList = new ArrayList<Clique>();
					if (generate) {
						graph = new Graph(vertexCount, dstCount);			
					}
					else {
						graph = new Graph("saved_inst/fracMF.txt"); // from file, todo
					}	
					graph.saveInstance();
					graph.generateAMPLData();
					ILPModel smt = new SMTModel(graph, false, Constants.INTEGER, false);
					ILPModel smt_lazy = new SMTModel(graph, false, Constants.INTEGER, false);
					ILPModel smtlp = new SMTModel(graph, false, Constants.LP, false);
					ILPModel smtViLp1 = new SMTMultiFlowModel(graph, false, Constants.LP, false);
					ILPModel smtViLp2 = new SMTMultiFlowModel(graph, true, Constants.LP, false);
					
//					ILPModel steiner_int = new SteinerModel(graph, false, Constants.INTEGER, false);
					ILPModel steinerlp = new SteinerModel(graph, false, Constants.LP, false);
//					ILPModel steinerpf2lp = new SteinerPF2Model(graph, false, Constants.LP, false);
//					ILPModel steinerpf2 = new SteinerPF2Model(graph, false, Constants.LP, false);
//					ILPModel steinerpf2rel = new SteinerPF2Relaxed(graph, false, Constants.LP, false);
					ILPModel steinermf = new SteinerMultiFlowModel(graph, false, Constants.INTEGER, false);
					ILPModel steinermflp = new SteinerMultiFlowModel(graph, false, Constants.LP, false);
					
//					Algorithm bip = new BIPAlgorithm(true, true);
					Algorithm bipmulti = new BIPAlgorithm(false, true);
					
					
//
//					double c_smtlp= runModel(smtlp,  true);
//					double c_smtvilp= runModel(smtViLp1,  true);
//					double ca1 = runAlg(bip);
//					double ca2 = runAlg(bipmulti);
					
//					
//					double c_steiner_int = runModel(steiner_int, true);
//					double c_steinerlp = runModel(steinerlp, true);
//					double c_steinerpf2 = runModel(steinerpf2, true);
//					double c_steinerpf2rel = runModel(steinerpf2rel, true);
//					double c_steinermf = runModel(steinermf, true);
//					double c_steinerpf2 = runModel(steinerpf2, true);
					double c_steinermflp = runModel(steinermflp, true);

//					double LP0Cost = runModel(smtlp, false);
//					double LP1Cost = runModel(smtViLp1, false);
//					double LP2Cost = runModel(smtViLp2, false);
//					double cost = runModel(smt, false);
//					double cost_lazy = runModel(smt_lazy, false);
//					double cost2 = runModel(smtViLp2, false);
//					double algCost = runAlg(bipmulti);				

//					logObjective(c_steinerpf2rel, graph.getInstId(), false);
//					logObjective(c_steiner_int, graph.getInstId(), false);
//					logObjective(c_steinerlp, -1, false);
					logObjective(c_steinermflp,-1, true);
//					logObjective(c_steinerpf2, graph.getInstId(), false);

					
					
//					logObjective(c_smt, graph.getInstId(), false);
//					logObjective(c_smtvi, -1, true);
//					logObjective(o_smt, -1, true);
//
////					
//					logObjective(c_steinermf, -1, true);
//					logObjective(c_steinerpf2, -1, true);
//					logObjective(LP0Cost, graph.getInstId(), false);					
//					logObjective(LP1Cost, -1, false);
//					logObjective(LP2Cost, -1, false);
//					logObjective(cost, -1, false);
//					logObjective(cost_lazy, -1, true);
////					logObjective(algCost, -1, true);
//					sumLP1Cost += LP1Cost;
//					sumLP2Cost += LP2Cost;
//					sumCost += cost;
//					sumAlgCost += algCost;
//				}		
//				avgLP1Cost = sumLP1Cost / iter;
//				avgLP2Cost = sumLP2Cost / iter;
//				avgCost = sumCost / iter;
//				avgAlgCost = sumAlgCost / iter;
//				logStat(avgLP1Cost, vertexCount == 20, "lp1");
//				logStat(avgLP2Cost, vertexCount == 20, "lp2");
//				logStat(avgCost, vertexCount == 20, "cost");
//				logStat(avgAlgCost, vertexCount == 20, "alg");
//			}
//		}
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
		model.solve();
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
	
	private void logObjective(double obj, int id, boolean newline) {
        try	{
        	File datafile = new File("logs/cost_log.txt");
        	FileWriter fw = new FileWriter(datafile,true); //the true will append the new data
        	fw.write((id > 0 ? id + ": ": " ") +  Miscellaneous.round(obj, 2) + (newline ? "\n" : "\t "));
        	fw.close();
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
	}
	
	private void logStat(double obj, boolean newline, String filename) {
        try	{
        	File datafile = new File("logs/stat_" + filename + ".txt");
        	FileWriter fw = new FileWriter(datafile,true); //the true will append the new data
        	fw.write(Miscellaneous.round(obj, 2) + (newline ? "\n" : "\t"));
        	fw.close();
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
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
