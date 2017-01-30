package model;

import java.util.ArrayList;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import org.javatuples.Pair;
import org.javatuples.Quartet;

import graph.Graph;
import graph.Node;

public class SMTModelFlexiFlow extends SMTModel {

	public SMTModelFlexiFlow(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
	}
	
//protected IloNumVar[][][][] f;

	public boolean solve(boolean useTimeLimit, int seconds) {
		try {
			cplex.setParam(IloCplex.DoubleParam.TiLim, seconds);
			ArrayList<Pair<Integer, Integer>> stPairs = new ArrayList<Pair<Integer,Integer>>();
			boolean solved = false;
			boolean ret;
			stPairs.add(new Pair<Integer, Integer>(0, 1));
			this.addFlowConstraints(stPairs);
			stPairs.clear();
			do {
				int correct = 0;
				int wrong = 0;
				ret = cplex.solve();
				System.err.println("OBJECTIVE: "+ cplex.getObjValue());
//				this.getZVar();
				solved = true;
				for (int s = 0; s < d; s++) {
					for (int t = 0; t < d; t++) {
						if (s != t) {
							STFlow stFlowModel = new STFlow(graph, getXVar(), s, t);
//							getFVal();
							stFlowModel.solve(false, 0);
							if (!stFlowModel.solve(false, 0)) {
								wrong++;
								stPairs.add(new Pair<Integer, Integer>(s, t));
								solved = false;
//								if (wrong > 4) {
//									break;
//								}
							}
							else {
								correct++;
							}
						}
					}
//					if (!solved && wrong > 4) break;
//					if (!solved) break;
				}
				System.err.println("Correct: " + correct);
				System.err.println("Wrong: " + wrong);
				addFlowConstraints(stPairs);
			} while (!solved);
			return ret;
		} catch (IloException e) {
			e.printStackTrace();
			return false;
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
			super.createConstraints();					
	}
	
	public void addFlowConstraints(ArrayList<Pair<Integer, Integer>> stPairs) {
		try {
			for (Pair<Integer, Integer> pair: stPairs) {
				int s = pair.getValue0();
				int t = pair.getValue1();
				// Flow conservation - normal
//				for (int s = 0; s < d; s++) {					
//					for (int t = 0; t < d; t++) {
						for (int i = 0; i < n; i++) {						
							if (t != i && s != i && s != t) {
								IloLinearNumExpr expr1a = cplex.linearNumExpr();
								IloLinearNumExpr expr1b = cplex.linearNumExpr();	
								for (int j = 0; j < n; j++) {
									if (i != j && j != s) {
										expr1a.addTerm(1.0, f[i][j][s][t]);									
									}								
								}
								for (int j = 0; j < n; j++) {
									if (i != j && j != t) {								
										expr1b.addTerm(1.0, f[j][i][s][t]);
									}								
								}						
								cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b)));
							}
						}
//					}	
//				}		
				
				// Flow conservation - dest
//				for (int s = 0; s < d; s++) {
//					for (int t = 0; t < d; t++) {
//						if (s != t) {
							IloLinearNumExpr expr2a = cplex.linearNumExpr();
							IloLinearNumExpr expr2b = cplex.linearNumExpr();	
							for (int i = 0; i < n; i++) {
								if (i != t) {
									expr2a.addTerm(1.0, f[t][i][s][t]);									
									expr2b.addTerm(1.0, f[i][t][s][t]);									
								}								
							}
							cplex.addEq(-1,cplex.sum(expr2a, cplex.negative(expr2b)));
//						}
//					}
//				}				

				// Flow conservation - source
//				for (int s = 0; s < d; s++) {
//					for (int t = 0; t < d; t++) {
//						if (s != t) {
							IloLinearNumExpr expr3a = cplex.linearNumExpr();
							IloLinearNumExpr expr3b = cplex.linearNumExpr();	
							for (int i = 0; i < n; i++) {
								if (i != s) {
									expr3a.addTerm(1.0, f[s][i][s][t]);									
									expr3b.addTerm(1.0, f[i][s][s][t]);									
								}								
							}
							cplex.addEq(1,cplex.sum(expr3a, cplex.negative(expr3b)));
//						}
//					}
//				}				
				
				
				// capacity
//				for (int s = 0; s < d; s++) {
//					for (int t = 0; t < d; t++) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i && s != t) {
									cplex.addLe(f[i][j][s][t], x[i][j][s]);
								}
							}
						}
//					}
//				}					
				
				// f sym
//				for (int s = 0; s < d; s++) {
//					for (int t = 0; t < d; t++) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i && s != t) {
									cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
								}
							}
						}
//					}
//				}	
						
					if (willAddVIs) {
										
							// vi10
								for (int t1 = 0; t1 < d; t1++) {
									if (s != t1) {
										for (int t2 = 0; t2 < d; t2++) {
											if (s != t2 && t1 != t2) {
												for (int i = 0; i < n; i++) {
													for (int j = 0; j < n; j++) {
														if (i != j) {
															cplex.addLe(f[i][j][s][t2],cplex.sum(f[i][j][s][t1], f[i][j][t1][t2]));
														}
													}
												}
											}
										}
									}
								}
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
	
}
