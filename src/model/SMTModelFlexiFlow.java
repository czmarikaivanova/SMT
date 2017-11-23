package model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.PriorityQueue;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import smt.Constants;
import smt.Miscellaneous;
import graph.Graph;
import graph.Node;

public class SMTModelFlexiFlow extends SMTX1VI {

	public SMTModelFlexiFlow(Graph graph, boolean isLP, boolean excludeC) {
		super(graph, isLP, excludeC);
	}
	
	protected IloNumVar[][][][] f;
	File stLogFile = new File("logs/cglog.txt");
	FileWriter fw;
	double tolerance = -1.9999;

	public boolean solve(boolean useTimeLimit, int seconds) {
		try {
			int satisfiedCnt = 0;   // # of s-t pairs satisfying the flow constraints
			int volatedCnt = 0;     // # of s-t pairs violating the flow constraints
			double currObj = 0;   // current objective value
			double currTime = 0;  // runtime of the current calculation
			int constraintCnt = 0; 	   // # of constraints in the model;
			int variableCnt = 0; 	   // # of variables in the model
			cplex.setParam(IloCplex.DoubleParam.TiLim, seconds);
			PriorityQueue<STPair> pairQueue = new PriorityQueue<STPair>();
			boolean solved = false; // true if all constraints are satisfied and the calculation can terminate
			boolean ret;
			int iter = 0;      // # of iterations (how many times we had to calculate the model with some added flow constraints) 
			IloCplex singleFlowCplex = new IloCplex();
			initLog();
			double totalStartTime = this.getCplexTime();
			do {
				iter++;
				pairQueue.clear();
				constraintCnt =  cplex.getNrows();
				variableCnt = cplex.getNcols();
				double startT = this.getCplexTime();
				ret = cplex.solve();
				double stopT = this.getCplexTime();
				currTime = Miscellaneous.round(stopT - startT, 2);
				currObj = Miscellaneous.round(cplex.getObjValue(), 2);
				solved = true;
				STFlow stFlowModel;
				Double[][][] xVar = getXVar();
				for (int s = 0; s < d; s++) {
					for (int t = s + 1; t < d; t++) {
						if (s != t) {
							stFlowModel = new STFlow(graph, xVar, s, t, singleFlowCplex);
							stFlowModel.solve(false, 3600);   // solve the max flow problem
							double stVal = stFlowModel.getObjectiveValue();
							STPair stPair = new STPair(s, t, stVal);
							if (stVal > tolerance) { // flow conservation not satisfied for {s,t}
								volatedCnt++;
								pairQueue.add(stPair);
								solved = false;
//								break;
							}
							else {
								satisfiedCnt++;
							}
						}
					}
//					if (pairQueue.size() > 5) break;
//					if (!solved) break;
				}
//				System.err.println("sat size: " + satPairs.size());
//				if (wrong > (wrong + correct)/4 ) {
//					pairQueue = leaveMatching(pairQueue);					
//				}
//				if (pairQueue.size() > 0 && pairQueue.peek().getDiff() < -0.99999999) {
//					solved = true;
//				}
//				else 
				iterationLog(fw, iter, currObj, currTime, satisfiedCnt, volatedCnt, pairQueue.size(), constraintCnt, variableCnt);
				if (!solved && pairQueue.size() > 0) {
//					addFlowConstraints(pairQueue,pairQueue.size());
//					prevAdded = pairQueue.peek();
//					fw.write("-------------------\n");
//					fw.write(pairQueue.peek().toString() + "\n");
//					fw.write("-------------------------\n");
					addFlowConstraints(pairQueue, pairQueue.size());
//					addFlowConstraints(pairQueue, 1);
				}
				// TODO: argument pairQueue.size() is not accurate, because some pairs can be omitted in add Flow constraints() method

				pairQueue.clear();
			} while (!solved);
			double totalExitTime = this.getCplexTime();
			exitLog(fw, currObj, totalExitTime - totalStartTime);
			return ret;
		} catch (IloException e) {
			e.printStackTrace();
			return false;
		}
//		}catch (IOException e) {
//			e.printStackTrace();
//			return false;
//		}
	}
	
	private void exitLog(FileWriter fw, double totalObj, double totalTime) {
		try {
			fw = new FileWriter(stLogFile, true);
			fw.write("TOTAL OBJECTIVE: " + totalObj + "\n");
			fw.write("TOTAL TIME: " + totalTime + "\n");
			fw.write(Constants.DELIMITER + Constants.DELIMITER + Constants.DELIMITER + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void iterationLog(FileWriter fw, int iter, double currObj, double currTime, int satisfiedCnt, int volatedCnt, int addedCnt,
			int constraintCnt, int variableCnt) {
		try {
			fw = new FileWriter(stLogFile, true);
			fw.write(iter + "\t" + currObj + "\t" + currTime + "\t" + satisfiedCnt + "\t" + volatedCnt + "\t" + addedCnt + "\t" + constraintCnt + "\t " + variableCnt + "\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	// write a header of the log
	private void initLog() {
		try {
			fw = new FileWriter(stLogFile, true);
			fw.write("ID: " + graph.getInstId() + " STRATEGY: " + this.toString() + " TOLERANCE: " + tolerance + "\n");
			fw.write("iter \t currObj \t currTime \t satCnt \t violCnt \t addedCnt \t conCnt \t varCnt \n");
			fw.close();
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
								cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
//								cplex.addLe(f[i][j][s][t], x[i][j][s]);
							}
						}
					}
				}
			}					
		} catch (IloException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * 
	 * @param queue Queue of constraints
	 * @param maxPCnt how many should be added
	 */
	public void addFlowConstraints(PriorityQueue<STPair> queue, int maxPCnt) {
		try {
			int cnt = 0;
			while (queue.size() > 0  && cnt < maxPCnt ) {
				cnt ++;
				STPair pair = queue.poll();
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
								expr2c.addTerm(1.0, f[s][i][t][s]);									// Flow conservation - dest
								expr2d.addTerm(1.0, f[i][s][t][s]);									// Flow conservation - dest
							}	
							IloLinearNumExpr expr1a = cplex.linearNumExpr();
							IloLinearNumExpr expr1b = cplex.linearNumExpr();	
							IloLinearNumExpr expr1c = cplex.linearNumExpr();
							IloLinearNumExpr expr1d = cplex.linearNumExpr();

							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(f[i][j][s][t], x[i][j][s]);				// capacity
									cplex.addLe(f[i][j][t][s], x[i][j][t]);				// capacity

									if (t != i && s != i) {
										if (j != s) {
											expr1a.addTerm(1.0, f[i][j][s][t]);									
											expr1c.addTerm(1.0, f[j][i][t][s]);									
										}								
										if (j != t) {								
											expr1b.addTerm(1.0, f[j][i][s][t]);
											expr1d.addTerm(1.0, f[i][j][t][s]);
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
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}	
	
	}
	
	private PriorityQueue<STPair> leaveMatching(PriorityQueue<STPair> stQueue) {
		System.out.println("---------------------------QUEUE--------------");
		System.out.println(stQueue.toString());
		if (stQueue.size() == 0) return stQueue;
		STPair[] inArray = stQueue.toArray(new STPair[stQueue.size()]);
		ArrayList<STPair> outArray = new ArrayList<STPair>();
		outArray.add(inArray[0]);
		for (int i = 1; i < inArray.length; i++) {
			STPair currPair = inArray[i];
			boolean add = true;
			for (STPair pair : outArray) {
				if (pair.getS() == currPair.getS() || pair.getS() == currPair.getT() || pair.getT() == currPair.getS() || pair.getT() == currPair.getT()) {
					add = false;
					break;
				}
			}
			if(add) {
				outArray.add(currPair);
			}
		}
		System.out.println(outArray.toString());
		return new PriorityQueue<STPair>(outArray);
	}
	
	public String toString() {
		return Constants.SMT_FLEXI_STRING;
	}
	

}
