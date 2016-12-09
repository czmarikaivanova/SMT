package model;

import java.util.ArrayList;

import org.javatuples.Quartet;

import smt.Constants;

import graph.Clique;
import graph.ExtendedNode;
import graph.Graph;
import graph.Node;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class MEBModel extends ILPModel {

	public MEBModel(Graph graph, boolean willAddVIs, boolean isLP, boolean allowCrossing, boolean lazy) {
		super(graph, willAddVIs, isLP, lazy);
		this.allowCrossing = allowCrossing;
	}

	int n; 
	int d;
	protected IloNumVar[][][] x;
	protected IloNumVar[] p;		
	
	@Override
	protected void initVars() {
		n = graph.getVertexCount();
		d = n - 1;
		try {
			cplex = new IloCplex();
			x = new IloNumVar[n][n][];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (isLP) {
						x[i][j] = cplex.numVarArray(d,0,1);
					}
					else {
						x[i][j] = cplex.boolVarArray(d);						
					}
				}					
			}
			p = cplex.numVarArray(n, 0, 99999);
			
			z = new IloNumVar[n][];				
			for (int j = 0; j < n; j++) {
				if (isLP) {
					z[j] = cplex.numVarArray(n,0,1);	
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
				obj.addTerm(1,p[i]);
			}
			cplex.addMinimize(obj);	
		} catch (IloException e) {
			e.printStackTrace();
		}			
	}

	@Override
	public void createConstraints() {
		try {
			// Size
			IloLinearNumExpr expr0 = cplex.linearNumExpr();				
			for (int i = 0; i < n; i++) {					
				for (int j = 0; j < n; j++) {
					if (i != j) {
						expr0.addTerm(1.0, z[i][j]);
					}
				}	
			}			
			cplex.addLe(expr0, n-1);					
			
			// Assignment						
			for (int i = 0; i < n; i++) {					
				for (int j = 0; j < n; j++) {
					if (i != j) {
						IloLinearNumExpr expr = cplex.linearNumExpr();	
						expr.addTerm(graph.getRequir(i,j), z[i][j]);
						cplex.addLe(expr, p[i]);	
					}
				}	
			}
			
			// Flow conservation - normal
			for (int k = 0; k < d; k++) {					
				for (int i = 0; i < d; i++) {
					if (k != i) {
						IloLinearNumExpr expr1a = cplex.linearNumExpr();
						IloLinearNumExpr expr1b = cplex.linearNumExpr();	
						for (int j = 0; j < d; j++) {
							if (i != j) {
								expr1a.addTerm(1.0, x[i][j][k]);									
							}								
						}
						for (int j = 0; j < n; j++) {
							if (i != j) {								
								expr1b.addTerm(1.0, x[j][i][k]);
							}								
						}						
						cplex.addEq(0,cplex.sum(expr1a, cplex.negative(expr1b)));
					}
				}	
			}		
			
			// Flow conservation - dest
			for (int k = 0; k < d; k++) {					
				IloLinearNumExpr expr2a = cplex.linearNumExpr();
				IloLinearNumExpr expr2b = cplex.linearNumExpr();	
				for (int i = 0; i < n; i++) {
					if (i != k) {
						expr2a.addTerm(1.0, x[k][i][k]);									
						expr2b.addTerm(1.0, x[i][k][k]);									
					}								
				}
				cplex.addEq(-1,cplex.sum(expr2a, cplex.negative(expr2b)));
			}				
			
			// capacity
			for (int k = 0; k < d; k++) {
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						if (j != i) {
							IloLinearNumExpr expr3 = cplex.linearNumExpr();
							expr3.addTerm(1.0, x[i][j][k]);
							cplex.addLe(expr3, z[i][j]);
						}
					}
				}
			}		
			
			if (!allowCrossing) {
				// crossing 2
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
	
	
	public double[] getPVar() {
		try {
			System.out.println("PVAL: ");
			double[] pval = new double[p.length];
			for (int i = 0 ; i < p.length; i++) {
				System.out.print("p"+i+ " = " + cplex.getValue(p[i]) + " ");						
				pval[i] = cplex.getValue(p[i]);						
				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
			cplex.end();
			return pval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}	
	
	public Double[][] getZVar() {
		try {
			Double[][] zval = new Double[z.length][z.length];
			for (int i = 0 ; i < z.length; i++) {
				for (int j = 0; j < z.length; j++) {
					if (i != j) {
//						System.out.print(cplex.getValue(z[i][j]) + " ");						
						zval[i][j] = cplex.getValue(z[i][j]);						
					}
				}
				System.out.println();
			}
			System.out.println("Objective: " + getObjectiveValue());
//			writeZ();
			return zval;		
		} catch (IloException e) {			
			e.printStackTrace();
			return null;
		}		
	}		
	
	public void writeZ() {
		for (int i = 0 ; i < z.length; i++) {
			for (int j = 0; j < z.length; j++) {
				if (i != j) {
					try {
						System.out.print("[" + i + ", " + j + "] = " + cplex.getValue(z[i][j]) + " ");
					} catch (IloException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}										
				}
			}
			System.out.println();
		}
	}
	
    protected String genAmplHead() {
        String dstStr = "set DESTS :=";
        String nonDstStr = "set NONDESTS :=";
        for (int i = 0; i < graph.getVertexCount(); i++) {
            if (i < graph.getDstCount() - 1) {
                dstStr += " " + i ;
            } else {
                nonDstStr += " " + i;
            }
        }
        dstStr += " ;\n";
        nonDstStr += " ;\n";
        return dstStr + nonDstStr;
    }

    public String toString() {
    	return Constants.MEB_STRING + (isLP ? "_LP" : "") + "(" + n + ", " + d + ")";
    }

	@Override
	public Double[][][] getXVar() {
		// TODO Auto-generated method stub
		return null;
	}
	


}
