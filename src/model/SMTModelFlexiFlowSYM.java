package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

import cgstrategy.CGStrategy;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import smt.Constants;
import smt.Miscellaneous;
import graph.Graph;

public class SMTModelFlexiFlowSYM extends SMTX1VI {
	
	protected IloNumVar[][][][] f;
	File stLogFile = new File("logs/cglog.txt");
	FileWriter fw;
	FileWriter xmlFw;
	CGStrategy cgStrategy;
	
	public SMTModelFlexiFlowSYM(Graph graph, boolean isLP, boolean includeC, CGStrategy cgStrategy ) {
		super(graph, isLP, includeC);
		this.cgStrategy = cgStrategy;
	}

	public boolean solve(boolean useTimeLimit, int seconds) {
		try {
			double currObj = 0;   // current objective value
			double currTime = 0;  // runtime of the current calculation
			int constraintCnt = 0; 	   // # of constraints in the model;
			int variableCnt = 0; 	   // # of variables in the model
//			cplex.setParam(IloCplex.DoubleParam.TiLim, seconds); TODO: consider whether you wanna use time limit here
			PriorityQueue<STPair> pairQueue = new PriorityQueue<STPair>();
			PriorityQueue<STPair> violatedPairsQueue = new PriorityQueue<STPair>();
			boolean solved = true; // true if all constraints are satisfied and the calculation can terminate
			boolean ret;
			int iter = 0;      // # of iterations (how many times we had to calculate the model with some added flow constraints) 
//			IloCplex singleFlowCplex = new IloCplex();
			initLog();
			double totalStartTime = this.getCplexTime();
			do {
				iter++;
				pairQueue.clear();
				violatedPairsQueue.clear();
				constraintCnt =  cplex.getNrows();
				variableCnt = cplex.getNcols();
				double startT = this.getCplexTime();
				ret = cplex.solve();
				double stopT = this.getCplexTime();
				currTime = Miscellaneous.round(stopT - startT, 2);
				currObj = Miscellaneous.round(cplex.getObjValue(), 2);
				solved = true;
				solved = cgStrategy.runSTMaxFlows(violatedPairsQueue, pairQueue, getXVar(), getYVar());
				iterationLog(fw, iter, currObj, currTime, cgStrategy.getSatisfiedCnt(), violatedPairsQueue, pairQueue, constraintCnt, variableCnt);
				if (!solved && pairQueue.size() > 0) {
					addFlowConstraints(pairQueue, fw);
				}
			} while (!solved /*&& iter < 4*/);
			double totalExitTime = this.getCplexTime();
			exitLog(fw, currObj, totalExitTime - totalStartTime);
			return ret;
		} catch (IloException e) {
			e.printStackTrace();
			return false;
		}
	}
	
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


	// write a header of the log
	private void initLog() {
		try {
			fw = new FileWriter(stLogFile, true);
			fw.write("ID: " + graph.getInstId() + " STRATEGY: " + cgStrategy.toString() + " TOLERANCE: " + cgStrategy.getTolerance() + "\n");
			fw.write("iter \t currObj \t currTime \t satCnt \t violCnt \t addedCnt \t conCnt \t varCnt \n");
			fw.close();
			File xmlFile = new File("cglogs/" + graph.getInstId() + "_" +cgStrategy.toString() + "_T=" + cgStrategy.getTolerance() + "_" + new File("cglogs/").list().length + ".xml");
			xmlFw = new FileWriter(xmlFile, true);
			xmlFw.write("<?xml version = \"1.0\"?>\n");
			xmlFw.write("<run strategy =\"" + cgStrategy.toString() + "\" tolerance = \"" + cgStrategy.getTolerance() + "\">\n");
			xmlFw.write(graph.getXMLString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	private void initFlexiVars(int s, int t) {
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
					try {
						if (f[i][j][s][t] == null)	f[i][j][s][t] = cplex.numVar(0, 1);
						if (f[i][j][t][s] == null) f[i][j][t][s] = cplex.numVar(0, 1);
						if (f[j][i][s][t] == null) f[j][i][s][t] = cplex.numVar(0, 1);
						if (f[j][i][t][s] == null) f[j][i][t][s] = cplex.numVar(0, 1);
					} catch (IloException e) {
						e.printStackTrace();
					}	
			}					
		}		
	}
	
//	protected void initVars() {
//		try {
//			super.initVars();
//			f = new IloNumVar[n][n][d][d];		
//			for (int i = 0; i < n; i++) {
//				for (int j = 0; j < n; j++) {
//					for (int k = 0; k < d; k++) {
//						for (int l = 0; l < d; l++) {
//							f[i][j][k][l] = cplex.numVar(0,1);
//						}
//					}	
//				}					
//			}
//		} catch (IloException e) {
//			e.printStackTrace();
//		}
//	}	
	
	protected void initVars() {
		try {
			super.initVars();
			f = new IloNumVar[n][n][d][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						f[i][j][k] = cplex.numVarArray(d,0,1);	
					}	
				}					
			}
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	

	
	public void createConstraints() {
		try{
			super.createConstraints();		
			
			// f sym + cap
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
//								cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
//								cplex.addLe(f[i][j][s][t], x[i][j][s]);
							}
						}
					}
				}
			}	
			
			// f imp y (very strong one)
//			if (includeC) {
			for (int j = 0; j < n; j++) {
				for (int s = 0; s < d; s++) {
					for (int t = 0; t < d; t++) {
						for (int k = 0; k < n; k++) {
							if (j != k && s != t) {
								IloLinearNumExpr expr1 = cplex.linearNumExpr();
								IloLinearNumExpr expr2 = cplex.linearNumExpr();
								for (int i = 0; i < n; i++) {
									if (i != j) { 
										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
											if (s < t) {
												expr1.addTerm(1.0, f[j][i][s][t]);
											}
											else {
												expr1.addTerm(1.0, f[i][j][t][s]);
											}
											expr2.addTerm(1.0, y[j][i][s]);		
										}
									}
								}
								cplex.addLe(expr1, expr2);
							}
						}
					}
				}
			}
//			}
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @param queue Queue of constraints
	 * @param maxPCnt how many should be added
	 */
	public void addFlowConstraints(PriorityQueue<STPair> queue,  FileWriter fw) {
		try {
			fw = new FileWriter(stLogFile, true);
			while (queue.size() > 0) {
				STPair pair = queue.poll();
//				fw.write(pair.toString() + "\n");
				System.out.println(pair.toString());
				int s = pair.getS();
				int t = pair.getT();
//				initFlexiVars(s, t);
				// Flow conservation - normal

				IloLinearNumExpr expr2a = cplex.linearNumExpr();
				IloLinearNumExpr expr2b = cplex.linearNumExpr();	
				IloLinearNumExpr expr2c = cplex.linearNumExpr();	
				IloLinearNumExpr expr2d = cplex.linearNumExpr();	
				
						for (int i = 0; i < n; i++) {		
							if (i != t) {
								expr2a.addTerm(1.0, f[t][i][s][t]);									// Flow conservation - dest				
								expr2b.addTerm(1.0, f[i][t][s][t]);									// Flow conservation - dest
							}
							if (i != s) {
								expr2c.addTerm(1.0, f[i][s][s][t]);									// Flow conservation - dest
								expr2d.addTerm(1.0, f[s][i][s][t]);									// Flow conservation - dest
							}	
							IloLinearNumExpr expr1a = cplex.linearNumExpr();
							IloLinearNumExpr expr1b = cplex.linearNumExpr();	
							IloLinearNumExpr expr1c = cplex.linearNumExpr();
							IloLinearNumExpr expr1d = cplex.linearNumExpr();

							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(f[i][j][s][t], x[i][j][s]);				// capacity
									cplex.addLe(f[j][i][s][t], x[i][j][t]);				// capacity

									if (t != i && s != i) {
										if (j != s) {
											expr1a.addTerm(1.0, f[i][j][s][t]);									
											expr1c.addTerm(1.0, f[i][j][s][t]);									
										}								
										if (j != t) {								
											expr1b.addTerm(1.0, f[j][i][s][t]);
											expr1d.addTerm(1.0, f[j][i][s][t]);
										}			
									}
								}
							}						
							cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b)));
							cplex.addEq(0,cplex.sum(expr1c, cplex.negative(expr1d)));
						}
						cplex.addEq(-1,cplex.sum(expr2a, cplex.negative(expr2b)));
						cplex.addEq(-1,cplex.sum(expr2c, cplex.negative(expr2d)));
				
				
				
				// f sym
//						for (int i = 0; i < n; i++) {
//							for (int j = 0; j < n; j++) {
//								if (j != i) {
//									cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
//								}
//							}
//						}
				
// VALID INEQUALITIES START HERE!
										
//					cplex.addEq(exprYsum, 1.0);''
								
	
		
//								sym h implication
//								for (int i = 0; i < n; i++) {
//									for (int j = 0; j < n; j++) {
//										if( i != j) {
//											if (s != t) {
//												for (int u = 0; u < d; u++) {
//													cplex.addGe(f[i][j][s][t], cplex.sum(f[i][j][u][t], cplex.negative(f[i][j][u][s])));
//													cplex.addEq(cplex.sum(f[i][j][u][t], f[j][i][u][s], f[i][j][t][s]), cplex.sum(f[i][j][u][s], f[j][i][u][t], f[i][j][s][t]));
//												}																												
//											}
//										}
//									}
//								}								
						}
			fw.close();
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	

	
	public String toString() {
		return Constants.SMT_FLEXI_STRING;
	}
	

}
