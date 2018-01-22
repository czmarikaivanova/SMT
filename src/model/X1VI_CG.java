package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;
import cgstrategy.CGStrategy;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import smt.Constants;
import smt.Miscellaneous;
import graph.Graph;

public class X1VI_CG extends SMTX1VI {
	
	protected IloNumVar[][][][] f;
	File stLogFile = new File("logs/cglog.txt");
	FileWriter fw;
	FileWriter xmlFw;
	CGStrategy cgStrategy;
	
	public X1VI_CG(Graph graph, boolean isLP, CGStrategy cgStrategy ) {
		super(graph, isLP);
		this.cgStrategy = cgStrategy;
	}

	public boolean solve(boolean useTimeLimit, int seconds) {
		try {
			outputSettingsInfo();
			double currObj = 0;   // current objective value
			double currTime = 0;  // runtime of the current calculation
			int constraintCnt = 0; 	   // # of constraints in the model;
			int variableCnt = 0; 	   // # of variables in the model
//			cplex.setParam(IloCplex.DoubleParam.TiLim, seconds); TODO: consider whether you wanna use time limit here
//			PriorityQueue<STPair> pairQueue = new PriorityQueue<STPair>(11, STPair.getFlowViolationComparator());
//			PriorityQueue<STPair> violatedPairsQueue = new PriorityQueue<STPair>(11, STPair.getFlowViolationComparator());
			PriorityQueue<STPair> pairQueue = new PriorityQueue<STPair>(11, cgStrategy.getComparator());
			PriorityQueue<STPair> violatedPairsQueue = new PriorityQueue<STPair>(11,cgStrategy.getComparator());
			boolean solved = true; // true if all constraints are satisfied and the calculation can terminate
			boolean ret;
			int iter = 0;      // # of iterations (how many times we had to calculate the model with some added flow constraints) 
//			IloCplex singleFlowCplex = new IloCplex();
			initLog();
			double totalStartTime = this.getCplexTime();
			do {
				iter++;
//				pairQueue.clear();
				violatedPairsQueue.clear();
				constraintCnt =  cplex.getNrows();
				variableCnt = cplex.getNcols();
				double startT = this.getCplexTime();
				ret = cplex.solve();
				double stopT = this.getCplexTime();
				currTime = Miscellaneous.round(stopT - startT, 2);
				currObj = Miscellaneous.round(cplex.getObjValue(), 2);
				solved = true;
				pairQueue = cgStrategy.runSTMaxFlows(violatedPairsQueue, get3DVar(), getYVar());
				solved = pairQueue.size() == 0;
				iterationLog(fw, iter, currObj, currTime, cgStrategy.getSatisfiedCnt(), violatedPairsQueue, pairQueue, constraintCnt, variableCnt);
				if (!solved) {
					addFlowConstraints(pairQueue);
				}
			} while (!solved);
			double totalExitTime = this.getCplexTime();
			exitLog(fw, currObj, totalExitTime - totalStartTime);
			return ret;
		} catch (IloException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Initialize variables.
	 */
	protected void initVars() {
		try {
			super.initVars(); // inherit all vars from X1VI. Here we define only f_{ij}^{st} for s < t
			f = new IloNumVar[n][n][d][d];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						for (int l = k + 1; l < d; l++) {
							f[i][j][k][l] = cplex.numVar(0,1);
						}
					}	
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	
	
	public void createConstraints() {
		super.createConstraints();  // constraints from parent class X1VI		
		// The symmetry f_{ij}^{st} = f_{ji}^{ts} is not needed. Whenever we need f_{ij}^{st} where s > t, we use f_{ji}^{ts} instead.
	}
	
	/**
	 * After each iteration we add some max flow constraints 
	 * @param queue Queue s-t pairs to which associated constraints shall be added
	 */
	public void addFlowConstraints(PriorityQueue<STPair> queue) {
		try {
			while (queue.size() > 0) {
				STPair pair = queue.poll();
				System.out.println(pair.toString());
				int s = pair.getS();
				int t = pair.getT();
				IloLinearNumExpr sumLeaveT_ST = cplex.linearNumExpr();
				IloLinearNumExpr sumEnterT_ST = cplex.linearNumExpr();	
				IloLinearNumExpr sumLeaveS_TS = cplex.linearNumExpr();	
				IloLinearNumExpr sumEnterS_TS = cplex.linearNumExpr();	
				for (int i = 0; i < n; i++) {		
					if (i != t) {
						sumLeaveT_ST.addTerm(1.0, f[t][i][s][t]);		
						sumEnterT_ST.addTerm(1.0, f[i][t][s][t]);
					}
					if (i != s) {
						sumLeaveS_TS.addTerm(1.0, f[i][s][s][t]);
						sumEnterS_TS.addTerm(1.0, f[s][i][s][t]);
					}	
					IloLinearNumExpr sumLeaveI_ST = cplex.linearNumExpr();
					IloLinearNumExpr sumEnterI_ST = cplex.linearNumExpr();	
					IloLinearNumExpr sumEnterI_TS = cplex.linearNumExpr();
					IloLinearNumExpr sumLeaveI_TS = cplex.linearNumExpr();

					for (int j = 0; j < n; j++) {
						if (j != i) {
							cplex.addLe(f[i][j][s][t], x[i][j][s]);				// capacity f_{ij}^{st} <= x_{ij}^s
							cplex.addLe(f[j][i][s][t], x[i][j][t]);				// capacity f_{ij}^{ts} = f_{ji}^{st} <= x_{ij}^t

							if (t != i && s != i) {
								if (j != s) {
									sumLeaveI_ST.addTerm(1.0, f[i][j][s][t]);									
									sumEnterI_TS.addTerm(1.0, f[i][j][s][t]);									
								}								
								if (j != t) {								
									sumEnterI_ST.addTerm(1.0, f[j][i][s][t]);
									sumLeaveI_TS.addTerm(1.0, f[j][i][s][t]);
								}			
							}
						}
					}						
					cplex.addEq(0,cplex.sum(sumLeaveI_ST, cplex.negative(sumEnterI_ST)));	// flow conservation at i \in V \ {t,s} for the commodity s-t
					cplex.addEq(0,cplex.sum(sumEnterI_TS, cplex.negative(sumLeaveI_TS)));	// flow conservation at i \in V \ {t,s} for the commodity t-s
				}
				cplex.addEq(-1,cplex.sum(sumLeaveT_ST, cplex.negative(sumEnterT_ST)));		// flow conservation at t for the commodity s-t
				cplex.addEq(-1,cplex.sum(sumLeaveS_TS, cplex.negative(sumEnterS_TS)));		// flow conservation at s for the commodity t-s
				
				// f imp y (2i). We use the symmetry, i. e. f_{ji}^{ts} = f_{ij}^{st} 
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < n; k++) {
						if (j != k) {
							IloLinearNumExpr fSumST = cplex.linearNumExpr();
							IloLinearNumExpr fSumTS = cplex.linearNumExpr();
							IloLinearNumExpr hSumST = cplex.linearNumExpr();
							IloLinearNumExpr hSumTS = cplex.linearNumExpr();
							for (int i = 0; i < n; i++) {
								if (i != j) { 
									if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
										fSumST.addTerm(1.0, f[j][i][s][t]);
										fSumTS.addTerm(1.0, f[i][j][s][t]);
										hSumST.addTerm(1.0, y[j][i][s]);		
										hSumTS.addTerm(1.0, y[j][i][t]);		
									}
								}
							}
							cplex.addLe(fSumST, hSumST);		// (2i) for commodity (s,t)
							cplex.addLe(fSumTS, hSumTS);		// (2i) for commodity (t,s)
						}
					}
				}				
			}
			
			
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		} 
	}
	
	
	// write a header of the log file. We are logging the course of the generated constraints
	private void initLog() {
		try {
			fw = new FileWriter(stLogFile, true); 
			fw.write("ID: " + graph.getInstId() + " STRATEGY: " + cgStrategy.toString() + " TOLERANCE: " + cgStrategy.getTolerance() + "\n");
			fw.write("iter \t currObj \t currTime \t satCnt \t violCnt \t addedCnt \t conCnt \t varCnt \n");
			fw.close();
			File xmlFile = new File("logs/cglogs/" + graph.getInstId() + "_" +cgStrategy.toString() + "_T=" + cgStrategy.getTolerance() + "_" + new File("logs/cglogs/").list().length + ".xml");
			xmlFw = new FileWriter(xmlFile, true);
			xmlFw.write("<?xml version = \"1.0\"?>\n");
			xmlFw.write("<run strategy =\"" + cgStrategy.toString() + "\" tolerance = \"" + cgStrategy.getTolerance() + "\">\n");
			xmlFw.write(graph.getXMLString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * log one iteration. We use two files:
	 *  	- TXT file that stores some general info about the CG iterations
	 *  	- XML file that stores exactly which s-t pairs is violated and what is the value of max flow. The file is used for visualisation
	 * @param fw File writer. 
	 * @param iter iteration
	 * @param currObj objective value after the current iteration
	 * @param currTime runtime of the current iteration
	 * @param satisfiedCnt # of satisfied s-t pairs in the current iteration
	 * @param violatedPairQueue 
	 * @param addedPairQueue
	 * @param constraintCnt
	 * @param variableCnt
	 */
	private void iterationLog(FileWriter fw, int iter, double currObj, double currTime, int satisfiedCnt, PriorityQueue<STPair> violatedPairQueue, PriorityQueue<STPair> addedPairQueue,
			int constraintCnt, int variableCnt) {
		int violatedCnt = violatedPairQueue.size();
		int addedCnt = addedPairQueue.size();
		try {
			fw = new FileWriter(stLogFile, true);
			fw.write(iter + "\t" + currObj + "\t" + currTime + "\t" + satisfiedCnt + "\t" + violatedCnt + "\t" + addedCnt + "\t" + constraintCnt + "\t " + variableCnt + "\n");
			fw.close();
			
			xmlFw.write("\t<iteration i=\"" +iter +"\" sc=\"" + satisfiedCnt + "\" vc=\"" + violatedCnt + "\" ac= \"" + addedCnt + "\">\n");
			for (STPair p: violatedPairQueue) {
				String isAdded = (addedPairQueue.contains(p) ? "true" : "false");
				xmlFw.write("\t\t<violated s=\"" + p.getS() + "\" t=\"" + p.getT() + "\" val=\"" + Miscellaneous.round(p.getDiff(),2) + "\"  added=\"" + isAdded + "\" />\n");
			}
			xmlFw.write("\t</iteration>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * log resulted values
	 * @param fw  file writer
	 * @param totalObj resulting objective value
	 * @param totalTime total time = sum of times of all iterations
	 */
	private void exitLog(FileWriter fw, double totalObj, double totalTime) {
		try {
			fw = new FileWriter(stLogFile, true);
			fw.write("TOTAL OBJECTIVE: " + totalObj + "\n");
			fw.write("TOTAL TIME: " + totalTime + "\n");
			fw.write(Constants.DELIMITER + Constants.DELIMITER + Constants.DELIMITER + "\n");
			fw.close();
			xmlFw.write("</run>");
			xmlFw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return "CG_"+ cgStrategy.toString()+ "-" + n + "-" + d + "";
	}
	

}
