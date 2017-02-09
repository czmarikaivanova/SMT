package model;

import graph.Clique;
import graph.Graph;
import graph.Node;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;

import org.javatuples.Quartet;

public class SteinerPF2Model extends ILPModel {

	public SteinerPF2Model(Graph graph, boolean willAddVIs, boolean isLP, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
	}
	
	
	protected IloNumVar[][] pz;
	protected IloNumVar[][][] py;
	protected IloNumVar[][][][] h;
	
	protected void initVars() {
		try {
			py = new IloNumVar[n][n][];
			h = new IloNumVar[n][n][d][];		
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					for (int k = 0; k < d; k++) {
						h[i][j][k] = cplex.numVarArray(d,0,1);	
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
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i != j) {
						obj.addTerm(graph.getRequir(i,j), pz[i][j]);
					}
				}
			}
			cplex.addMinimize(obj);				
		} catch (IloException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void createConstraints() {
		try {
	
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
			
			// h_x1
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
			
			// h_x2
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
			
			
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}		
		
	}
	
	public void addLazyConstraints() {
		
	}

	@Override
	public void addCrossCliqueConstraints(ArrayList<Clique> cliqueList) {
		// TODO Auto-generated method stub
		
	}

	public Double[][][] getXVar() {
		try {
			Double[][][] xval = new Double[py.length][py.length][py.length];
			for (int i = 0 ; i < py.length; i++) {
				for (int j = 0; j < py.length; j++) {
					if (i != j) {
						for (int k = 0; k < py.length; k++) {
//							System.out.print(i + " " + j + " " + k +" :" + cplex.getValue(x[i][j][k]) + " --");						
							xval[i][j][k] = cplex.getValue(py[i][j][k]);
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
	public String toString() {
		// TODO Auto-generated method stub
		return "Steiner PF2";
	}

	@Override
	public Double[][] getZVar() {
		return getPZ();
	}

}
