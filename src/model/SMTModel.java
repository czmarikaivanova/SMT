package model;

import java.util.ArrayList;

import graph.Clique;
import graph.Graph;
import graph.Node;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import org.javatuples.Quartet;

public class SMTModel extends ILPModel {	
	
	public SMTModel(Graph graph, boolean allowCrossing) {
		super(graph, allowCrossing);
	}
	
	protected int n; 
	protected int d;
	
	protected IloNumVar[][][] x;
	protected IloNumVar[][][] y;	
	
	protected void initVars() {
		try {
			n = graph.getVertexCount();
			d = graph.getDstCount();
			cplex = new IloCplex();
			x = new IloNumVar[n][n][];
			y = new IloNumVar[n][n][];				
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {				
					x[i][j] = cplex.boolVarArray(d);
					y[i][j] = cplex.boolVarArray(d);				
				}					
			}
			z = new IloNumVar[n][];				
			for (int j = 0; j < n; j++) {		
				z[j] = cplex.boolVarArray(n);	
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
					if (i != j) {
						for (int s = 0; s < d; s++) {
							obj.addTerm(graph.getRequir(i,j), y[i][j][s]);
						}
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
	
	//		// YVar
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
//							cplex.addLazyConstraint((IloRange) cplex.le(x[i][j][s], expr7));
							cplex.addLe(x[i][j][s], expr7);
						}			
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
			cplex.end();
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
				IloNumExpr[] varArray = new IloNumExpr[clique.size() * 2];
				for (int k = 0; k < clique.size(); k++) {
					int i = clique.get(k).getOrigU().getId();
					int j = clique.get(k).getOrigV().getId();
					varArray[k] = z[i][j];
					varArray[clique.size() + k] = z[j][i];
				}
				cplex.addLe(cplex.sum(varArray), 1.0);
			}
		} catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}				
	
}
	
	
	
