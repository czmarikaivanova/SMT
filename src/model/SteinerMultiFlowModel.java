package model;

import java.util.ArrayList;

import org.javatuples.Quartet;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import graph.Clique;
import graph.Graph;
import graph.Node;

public class SteinerMultiFlowModel extends SteinerModel {

	public SteinerMultiFlowModel(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
	}
	
	protected IloNumVar[][][][] f;
	
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
		try {
			super.createConstraints();
			// Flow conservation - normal
			for (int s = 0; s < d; s++) {					
				for (int t = 0; t < d; t++) {
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
				}	
			}		
			
			// Flow conservation - dest
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					if (s != t) {
						IloLinearNumExpr expr2a = cplex.linearNumExpr();
						IloLinearNumExpr expr2b = cplex.linearNumExpr();	
						for (int i = 0; i < n; i++) {
							if (i != t) {
								expr2a.addTerm(1.0, f[t][i][s][t]);									
								expr2b.addTerm(1.0, f[i][t][s][t]);									
							}								
						}
						cplex.addEq(-1,cplex.sum(expr2a, cplex.negative(expr2b)));
					}
				}
			}				

			// Flow conservation - source
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					if (s != t) {
						IloLinearNumExpr expr2a = cplex.linearNumExpr();
						IloLinearNumExpr expr2b = cplex.linearNumExpr();	
						for (int i = 0; i < n; i++) {
							if (i != s) {
								expr2a.addTerm(1.0, f[s][i][s][t]);									
								expr2b.addTerm(1.0, f[i][s][s][t]);									
							}								
						}
						cplex.addEq(1,cplex.sum(expr2a, cplex.negative(expr2b)));
					}
				}
			}				
			
			
			// capacity
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
								cplex.addLe(f[i][j][s][t], x[i][j][s]);
							}
						}
					}
				}
			}					
			
			// f sym
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j != i && s != t) {
								cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
							}
						}
					}
				}
			}					
			if (willAddVIs) {
				addValidInequalities();
			}
			
			// crossing
			if (!allowCrossing) {
				for (Quartet<Node, Node, Node, Node> crossPair: graph.getCrossList()) {
					int i = crossPair.getValue0().getId();
					int j = crossPair.getValue1().getId();
					int k = crossPair.getValue2().getId();
					int l = crossPair.getValue3().getId();				
					cplex.addLe(cplex.sum(z[i][j], z[k][l], z[l][k], z[j][i]), 1.0);
				}	
			}
			
		//	cplex.addEq(z[0][1], 0);
			//cplex.addEq(z[0][1], 1);
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}

	public void addValidInequalities() {
		try {
			// vi3
//			for (int s = 0; s < d; s++) {
//				for (int t = 0; t < d; t++) {
//					for (int i = 0; i < n; i++) {
//						for (int j = 0; j < n; j++) {
//							if (j > i && s != t) {
//								cplex.addLe(cplex.sum(x[i][j][s], x[j][i][t]), cplex.sum(1,f[i][j][s][t]));
//							}
//						}
//					}
//				}
//			}					
			// vi4
			for (int s = 0; s < d; s++) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();
						for (int t = 0; t < d; t++) {
							if (s != t)
							expr1.addTerm(1.0, f[i][j][s][t]);
						}
						if (j > i) {
							cplex.addLe(x[i][j][s], expr1);
						}
					}
				}
			}	
			
//			// vi5
//			for (int s = 0; s < d; s++) {
//				for (int i = 0; i < n; i++) {
//					for (int j = 0; j < d; j++) {
//							cplex.addLe(x[i][j][s], x[j][i][j]);
//					}
//				}
//			}				
//			// vi6
//			for (int s = 0; s < d; s++) {
//				for (int i = 0; i < n; i++) {
//					for (int j = 0; j < d; j++) {
//						cplex.addEq(x[i][j][s], f[j][i][j][s]);P
//					}
//				}
//			}	
//						
//			// vi7
//			for (int s = 0; s < d; s++) {
//				for (int i = 0; i < n; i++) {
//					for (int j = 0; j < d; j++) {
//						cplex.addEq(x[i][j][s], f[i][j][s][j]);
//					}
//				}
//			}	
						
////			// vi8
//			for (int s = 0; s < d; s++) {
//				for (int t1 = 0; t1 < d; t1++) {
//					if (s != t1) {
//						for (int t2 = 0; t2 < d; t2++) {
//							if (s != t2 && t1 != t2) {
//								for (int i = 0; i < n; i++) {
//									for (int j = 0; j < n; j++) {
//										if (i != j) {
//											cplex.addLe(cplex.sum(f[i][j][s][t1], f[j][i][s][t2]), cplex.sum(2,cplex.negative(f[i][j][t1][t2])));
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}				
////			// vi9
//			for (int s = 0; s < d; s++) {
//				for (int t1 = 0; t1 < d; t1++) {
//					if (s != t1) {
//						for (int t2 = 0; t2 < d; t2++) {
//							if (s != t2 && t1 != t2) {
//								for (int i = 0; i < n; i++) {
//									for (int j = 0; j < n; j++) {
//										if (i != j) {
//											cplex.addLe(cplex.sum(f[i][j][s][t1], f[j][i][s][t2]), cplex.sum(2,cplex.negative(f[j][i][t1][t2])));
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}				
			// vi10
			for (int s = 0; s < d; s++) {
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
			}		
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}	
	}	

	public String toString() {
    	return "Steiner_MULTI_FLOW ";
	}

	@Override
	public void addCrossCliqueConstraints(ArrayList<Clique> cliqueList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Double[][] getZVar() {
		try {
			Double[][] zval = new Double[z.length][z.length];
			for (int i = 0 ; i < z.length; i++) {
				for (int j = i+1; j < z.length; j++) {
					System.out.print(cplex.getValue(z[i][j]) + " ");						
					zval[i][j] = cplex.getValue(z[i][j]);						
				}
				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
			return zval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}


		


}
