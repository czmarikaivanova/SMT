package model;

import smt.Constants;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import graph.Graph;

public class SMTF1 extends ILPModel {

	public SMTF1(Graph graph , boolean isLP, boolean excludeC) {
		super(graph, isLP, excludeC);
//		System.err.println(this.toString() + " CONSTRINT COUNT " + cplex.getNrows());
//		System.err.println(this.toString() + " VARIABLE COUNT " + cplex.getNcols());
	}

	protected IloNumVar[][] pz;
	protected IloNumVar[][][] py;
	protected IloNumVar[][][] y;	
	
	
	protected void initVars() {
		try {
			py = new IloNumVar[n][n][];
			y = new IloNumVar[n][n][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
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
//			 obvious
			for (int i = 0; i < n; i++) {
				for (int t = 1; t < d; t++) {
					if (i != t) {
						cplex.addEq(py[i][t][t], pz[i][t]);  // 3l
						cplex.addEq(py[t][i][t], 0.0);  // 3k
					}
				}
			}	
			//no_flow_back
			for (int i = 0; i < n; i++) {
				cplex.addEq(pz[i][0], 0.0);
				for (int j = 0; j < n; j++) {
					if (i != j) {
//						cplex.addEq(py[i][j][0], 0.0);  // f  -- probably implied by x (see flow conservation plus f_x_rel
						cplex.addEq(pz[i][0], 0.0);		// x
					}
				}
			}
			// steiner_flow_cons
			if (lazy) {
				System.err.println("INCLUDING NONDEST FLOW****");
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
			}
			else {
				System.err.println("NO NONDEST FLOW");
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
				for (int t = 0; t < d; t++) { // must not be zero OR CAN BE???
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
			
			// h_x_weaker
			for (int k = 0; k < d; k++) { // must not be zero OR CAN BE???
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						if (j != i) {
							cplex.addLe(py[i][j][k], pz[i][j]);
						}
					}
				}
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

			// (B) - necessary, see instance in ..\pictures
			for (int i = d; i < n; i++) {
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for (int j = 0; j < n; j++) {
					if (i != j) {
						expr.addTerm(1.0, pz[j][i]);
					}
				}
				cplex.addLe(expr, 1.0);  
			}
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}

	public Double[][][] getPY() {
		try {
			Double[][][] xval = new Double[n][n][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
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
//						if (j == 2 && cplex.getValue(pz[i][j]) > 0) {
							System.out.print(i + " " + j + " " +" :" + cplex.getValue(pz[i][j]) + " --");	
//						}
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
		return getPZ();
	}

	@Override
	public Double[][][] getXVar() {
		try {
			System.err.println("FFFFFF:");
			Double[][][] xval = new Double[n][n][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
//							if (j == 2 &&  cplex.getValue(py[i][j][k]) > 0 || k == 5 && cplex.getValue(py[i][j][k])> 0) {
							if (k == 2 && cplex.getValue(py[i][j][k]) > 0  ) {
								System.out.print(i + " " + j + " " + k +" :" + cplex.getValue(py[i][j][k]) + " --");	
							}
							System.out.print(i + " " + j + " " + k +" :" + cplex.getValue(py[i][j][k]) + " --");
							xval[i][j][k] = cplex.getValue(py[i][j][k]);
						}
					}
				}
				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
			return xval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	
	
	public Double[][][] getYVar() {
		try {
			Double[][][] yval = new Double[n][n][d];
			for (int i = 0 ; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
//							if (j == 2 &&  cplex.getValue(py[i][j][k]) > 0 || k == 5 && cplex.getValue(py[i][j][k])> 0) {
//							if (k == 2 && cplex.getValue(py[i][j][k]) > 0  ) {
//								System.out.print(i + " " + j + " " + k +" :" + cplex.getValue(py[i][j][k]) + " --");	
//							}
							yval[i][j][k] = cplex.getValue(y[i][j][k]);
							System.out.print(i + " " + j + " " + k +" :" + cplex.getValue(y[i][j][k]) + " --");
						}
					}
				}
				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
			return yval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	

	@Override
	public String toString() {
    	return Constants.SMT_PF2_STRING + "(" + n + "," + d + ")";
	}
}
