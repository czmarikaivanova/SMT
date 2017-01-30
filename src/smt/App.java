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
import model.SMTModelFlexiFlow;
import model.SMTMultiFlowModel;
import model.SMTOnlyFlow;
import model.SMTPF2Model;
import model.STFlow;
import model.SteinerModel;
import model.SteinerMultiFlowModel;
import model.SteinerPF2Model;
import model.SteinerPF2Relaxed;

public class App {
	
    int vertexCount = 16;
    int dstCount = 8;
    Graph graph;
    private boolean draw = true;
    private boolean generate = true;
    private boolean allowCrossing = true;
    private int n;
    private int d;
	public int run() {
		int iter = 10;
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
						graph = new Graph("saved_inst/weird.txt"); // from file, todo
					}	
					graph.saveInstance();
					graph.generateAMPLData();
					n = graph.getVertexCount();
					d = graph.getDstCount();
					ArrayList<ILPModel> modelList = new ArrayList<ILPModel>();
					
//					modelList.add(new SteinerModel(graph, false, Constants.LP, false));
//					modelList.add(new SteinerMultiFlowModel(graph, false, Constants.LP, false));
//					modelList.add(new SteinerPF2Model(graph, false, Constants.INTEGER, false));
					
					
//					ILPModel smt_lazy = new SMTModel(graph, false, Constants.INTEGER, false);
//					modelList.add(new SMTModel(graph, false, Constants.LP, false));
					modelList.add(new SMTMultiFlowModel(graph, false, Constants.LP, false));
	//				modelList.add(new SMTPF2Model(graph, false, Constants.LP, false));
//					modelList.add(new SMTMultiFlowModel(graph, true, Constants.LP, false));
//					modelList.add(new SMTPF2Model(graph, true, Constants.LP, false));
//					modelList.add(new SMTMultiFlowModel(graph, false, Constants.INTEGER, false));
//					modelList.add(new SMTPF2Model(graph, false, Constants.INTEGER, false));

					modelList.add(new SMTModelFlexiFlow(graph, false, Constants.LP, false));
//					modelList.add(new SMTModelFlexiFlow(graph, true, Constants.LP, false));
//					modelList.add(new STFlow(graph, null));
					
//					modelList.add(new SMTModel(graph, false, Constants.INTEGER, false));
					
					
					
					Double[][] pz = new Double[n][n];
					Double[][] z =  new Double[n][n];
					Double[][][] x = new Double[n][n][d];
					for (ILPModel model: modelList) {
						model.solve(true, Constants.MAX_SOL_TIME);
						boolean newline = modelList.indexOf(model) == modelList.size() - 1;
						int id = (modelList.indexOf(model) == 0) ? graph.getInstId() : -1;
						double cost = model.getObjectiveValue();
						System.out.println("obj: " + cost);
//						if (model instanceof SMTPF2Model) {
//							pz = ((SMTPF2Model) model).getPZ();
//							x = ((SMTPF2Model) model).getXVar();
//						}
//						if (model instanceof SteinerModel && !(model instanceof SteinerMultiFlowModel)) {
//							x = ((SteinerModel)model).getXVar();
//							generateViolatedFlowConstraints(constructF(x));
//						}

						z = model.getZVar();
//						draw(z, graph.getInstId(), model.toString(), model instanceof MEBModel);
						logObjective(cost, id, newline);
					}

					
					
//					Algorithm bip = new BIPAlgorithm(true, true);
//					Algorithm bipmulti = new BIPAlgorithm(false, true);
//					double algCost = runAlg(bipmulti);				
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
	
	private Double[][][][] constructF(Double[][][] x) {
		Double[][][][] f = new Double[n][n][d][d];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				for (int s = 0; s < d; s++) {
					for (int t = 0; t < d; t++) {
						if (x[j][i][s] != null && x[j][i][t] != null) {
							double newval = Math.max(x[j][i][s] + x[j][i][t] - 1, 0);
							f[i][j][s][t] = x[j][i][s] * x[j][i][t];
//							if (f[i][j][s][t] > x[i][j][s]) {
//								f[i][j][s][t] = x[i][j][s];
//							}
						}
					}
				}
			}
		}
		return f;
	}
	
	// this function has to be written nicer !
	private void generateViolatedFlowConstraints(Double [][][][] f) {
		ArrayList<Pair<Integer, Integer>> stPairs = new ArrayList<Pair<Integer,Integer>>();
		int addingcnt = 0;
		int okcnt = 0;
		for (int s = 0; s < d; s++) {
			for (int t = 0; t < d; t++) {
				if (t != s) {
					for (int i = 0; i < n; i++) {
						if (i != s) {
							if (i != t) {
								double lhs = 0;
								double rhs = 0;
								for (int j = 0; j < n; j++) {  // flow balance normal
									if (i != j) {
										if (j != s) {
											lhs += f[i][j][s][t];
										}
										if (j != t) {
											rhs += f[j][i][s][t];
										}
									}
								}
								if (Math.abs(lhs - rhs) < 0.0005) {
									System.out.println("add normal");
									stPairs.add(new Pair<Integer, Integer>(s, t));
									addingcnt++;
									break;  // for i
								}
								else {
									okcnt++;
								}
							}
							else { // i == t
								double lhs = 0;
								double rhs = 0;
								for (int j = 0; j < n; j++) {
									if (i != j) {
										if (j != s) {
											lhs += f[i][j][s][t];
										}
										if (j != t) {
											rhs += f[j][i][s][t];
										}
									}
								}
								if ((lhs - rhs < -1.005) || (lhs - rhs > -0.995)) { 
//								if (lhs - rhs != -1) {
//									model.addFlowBalanceNormalConstraint(i, s, t);
									System.out.println("add dst");
									addingcnt++;
									break; // for i
								}	
								else {
									okcnt++;
								}
							}
						} else { // i == s
							if (t != s) {
								double lhs = 0;
								double rhs = 0;
								for (int j = 0; j < n; j++) {
									if (i != j && j != s) {
										lhs += f[i][j][s][t];
									}
									if (i != j && j != t) {
										rhs += f[j][i][s][t];
									}
									if (lhs - rhs < 0.995 || lhs - rhs > 1.005) {
//											model.addFlowBalanceNormalConstraint(i, s, t);
										System.out.println("add source");
										addingcnt++;
										break; // for i
									}		
									else {
										okcnt++;
									}
								}					
							}
						}
					}
				}
			} 
		}
		System.err.println("pairs: " + stPairs.size());
		System.err.println("added: " + addingcnt);
		System.err.println("ok: " + okcnt);
		SteinerModel model = new SteinerModel(graph, false, Constants.LP, false);
		model.addFlowBalanceNormalConstraints(stPairs);
		model.solve(false, 0);
		Double[][][][] fVar = model.getFVal();
		System.err.println("next run: " + model.getObjectiveValue());
		model.end();
	}
	
	private void checkXXConstraint(Double[][][] x, Double[][] pz) {
		System.err.println("checking z pz");
		for (int i = 0; i < pz.length; i++) {
			for (int j = i + 1; j < pz.length; j++) {
				if (pz[i][j] != x[i][j][0]) {
					System.err.println("pz" + i + "" +j+ ":" +pz[i][j]);
					System.err.println("x" + i + "" +j+ ":" +x[i][j][0]);
				}
			}
		}
	}

	private void checkZPZConstraint(Double[][] z, Double[][] pz) {
		System.err.println("checking z pz");
		for (int i = 0; i < z.length; i++) {
			for (int j = i + 1; j < z.length; j++) {
				if (z[i][j] != pz[i][j] + pz[j][i]) {
					System.err.println("z" + i + "" +j+ ":" +z[i][j]);
					System.err.println("pz" + i + "" +j+ ":" +pz[i][j]+ "and " + pz[j][i]);
				}
			}
		}
	}

	private double runAlg(Algorithm alg) {
		Graph tree = alg.solve(graph);
		draw(tree, graph.getInstId(), alg.toString(), false);
		return tree.evaluate(dstCount);
	}
	
	private double runModel(ILPModel model) {
		model.solve(false, 0);
		double lpCost1 = model.getObjectiveValue();
		Double[][] z = (Double[][]) model.getZVar();

		
		draw(z, graph.getInstId(), model.toString(), model instanceof MEBModel);
		
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
