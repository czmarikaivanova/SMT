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
import model.SMTF1VI;
import model.SMTF2VI;
import model.SMTFlowModel;
import model.SMTX1;
import model.SMTModelFlexiFlow;
import model.SMTX2;
import model.SMTF1;
import model.SMTF2;
import model.SMTX1VI;
import model.SteinerX;
import model.SMTX2VI;
import model.SteinerPF2Model;
import model.SteinerPF2Relaxed;

public class App {
    public static boolean stronger;
    public static boolean stronger2;
	int n;
    int d;
    Graph graph;
    private boolean draw;
	int iter;    
	String fname;  // generate from file
	
	public App(String fname) {
//		this.n = 12;
		this.d = 8;
		this.draw = false;
		this.iter = 50;
		this.fname = fname;
	}
	
	public int run() {
		for (int n = 10; n < 19; n++) {
			for  (int i = 0; i < iter; i++) {
				ArrayList<ILPModel> models = new ArrayList<ILPModel>();
				if (fname == null) {
					graph = new Graph(n, d);	// generate a new graph		
				}
				else {
					graph = new Graph(fname); // from file,
				}	
				graph.saveInstance();
				graph.generateAMPLData();
				
				models.add(new SMTX1(graph, Constants.LP, false));
				models.add(new SMTF1(graph, Constants.LP, false));
				models.add(new SMTX1VI(graph, Constants.LP, false));
				models.add(new SMTF1VI(graph, Constants.LP, false));
				models.add(new SMTX2(graph, Constants.LP, false));
				models.add(new SMTF2(graph, Constants.LP, false));
				models.add(new SMTX2VI(graph, Constants.LP, false));
				models.add(new SMTF2VI(graph, Constants.LP, false));
//				models.add(new SMTX1(graph, Constants.INTEGER, false));
				models.add(new SMTF1(graph, Constants.INTEGER, false));
	//
	//			models.add(new SMTModelFlexiFlow(graph, true, Constants.LP, false));
				
				runModel(models);
	//					ILPModel smtPf2LP = new SMTF2(graph, false, Constants.LP, false);
	//					Algorithm bip = new BIPAlgorithm(true, true);
	//					Algorithm bipmulti = new BIPAlgorithm(false, true);
			}
		}
		return 0;
	}
	
	private double runAlg(Algorithm alg) {
		Graph tree = alg.solve(graph);
		draw(tree, graph.getInstId(), alg.toString(), false);
		return tree.evaluate(d);
	}
	
	private void runModel(ArrayList<ILPModel> models) {
		for (ILPModel model: models) {
			long startT = System.currentTimeMillis();
			model.solve(true, Constants.MAX_SOL_TIME);
			long endT = System.currentTimeMillis();

			double lpCost1 = model.getObjectiveValue();
			logObjective(new File("logs/cost_log.txt"), lpCost1, models.indexOf(model) == 0 ? graph.getInstId() : -1, models.indexOf(model) == models.size() - 1);
			logObjective(new File("logs/runtime_log.txt"), (endT - startT)/1000, models.indexOf(model) == 0 ? graph.getInstId() : -1, models.indexOf(model) == models.size() - 1);
			Double[][] z = (Double[][]) model.getZVar();
//			Double[][][] f = (Double[][][]) model.getXVar();
//			checkConstraints(f, z);
//			System.err.println(" Y var for PF2 model: ");
			if (model instanceof SMTF2 ) {
//				checkFHzeroEqFzero(((SMTF2)model).getH(), f);
//				((SMTF2)model).getYVar();
			}
 			draw(z, graph.getInstId(), model.toString(), model instanceof MEBModel);
			model.end();
		}
	}
	
	private void checkConstraints(Double[][][] fvar, Double[][] zvar) {
		for (int t = 1; t < d; t++) {
			for (int i = 0; i < n; i++) {
				if (i != t) {
					if (fvar[t][i][t] != 0.0) {
						System.err.println("f_tit  is not nULL!!! i = " + i + " t = " +t  + " value: " + fvar[t][i][t]);
					}
					if (!fvar[i][t][t].equals(zvar[i][t])) {
						System.err.println("f_itt != x_it i = " + i + " t = " + t + "values: " + fvar[i][t][t] + " " + zvar[i][t] );
					}
				}
			}
			
		}
	}
	
	private void checkFHzeroEqFzero(Double[][][][] h, Double[][][] f) {
		for (int s = 0; s < d; s++) {
			for (int t = 0; t < d; t++) {
				if (s != t && s != 0) {
					Double hv = Miscellaneous.round(h[0][s][s][t], 2);
					Double fv = Miscellaneous.round(f[0][s][t], 2);
					if (!hv.equals(fv)) {
						System.err.println("H[0][s][s][t] at zero different from F[0][s][t]: s=" + s +" t=" + t+ " value: " + hv);
					}
				}
			}
		}
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
	
	private void draw(Object solution, int instId, String methodName, boolean useArrows) {
		if (draw) {
			Visualizer vis = new Visualizer(solution, graph, useArrows, false, methodName + " ID: " + instId);
			//Visualizer vis = new Visualizer(new File("instance.txt"), z, null);			
	        JFrame frame = new JFrame(methodName + " ID: " + instId);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.add(vis);
	        frame.setSize(Constants.WINDOW_SIZE, Constants.WINDOW_SIZE);
	        frame.setLocationRelativeTo(null);
	        frame.setVisible(true);
		}
    }		
	
	private void logObjective(File file, double obj, int id, boolean newline) {
        try	{
        	FileWriter fw = new FileWriter(file,true); //the true will append the new data
        	fw.write((id > 0 ? id + ": ": " ") +  Miscellaneous.round(obj, 4) + (newline ? "\n" : "\t "));
        	fw.close();
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
	}
	
}
