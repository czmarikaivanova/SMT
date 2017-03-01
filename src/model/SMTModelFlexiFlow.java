package model;

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

public class SMTModelFlexiFlow extends SMTModel {

	public SMTModelFlexiFlow(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
	}
	
protected IloNumVar[][][][] f;

	public boolean solve(boolean useTimeLimit, int seconds) {
		try {
			cplex.setParam(IloCplex.DoubleParam.TiLim, seconds);
			PriorityQueue<STPair> pairQueue = new PriorityQueue<STPair>();
			boolean solved = false;
			boolean ret;
			IloCplex singleFlowCplex = new IloCplex();
			do {
				pairQueue.clear();
				int correct = 0;
				int wrong = 0;
				System.err.println(this.toString() + " CONSTRINT COUNT " + cplex.getNrows());
				System.err.println(this.toString() + " VARIABLE COUNT " + cplex.getNcols());
				double startT = this.getCplexTime();
				ret = cplex.solve();
				double stopT = this.getCplexTime();
				System.err.println("OBJECTIVE: "+ cplex.getObjValue());
				double time = stopT - startT;
				System.err.println("TIME: "+ time);
				//				this.getZVar();
				solved = true;
				STFlow stFlowModel;
				Double[][][] xVar = getXVar();
				for (int s = 0; s < d; s++) {
					for (int t = 0; t < d; t++) {
						if (s != t) {
							stFlowModel = new STFlow(graph, xVar, s, t, singleFlowCplex);
//							getFVal();
							stFlowModel.solve(false, 3600);
							double stVal = stFlowModel.getObjectiveValue();
							if (stVal > -0.99) {
								wrong++;
								pairQueue.add(new STPair(s, t, stVal));
								solved = false;
//									break;
							}
							else {
								correct++;
							}
						}
					}
//					if (!solved) break;
				}
				System.err.println("Correct: " + correct + "\n");
				System.err.println("Wrong: " + wrong + "\n");
				if (wrong > (wrong + correct)/10 ) {
					pairQueue = leaveMatching(pairQueue);					
				}
				if (pairQueue.size() > 0 && pairQueue.peek().getDiff() < -0.9) {
					solved = true;
				}
				addFlowConstraints(pairQueue,pairQueue.size());
//				addFlowConstraints(pairQueue,2);
				pairQueue.clear();
			} while (!solved);
			return ret;
		} catch (IloException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	private void initFlexiVars(int s, int t) {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
					try {
						f[i][j][s][t] = cplex.numVar(0, 1);
						f[i][j][t][s] = cplex.numVar(0, 1);
					} catch (IloException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
			}					
		}		
	}
	
	protected void initVars() {
//		try {
			super.initVars();
			f = new IloNumVar[n][n][d][d];		
//			for (int i = 0; i < n; i++) {
//				for (int j = 0; j < n; j++) {
//					for (int k = 0; k < d; k++) {
//						f[i][j][k] = cplex.numVarArray(d,0,1);	
//					}	
//				}					
//			}
//		} catch (IloException e) {
//			e.printStackTrace();
//		}
	}	

	
	public void createConstraints() {
			super.createConstraints();					
	}
	
	/**
	 * 
	 * @param queue Queue of constraints
	 * @param maxPCnt how many should be added
	 */
	public void addFlowConstraints(PriorityQueue<STPair> queue, int maxPCnt) {
		try {
			int cnt = 0;
			while (queue.size() > 0 && cnt < maxPCnt) {
				cnt ++;
				STPair pair = queue.poll();
				System.out.println(pair.toString());
				int s = pair.getS();
				int t = pair.getT();
				initFlexiVars(s, t);
				IloLinearNumExpr expr2a = cplex.linearNumExpr();
				IloLinearNumExpr expr2b = cplex.linearNumExpr();	
				IloLinearNumExpr expr3a = cplex.linearNumExpr();
				IloLinearNumExpr expr3b = cplex.linearNumExpr();	
				for (int i = 0; i < n; i++) {
					if (i != t) {
						expr2a.addTerm(1.0, f[t][i][s][t]);									
						expr2b.addTerm(1.0, f[i][t][s][t]);									
					}	
					if (i != s) {
						expr3a.addTerm(1.0, f[s][i][s][t]);									
						expr3b.addTerm(1.0, f[i][s][s][t]);									
					}				
					IloLinearNumExpr expr1a = cplex.linearNumExpr();
					IloLinearNumExpr expr1b = cplex.linearNumExpr();	
					for (int j = 0; j < n; j++) {
						if (j != i && s != t) {
							cplex.addLe(f[i][j][s][t], x[i][j][s]);			//capacity
							cplex.addEq(f[i][j][s][t], f[j][i][t][s]);		// f sym
						}
						if (t != i && s != i && s != t) {
							if (i != j && j != s) {
								expr1a.addTerm(1.0, f[i][j][s][t]);									
							}								
							if (i != j && j != t) {								
								expr1b.addTerm(1.0, f[j][i][s][t]);
							}	
						}
					}
					cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b))); // flow conservation - normal
				}
				cplex.addEq(-1,cplex.sum(expr2a, cplex.negative(expr2b))); // flow conservation - dest
				cplex.addEq(1,cplex.sum(expr3a, cplex.negative(expr3b)));    // flow conservation - source
				

					if (willAddVIs) {
										
							// vi10
//								for (int t1 = 0; t1 < d; t1++) {
//									if (s != t1) {
//										for (int t2 = 0; t2 < d; t2++) {
//											if (s != t2 && t1 != t2) {
//												for (int i = 0; i < n; i++) {
//													for (int j = 0; j < n; j++) {
//														if (i != j) {
//															cplex.addLe(f[i][j][s][t2],cplex.sum(f[i][j][s][t1], f[i][j][t1][t2]));
//														}
//													}
//												}
//											}
//										}
//									}
//								}
//								// y_sum=1
									IloLinearNumExpr exprYsum = cplex.linearNumExpr();
									for (int j = 0; j < n; j++) {
										if (j != s) {
											exprYsum.addTerm(1.0, y[s][j][s]);						
										}
									}
									cplex.addEq(exprYsum, 1.0);
								
//								// x to nondest => y from there 
								for (int j = d; j < n; j++) {
										IloLinearNumExpr expr1 = cplex.linearNumExpr();
										IloLinearNumExpr expr2 = cplex.linearNumExpr();
										for (int i = 0; i < n; i++) {
											if (i != j) {
												expr1.addTerm(1.0, y[j][i][s]);
												expr2.addTerm(1.0, x[i][j][s]);
											}
										}
										cplex.addGe(expr1, expr2);
								}			
//								// f imp y in nondest 
								for (int j = 0; j < n; j ++) {
											for (int k = 0; k < n; k++) {
												if (j != k && s != t) {
													IloLinearNumExpr expr1 = cplex.linearNumExpr();
													IloLinearNumExpr expr2 = cplex.linearNumExpr();
													for (int i = 0; i < n; i++) {
														if (i != j) { 
															if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
																expr1.addTerm(1.0, f[j][i][s][t]);
															}
															if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
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
