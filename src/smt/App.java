package smt;

import graph.Graph;
import graph.Node;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;

import cgstrategy.CGStrategy;
import cgstrategy.CG_All;
import cgstrategy.CG_FirstK;
import cgstrategy.CG_AddMatching;
import cgstrategy.CG_MaxSpanningTree;
import algorithm.Algorithm;
import algorithm.BIPAlgorithm;
import algorithm.MSTAlgorithm;
import algorithm.Meta;
import model.ILPModel;
import model.MEBModel;
import model.SMTF1VI;
import model.SMTF2;
import model.SMTF2VI;
import model.STPair;
import model.SMTX1;
import model.SMTX2;
import model.SMTF1;
import model.SMTX1VI;
import model.SMTX2B;
import model.SMTX2VIB;
import model.SteinerX;
import model.SMTX2VI;
import model.X1VI_CG;

public class App {
    public static boolean stronger;
    public static boolean stronger2;
	int n;
    int d;
    Graph graph;
    private boolean draw =  true;
	int iter;    
	String fname;  // generate from file
	private File costFile;
	private File timeFile;
	
	
	public App(int n, int d, int iter) {
		this.n = n;
		this.d = d;
		this.iter = 1;
		this.fname =  "backbin/saved_ampl_inst/BandB-22-11";
//		this.fname =  null;
		this.costFile = new File("logs/cost_log2.txt");
		this.timeFile = new File("logs/runtime_log2.txt");
	}
	
	public int run() {
			for  (int i = 0; i < iter; i++) {
				ArrayList<ILPModel> models = new ArrayList<ILPModel>();
				if (fname == null) {
					graph = new Graph(n, d);	// generate a new graph		
				}
				else {
					if (new File(fname).isDirectory()) {
						File dir = new File(fname);
						File[] dirListing = dir.listFiles();
						if (dirListing != null) {
							for (File childF : dirListing) {
								models.clear();
								
								graph = new Graph(childF.getAbsolutePath()); // from file,
								n = graph.getVertexCount();
								d = graph.getDstCount();
								Constants.INCLUDE = false;
								models.add(new SMTX1(graph, Constants.INTEGER));
								Constants.INCLUDE = true;
								models.add(new SMTF1(graph, Constants.INTEGER));
								runModel(models, i == 0);
								Algorithm meta = new Meta(false); // false = not only destinations
								runAlg(meta);
							}
						}
						
					}
					else {
						graph = new Graph(fname); // from file,
						n = graph.getVertexCount();
						d = graph.getDstCount();
					}
				}	
				
				graph.saveInstance();
				graph.generateAMPLData();
//				
				
		//		models.add(new SMTX1(graph, Constants.LP));
//				models.add(new SMTF1(graph, Constants.LP));				

		//		models.add(new SMTX1VI(graph, Constants.LP));

//				models.add(new SMTF1VI(graph, Constants.LP));
//				Constants.INCLUDE = true;
//				models.add(new SMTF1VI(graph, Constants.LP));
//				Constants.INCLUDE = false;
//				models.add(new SMTX1VI(graph, Constants.LP));
//				Constants.INCLUDE = true;
//				models.add(new SMTX1VI(graph, Constants.LP));
//				models.add(new SMTX2(graph, Constants.LP));
		

//				models.add(new SMTX1(graph, Constants.INTEGER, true));
//				models.add(new X1VI_CG(graph, Constants.LP, new (-0.9, graph, STPair.getFlowViolationComparator())));
//				models.add(new X1VI_CG(graph, Constants.LP, new CG_FirstK(-0.9, graph, STPair.getFlowViolationComparator(), 10)));
//				models.add(new X1VI_CG(graph, Constants.LP, new CG_FirstK(-0.9, graph, STPair.getDistanceComparator(), 10)));
//				models.add(new X1VI_CG(graph, Constants.LP, new CG_MaxSpanningTree(-0.9, graph, STPair.getFlowViolationComparator())));
//				models.add(new X1VI_CG(graph, Constants.LP, new CG_MaxSpanningTree(-0.9, graph, STPair.getDistanceComparator())));
//				models.add(new X1VI_CG(graph, Constants.LP, new CG_AddMatching(-0.9, graph, STPair.getFlowViolationComparator())));
				Constants.INCLUDE = false;
				models.add(new SMTX1(graph, Constants.INTEGER));
				Constants.INCLUDE = true;
				models.add(new SMTF1(graph, Constants.INTEGER));				
//				models.add(new X1VI_CG(graph, Constants.LP, new CG_AddMatching(-0.9, graph, STPair.getDistanceComparator())));
//				models.add(new SMTX2(graph, Constants.LP, true));
//				models.add(new SMTX1(graph, Constants.INTEGER));
//				models.add(new SMTF2(graph, Constants.INTEGER));				
//				models.add(new SMTX2B(graph, Constants.INTEGER));
				runModel(models, i == 0);
	//					ILPModel smtPf2LP = new SMTF2(graph, false, Constants.LP, false);
	//					Algorithm bip = new BIPAlgorithm(true, true);
	//					Algorithm bipmulti = new BIPAlgorithm(false, true);

				Algorithm meta = new Meta(false); // false = not only destinations
				runAlg(meta);		
			}
//		}
		return 0;
	}
	
	private double runAlg(Algorithm alg) {
		long startT = System.currentTimeMillis();
		Graph tree = alg.solve(graph);
		long endT = System.currentTimeMillis();
//		draw(tree, graph.getInstId(), alg.toString(), false);
		double obj = tree.evaluate(d); 
		logObjective(costFile, obj, 0, -1, true);
		logObjective(timeFile, (endT - startT)/1000, 0, -1, true);
		return obj;
	}
	
	private void runModel(ArrayList<ILPModel> models, boolean firstRun) {
		if (firstRun) {
//			logHeaders(costFile, timeFile, models);
		}
		for (ILPModel model: models) {
			int modelIdx = models.indexOf(model);
			System.out.println("New model started:  " + model.toString() + "  " + modelIdx);
			long startT = System.currentTimeMillis();
			model.solve(true,Constants.MAX_SOL_TIME);
			long endT = System.currentTimeMillis();

			double lpCost1 = model.getObjectiveValue();
			double gap = (!model.isLP() ? model.getGap() : -1.0);
			logObjective(costFile, lpCost1, gap, modelIdx  == 0 ? graph.getInstId() : -1, false);
			logObjective(timeFile, (endT - startT)/1000, gap, modelIdx == 0 ? graph.getInstId() : -1, false);
//			logObjective(costFile, lpCost1, gap, modelIdx  == 0 ? graph.getInstId() : -1, modelIdx  == models.size() - 1);
//			logObjective(timeFile, (endT - startT)/1000, gap, modelIdx == 0 ? graph.getInstId() : -1, modelIdx == models.size() - 1);
			Double[][] z = (Double[][]) model.getTreeVar();

//			Double[][][] f = (Double[][][]) model.getXVar();
//			checkConstraints(f, z);
//			System.err.println(" Y var for PF2 model: ");
			if (model instanceof SMTX2 ) {
				Double[][][] xvar = model.get3DVar();
				Double[][][][] fvar = ((SMTX2) model).getFVar();
				checkXXFF(fvar, xvar);
//				checkFHzeroEqFzero(((SMTF2)model).getH(), f);
//				((SMTF2)model).getYVar();
			}
// 			draw(z, graph.getInstId(), model.toString(), model instanceof SMTF1VI);
			model.end();
		}
	}

	private void checkXXFF(Double[][][][] fvar, Double[][][] xvar) {
		for (int i = 0; i < n; i ++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
					for (int s = 0; s < d; s++) {
						for (int t = 0; t < d; t++) {
							if (s != t) {
								double absdiff = Math.abs((xvar[i][j][s] - fvar[i][j][s][t]) - (xvar[i][j][t] - fvar[i][j][t][s]));
								if (absdiff > 0.00001) {
									System.err.println("INSTANCE: " + graph.getInstId() + " DIFF: " + absdiff);
									System.err.print(" i = " + i);
									System.err.print(" j = " + j);
									System.err.print(" s = " + s);
									System.err.println(" t = " + t);
									System.err.print(" x ijt = " + xvar[i][j][t]);
									System.err.print(" x ijs = " + xvar[i][j][s]);
									System.err.print(" f jist = " + fvar[j][i][s][t]);
									System.err.println(" f ijst = " + fvar[i][j][s][t]);
							//		System.err.println(" X_ij^t - x_ijs NOT EQUAL f_ij^st - f_ij^st !!!");
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void checkFXRELTIGHT(Double[][][] fvar, Double[][] zvar) {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i != j) {
					boolean tight = false;
					int tightt = -1;
					for (int t = 0; t < d; t++) {
						if (Math.abs(fvar[i][j][t] - zvar[i][j]) < 0.001) {
							tight = true;
							tightt = t;
						}
					}
					if (!tight) {
						System.err.println("No tight!");
						System.err.println("i="+i);
						System.err.println("j="+j);
						System.err.println("min slack: " + tightt);
						System.err.println(graph.getInstId());
						System.exit(0);
					}
				
				}
			}
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
	
	/**
	 * log objective or time for one run
	 * @param file
	 * @param obj
	 * @param id
	 * @param newline
	 */
	private void logObjective(File file, double obj, double gap, int id, boolean newline) {
        try	{
        	FileWriter fw = new FileWriter(file,true); //the true will append the new data
        	fw.write((id > 0 ? id + ": ": " ") +  Miscellaneous.round(obj, 4) + (newline ? "\n" : "\t "));
//        	fw.write((id > 0 ? id + ": ": " ") +  Miscellaneous.round(obj, 4) + "\t" + Miscellaneous.round(gap, 2) + (newline ? "\n" : "\t "));
        	fw.close();
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
	}
	
	/**
	 * write model names (and possibly strategies ) to the first line of log files
	 * @param costFile2
	 * @param timeFile2
	 * @param models
	 */
	private void logHeaders(File costFile2, File timeFile2, ArrayList<ILPModel> models) {
        try	{
        	FileWriter fw = new FileWriter(costFile,true); //the true will append the new data
        	for (ILPModel m: models) {
        		fw.write(m.toString() + ((models.indexOf(m) == models.size() - 1) ? "\n" : "\t"));
        	}
        	fw.close();
        	fw = new FileWriter(timeFile,true); //the true will append the new data
        	for (ILPModel m: models) {
        		fw.write(m.toString() + ((models.indexOf(m) == models.size() - 1) ? "\n" : "\t"));
        	}
        	fw.close();
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
	}
	
}
