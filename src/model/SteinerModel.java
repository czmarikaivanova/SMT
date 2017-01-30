package model;

import java.util.ArrayList;

import graph.Clique;
import graph.ExtendedNode;
import graph.Graph;
import graph.Node;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;

import smt.Constants;

public class SteinerModel extends ILPModel {	
	
	public SteinerModel(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
	}
	
	protected IloNumVar[][][] x;
	
	protected IloNumVar[][][][] f;
	
	protected void initVars() {
		try {
			x = new IloNumVar[n][n][];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {			
					if (isLP) {
						x[i][j] = cplex.numVarArray(d, 0, 1);
					}
					else {
						x[i][j] = cplex.boolVarArray(d);						
					}

				}					
			}
			z = new IloNumVar[n][];				
			for (int j = 0; j < n; j++) {		
				if (isLP) {
					z[j] = cplex.numVarArray(n, 0, 1);					
				}
				else {
					z[j] = cplex.boolVarArray(n);
				}
			}									
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	protected void createObjFunction() {
		try {
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i < j) {
						obj.addTerm(graph.getRequir(i,j), z[i][j]);
					}
				}
			}
			cplex.addMinimize(obj);				
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	public void createConstraints() {
		try {
			// Size
			IloLinearNumExpr expr = cplex.linearNumExpr();				
			for (int i = 0; i < n; i++) {					
				for (int j = i+1; j < n; j++) {
					expr.addTerm(1.0, z[i][j]);
				}	
			}
			cplex.addLe(expr, n-1);				
			// OneDirDest
			for (int j = 0; j < d; j++) {					
				for (int s = 0; s < d; s++) {
					if (j != s) {
						IloLinearNumExpr expr1 = cplex.linearNumExpr();			
						for (int i = 0; i < n; i++) {
							if (i != j) {
								expr1.addTerm(1.0, x[i][j][s]);									
							}								
						}
						cplex.addEq(expr1, 1.0);
					}
				}	
			}		
			// OneDirNonDest_A
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					for (int k = 0; k < n; k++) {
						if (j != k) {
							IloLinearNumExpr expr2 = cplex.linearNumExpr();
							for (int i = 0; i < n; i++) {
								if (i != j) {
									expr2.addTerm(1.0, x[i][j][s]);
								}
							}
							cplex.addLe(x[j][k][s], expr2);
						}
					}
				}
			}
			// OneDirNonDest_B	
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					for (int k = 0; k < n; k++) {
						IloLinearNumExpr expr3 = cplex.linearNumExpr();
						for (int i = 0; i < n; i++) {
							if (i != j) {
								expr3.addTerm(1.0, x[i][j][s]);
							}
						}
						cplex.addLe(expr3, 1.0);
					}
				}
			}
			// NonDestNoLEaf
			for (int j = d; j < n; j++) {
				for (int s = 0; s < d; s++) {
					IloLinearNumExpr expr4 = cplex.linearNumExpr();
					for (int i = 0; i < n; i++) {
						if (i != j) {
							expr4.addTerm(1.0, x[i][j][s]);
						}
					}
					IloLinearNumExpr expr5 = cplex.linearNumExpr();
					for (int k = 0; k < n; k++) {
						if (j != k) {
							expr5.addTerm(1.0, x[j][k][s]);
						}
					}
					cplex.addLe(expr4, expr5);
				}
			}
			// OneDir
			for (int i = 0; i < n; i++) {
				for (int j = i+1; j < n; j++) {
					for (int s = 0; s < d; s++) {
						IloLinearNumExpr expr6 = cplex.linearNumExpr();								
						expr6.addTerm(1.0, x[i][j][s]);
						expr6.addTerm(1.0, x[j][i][s]);
						cplex.addEq(expr6, z[i][j]);						
					}
				}
			}
			// NoCycles
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < d; j++) {
					if (i != j) {
						cplex.addEq(0.0, x[i][j][j]);
					}
				}
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
			
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
	}
	
	public Double[][][] getXVar() {
		try {
			Double[][][] xval = new Double[x.length][x.length][x.length];
			for (int i = 0 ; i < x.length; i++) {
				for (int j = 0; j < x.length; j++) {
					if (i != j) {
						for (int k = 0; k < d; k++) {
//							if (k == 0 || k == 1)   {
//								System.out.print(i + " " + j + " " + k +" :" + cplex.getValue(x[i][j][k]) + " --");						
//							}
							xval[i][j][k] = cplex.getValue(x[i][j][k]);
						}
					}
				}
//				System.out.println();
			}
			return xval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	
	
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

	@Override
	public void addCrossCliqueConstraints(ArrayList<Clique> cliqueList) {
		try {
			for (Clique clique: cliqueList) {
				IloNumExpr[] varArray = new IloNumExpr[clique.size()];
				for (int k = 0; k < clique.size(); k++) {
					for (ExtendedNode en: clique) {
						int i = en.getOrigU().getId();
						int j = en.getOrigV().getId();
						varArray[k] = z[i][j];
					}
				}
				cplex.addLe(cplex.sum(varArray), 1.0);
			}
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}				
	
	public String toString() {
    	return Constants.SMT_STRING + "(" + n + "," + d + ")";
	}
	
	/**
	 * Add flow balance normal constraints for given indices determined after 
	 * a completed LP relaxation
	 * @param i
	 * @param s
	 * @param t
	 */
	public void addFlowBalanceNormalConstraints(ArrayList<Pair<Integer, Integer>> idxList) {  // TODO finish for fixed s t variables !!!!
		try {
			f = initF();
			for (Pair<Integer,  Integer> indices: idxList) {
				int s = indices.getValue0();
				int t = indices. getValue1();
				for (int i = 0; i < n; i++) {
					if (i != t) {
						if (i != s) {
							IloLinearNumExpr expr1a = cplex.linearNumExpr();
							IloLinearNumExpr expr1b = cplex.linearNumExpr();	
							for (int j = 0; j < n; j++) {
								if (i != j) {
									cplex.addLe(f[i][j][s][t], x[i][j][s]);
									if (j != s) { // j != s
										expr1a.addTerm(1.0, f[i][j][s][t]);	
									}
									if (j != t) { // j != t
										expr1b.addTerm(1.0, f[j][i][s][t]);
									}
								}
							}
							cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b)));
						}
						else { // i == s
							IloLinearNumExpr expr1a = cplex.linearNumExpr();
							IloLinearNumExpr expr1b = cplex.linearNumExpr();	
							for (int j = 0; j < n; j++) {
								if (s != j) {
									cplex.addLe(f[i][j][s][t], x[i][j][s]);
									if (j != s) { // j != s
										expr1a.addTerm(1.0, f[i][j][s][t]);	
									}
									if (j != t) { // j != t
										expr1b.addTerm(1.0, f[j][i][s][t]);
									}
								}
							}
							cplex.addEq(1,cplex.sum(expr1a, cplex.negative(expr1b)));
						}
					}
					else { // i == t
						IloLinearNumExpr expr1a = cplex.linearNumExpr();
						IloLinearNumExpr expr1b = cplex.linearNumExpr();	
						for (int j = 0; j < n; j++) {
							if (t != j) {
								cplex.addLe(f[i][j][s][t], x[i][j][s]);
								if (j != s) { // j != s
									expr1a.addTerm(1.0, f[i][j][s][t]);	
								}
								if (j != t) { // j != t
									expr1b.addTerm(1.0, f[j][i][s][t]);
								}
							}
						}
						cplex.addEq(-1,cplex.sum(expr1a, cplex.negative(expr1b)));
					}
				}
			}
		} catch (IloException e) {
			return;
		}
	}
	
	private IloNumVar[][][][] initF() {
		try {
			f = new IloNumVar[n][n][d][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						f[i][j][k] = cplex.numVarArray(d,0,1);	
					}	
				}					
			}
			return f;
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	public Double[][][][] getFVal() {
		try {
			Double[][][][] fVal =new Double[n][n][d][d];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						for (int s = 0; s < d; s++) {
							for (int t = 0; t < d; t++) {
								if (s != t) {
									fVal[i][j][s][t] = cplex.getValue(f[i][j][s][t]);
								}
							}
						}
					}
				}
			}
			return fVal;
		} catch (IloException e) {
			e.printStackTrace();
			return null;
		}
	}

}
	
	
	
