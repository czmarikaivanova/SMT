package model;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import graph.Graph;

public class SMTMultiFlowModelVI extends SMTMultiFlowModel {

	public SMTMultiFlowModelVI(Graph graph, boolean allowCrossing) {
		super(graph, allowCrossing);
	}
	
	@Override
	public void createConstraints() {
		try {
			super.createConstraints();
			// create model and solve it							
			// -------------------------------------- constraints							
			
			// capacity
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j > i && s != t) {
								cplex.addLe(f[i][j][s][t], x[i][j][s]);
							}
						}
					}
				}
			}	
			
			// capacity 2
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j > i && s != t) {
								cplex.addLe(f[i][j][s][t], x[j][i][t]);
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
							if (j > i && s != t) {
								cplex.addEq(f[i][j][s][t], f[j][i][t][s]);
							}
						}
					}
				}
			}		
			
			// vi3
			for (int s = 0; s < d; s++) {
				for (int t = 0; t < d; t++) {
					for (int i = 0; i < n; i++) {
						for (int j = 0; j < n; j++) {
							if (j > i && s != t) {
								cplex.addLe(cplex.sum(x[i][j][s], x[j][i][t]), cplex.sum(1,f[i][j][s][t]));
							}
						}
					}
				}
			}					
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
			
			// vi5
			for (int s = 0; s < d; s++) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < d; j++) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();
							cplex.addLe(x[i][j][s], x[j][i][j]);
					}
				}
			}				
			// vi6
			for (int s = 0; s < d; s++) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < d; j++) {
						cplex.addEq(x[i][j][s], f[j][i][j][s]);
					}
				}
			}	
						
			// vi7
			for (int s = 0; s < d; s++) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < d; j++) {
						cplex.addEq(x[i][j][s], f[i][j][s][j]);
					}
				}
			}	
						
//			// vi8
			for (int s = 0; s < d; s++) {
				for (int t1 = 0; t1 < d; t1++) {
					if (s != t1) {
						for (int t2 = 0; t2 < d; t2++) {
							if (s != t2 && t1 != t2) {
								for (int i = 0; i < n; i++) {
									for (int j = 0; j < n; j++) {
										if (i != j) {
											cplex.addLe(cplex.sum(f[i][j][s][t1], f[j][i][s][t2]), cplex.sum(2,cplex.negative(f[i][j][t1][t2])));
										}
									}
								}
							}
						}
					}
				}
			}				
//			// vi9
			for (int s = 0; s < d; s++) {
				for (int t1 = 0; t1 < d; t1++) {
					if (s != t1) {
						for (int t2 = 0; t2 < d; t2++) {
							if (s != t2 && t1 != t2) {
								for (int i = 0; i < n; i++) {
									for (int j = 0; j < n; j++) {
										if (i != j) {
											cplex.addLe(cplex.sum(f[i][j][s][t1], f[j][i][s][t2]), cplex.sum(2,cplex.negative(f[j][i][t1][t2])));
										}
									}
								}
							}
						}
					}
				}
			}				
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
    	return Constants.SMT_MULTI_FLOW_VI_STRING + "(" + Integer.toString(n) + "," + Integer.toString(d) + ")";
	}
}
