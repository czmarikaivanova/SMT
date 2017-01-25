package model;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import graph.Graph;

public class SMTPF2Model extends ILPModel {

	public SMTPF2Model(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
	}

	protected IloNumVar[][] pz;
	protected IloNumVar[][][] py;
	protected IloNumVar[][][][] h;  // y hook
	protected IloNumVar[][][] y;	
	
	
	protected void initVars() {
		try {
			py = new IloNumVar[n][n][];
			h = new IloNumVar[n][n][d][];	
			y = new IloNumVar[n][n][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						h[i][j][k] = cplex.numVarArray(d,0,1);	
					}
					if (isLP) {
						y[i][j] = cplex.numVarArray(d, 0, 1);
					}
					else {
						y[i][j] = cplex.boolVarArray(d);
					}					
					py[i][j] = cplex.numVarArray(d,0,1);
				}					
			}
			pz = new IloNumVar[n][];				
			for (int j = 0; j < n; j++) {
				if (isLP) {
					pz[j] = cplex.numVarArray(n, 0, 1);					
				}
				else {
					pz[j] = cplex.boolVarArray(n);					
				}
			}	
		} catch (IloException e) {
			e.printStackTrace();
		}
	}	
	
	protected void createObjFunction() {
		try {
			// create model and solve it				
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							obj.addTerm(graph.getRequir(i,j), y[i][j][s]);
						}
					}
				}
			}
			cplex.addMinimize(obj);	
		} catch (IloException e) {
			e.printStackTrace();
		}			
	}	
	
	@Override
	public void createConstraints() {
		try {
			//no_flow_back
			
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						cplex.addEq(py[i][j][0], 0.0);
					}
				}
			}
			
			// flow1
			for (int t = 1; t < d; t++) { // must not be zero
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				IloLinearNumExpr expr2 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (t != j) {
						expr1.addTerm(1.0, py[j][t][t]);									
						expr2.addTerm(1.0, py[t][j][t]);
					}								
				}
				cplex.addEq(cplex.sum(expr1, cplex.negative(expr2)), 1.0);
			}		

			// flow2
			for (int i = 1; i < n; i++) { // must not be zero
				for (int t = 1; t < d; t++) { // must not be zero
					if (i != t) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();
						IloLinearNumExpr expr2 = cplex.linearNumExpr();
						for (int j = 0; j < n; j++) {
							if (i != j) {
								expr1.addTerm(1.0, py[j][i][t]);									
								expr2.addTerm(1.0, py[i][j][t]);
							}								
						}
						cplex.addEq(cplex.sum(expr1, cplex.negative(expr2)), 0.0);
					}
				}				
			}
			
			// flow3
			for (int k = 1; k < d; k++) { // must not be zero
				for (int l = 1; l < d; l++) {
					if (k != l) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();
						IloLinearNumExpr expr2 = cplex.linearNumExpr();
						for (int j = 1; j < n; j++) { // must not be zero
							expr1.addTerm(1.0, h[j][0][k][l]);									
							expr2.addTerm(1.0, h[0][j][k][l]);
						}
						cplex.addGe(cplex.sum(expr1, cplex.negative(expr2)), -1.0);
					}
				}
			}		

			// flow4
			for (int i = 1; i < n; i++) { // must not be zero
				for (int k = 1; k < d; k++) { // must not be zero
					for (int l = 1; l < d; l++) { // must not be zero
						if (k != l) {
							IloLinearNumExpr expr1 = cplex.linearNumExpr();
							IloLinearNumExpr expr2 = cplex.linearNumExpr();
							for (int j = 0; j < n; j++) {
								if (i != j) {
									expr1.addTerm(1.0, h[j][i][k][l]);									
									expr2.addTerm(1.0, h[i][j][k][l]);
								}								
							}
							cplex.addGe(cplex.sum(expr1, cplex.negative(expr2)), 0.0);
						}
					}
				}				
			}
			
			// h_py1
			for (int k = 1; k < d; k++) { // must not be zero
				for (int l = 1; l < d; l++) { // must not be zero
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(h[i][j][k][l], py[i][j][k]);
								}
							}
						}
					}
				}
			}
			
			// h_py2
			for (int k = 1; k < d; k++) { // must not be zero
				for (int l = 1; l < d; l++) { // must not be zero
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(h[i][j][k][l], py[i][j][l]);
								}
							}
						}
					}
				}
			}			
			
			// h_x_stronger
			for (int k = 1; k < d; k++) { // must not be zero
				for (int l = 1; l < d; l++) { // must not be zero
					if (k != l) {
						for (int i = 0; i < n; i++) {
							for (int j = 0; j < n; j++) {
								if (j != i) {
									cplex.addLe(cplex.sum(py[i][j][k], py[i][j][l], cplex.negative(h[i][j][k][l])), pz[i][j]);
								}
							}
						}
					}
				}
			}
			
			// steiner_flow_cons
			for (int i = d; i < n; i++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				IloLinearNumExpr expr2 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						expr1.addTerm(1.0, pz[j][i]);
						expr2.addTerm(1.0, pz[i][j]);
					}
				}
				cplex.addLe(cplex.sum(expr1, cplex.negative(expr2)), 0.0);
			}
			// yvar
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							IloLinearNumExpr expr7 = cplex.linearNumExpr();
							for (int k = 0; k < n; k++) {
								if ((graph.getRequir(i,k) >= graph.getRequir(i,j)) && (i != k)) {
									expr7.addTerm(1.0, y[i][k][s]);
								}
							}
							if (lazy) {
								cplex.addLazyConstraint((IloRange) cplex.le(cplex.sum(pz[i][j], py[j][i][s], cplex.negative(py[i][j][s])), expr7));								
							}
							else {
								cplex.addLe(cplex.sum(pz[i][j], py[j][i][s], cplex.negative(py[i][j][s])), expr7);								
							}
						}			
					}
				}					
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
	
	public void addValidInequalities() {
		try {
			super.addValidInequalities();
//			// y_sum=1
			for (int s = 0; s < d; s ++) {
				IloLinearNumExpr expr1 = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (j != s) {
						expr1.addTerm(1.0, y[s][j][s]);						
					}
				}
				cplex.addEq(expr1, 1.0);
			}

//			// x to nondest => y from there 
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					IloLinearNumExpr expr1 = cplex.linearNumExpr();
					IloLinearNumExpr expr2 = cplex.linearNumExpr();
					for (int i = 0; i < n; i++) {
						if (i != j) {
							expr1.addTerm(1.0, y[j][i][s]);
							expr2.addTerm(1.0, pz[i][j]);
							expr2.addTerm(-1.0, py[i][j][s]);
							expr2.addTerm(1.0, py[j][i][s]);
						}
					}
					cplex.addGe(expr1, expr2);
				}
			}			
			
//			// f imp y in nondest 
			for (int j = d; j < n; j ++) {
				for (int s = 0; s < d; s++) {
					for (int t = 0; t < d; t++) {
						for (int k = 0; k < n; k++) {
							if (j != k && s != t) {
								IloLinearNumExpr expr1 = cplex.linearNumExpr();
								IloLinearNumExpr expr2 = cplex.linearNumExpr();
								for (int i = 0; i < n; i++) {
									if (i != j) { 
										if (graph.getRequir(j, i) >= graph.getRequir(j, k)) {
											expr1.addTerm(1.0, py[i][j][s]);
											expr1.addTerm(1.0, py[j][i][t]);
											expr1.addTerm(-1.0, h[i][j][s][t]);
											expr1.addTerm(-1.0, h[j][i][s][t]);
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
////			
////			// vi4
			for (int s = 0; s < d; s++) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();
						IloLinearNumExpr expr2 = cplex.linearNumExpr();
						for (int t = 0; t < d; t++) {
							if (s != t) {
								expr1.addTerm(1.0, py[i][j][t]);
								expr1.addTerm(1.0, py[j][i][s]);
								expr1.addTerm(-1.0, h[i][j][s][t]);
								expr1.addTerm(-1.0, h[j][i][s][t]);
							}
						}
						if (j > i) {
							expr2.addTerm(1.0, pz[i][j]);
							expr2.addTerm(-1.0, py[i][j][s]);
							expr2.addTerm(1.0, py[j][i][s]);
							cplex.addLe(expr2, expr1);
						}
					}
				}
			}	
////			
////			// vi10
			for (int s = 0; s < d; s++) {
				for (int t1 = 0; t1 < d; t1++) {
					if (s != t1) {
						for (int t2 = 0; t2 < d; t2++) {
							if (s != t2 && t1 != t2) {
								for (int i = 0; i < n; i++) {
									for (int j = 0; j < n; j++) {
										if (i != j) {
											cplex.addLe(cplex.sum(pz[i][j], pz[j][i], cplex.negative(h[i][j][s][t2]), cplex.negative(h[j][i][s][t2])), cplex.sum(cplex.sum(pz[i][j], pz[j][i], cplex.negative(h[i][j][s][t1]), cplex.negative(h[j][i][s][t1])), cplex.sum(pz[i][j], pz[j][i], cplex.negative(h[i][j][t1][t2]), cplex.negative(h[j][i][t1][t2]))));
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public Double[][][] getPY() {
		try {
			Double[][][] xval = new Double[py.length][py.length][py.length];
			for (int i = 0 ; i < py.length; i++) {
				for (int j = 0; j < py.length; j++) {
					if (i != j) {
						for (int k = 0; k < py.length; k++) {
							xval[i][j][k] = cplex.getValue(py[i][j][k]);
						}
					}
				}
			}
			return xval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	
	
	public Double[][] getPZ() {
		try {
			Double[][] zval = new Double[pz.length][pz.length];
			for (int i = 0 ; i < pz.length; i++) {
				for (int j = 0; j < pz.length; j++) {
					if (i != j) {
						zval[i][j] = cplex.getValue(pz[i][j]);
						System.out.print(cplex.getValue(pz[i][j]) + " ");	
					}
				}
				System.out.println();
			}
			return zval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}

	@Override
	public Double[][] getZVar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Double[][][] getXVar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}	
}
