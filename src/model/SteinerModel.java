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
import org.javatuples.Quartet;

import smt.Constants;

public class SteinerModel extends ILPModel {	
	
	public SteinerModel(Graph graph, boolean willAddVIs, boolean isLP) {
		super(graph, willAddVIs, isLP);
	}
	
	protected int n; 
	protected int d;
	
	protected IloNumVar[][][] x;
	
	protected void initVars() {
		try {
			n = graph.getVertexCount();
			d = graph.getDstCount();
			cplex = new IloCplex();
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
	
	public void createConstraints() {
		try {
			// create model and solve it				
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i < j) {
						obj.addTerm(graph.getRequir(i,j), z[i][j]);
					}
				}
			}
			cplex.addMinimize(obj);				
			// -------------------------------------- constraints							
			
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
			
		//	cplex.addEq(z[0][1], 0);
			//cplex.addEq(z[0][1], 1);
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
						for (int k = 0; k < x.length; k++) {
//							System.out.print(i + " " + j + " " + k +" :" + cplex.getValue(x[i][j][k]) + " --");						
							xval[i][j][k] = cplex.getValue(x[i][j][k]);
						}
					}
				}
//				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
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
	
}
	
	
	
