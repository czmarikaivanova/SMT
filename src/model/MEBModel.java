package model;

import org.javatuples.Quartet;

import graph.Graph;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class MEBModel extends ILPModel {

	public MEBModel(Graph graph, boolean allowCrossing) {
		super(graph, allowCrossing);

	}

	int n; 
	int d;
	private IloNumVar[][][] x;
	private IloNumVar[] p;		
	
	@Override
	public void createModel() {
		try {
			n = graph.getVertexCount();
			d = n - 1;
			cplex = new IloCplex();
			x = new IloNumVar[n][n][];
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
//					x[i][j] = cplex.numVarArray(d,0,1);
					x[i][j] = cplex.boolVarArray(d);
				

				}					
			}
			p = cplex.numVarArray(n, 0, 99999);
			
			z = new IloNumVar[n][];				
			for (int j = 0; j < n; j++) {
				//z[j] = cplex.numVarArray(n,0,1);		
				z[j] = cplex.boolVarArray(n);	
			}									
			
			// create model and solve it				
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for (int i = 0; i < n; i++) {
				obj.addTerm(1,p[i]);
			}
			cplex.addMinimize(obj);				
			// -------------------------------------- constraints							
			
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
						expr.addTerm(requir[i][j], z[i][j]);
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
			
			if (allowCrossing) {
				// crossing 1
				for (Quartet<Integer, Integer, Integer, Integer> crossPair: crossList) {
					int i = crossPair.getValue0();
					int j = crossPair.getValue1();
					int k = crossPair.getValue2();
					int l = crossPair.getValue3();				
					cplex.addLe(cplex.sum(z[i][j], z[k][l]), 1.0);
					cplex.addLe(cplex.sum(z[i][j], z[l][k]), 1.0);
					cplex.addLe(cplex.sum(z[j][i], z[k][l]), 1.0);
					cplex.addLe(cplex.sum(z[j][i], z[l][k]), 1.0);
				}
				
				// crossing 2
//				for (Quartet<Integer, Integer, Integer, Integer> crossPair: crossList) {
//					int i = crossPair.getValue0();
//					int j = crossPair.getValue1();
//					int k = crossPair.getValue2();
//					int l = crossPair.getValue3();				
//					cplex.addLe(cplex.sum(z[i][j], z[k][l], z[l][k], z[j][i]), 1.0);
//				}			
			}
			
		} catch (IloException e) {
			System.err.println("Concert exception caught: " + e);
		}	
	}

//	@Override
//	
//	/**
//	 *  Feed data structure from the AMPL file
//	 */
//	public void populate() {
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(amplDataFile));
//			String line;
//			while ((line = br.readLine()) != null) {
//				if (line.matches("param.*")) {
//				//	append(); // crate array vertices
//					requir = new double[graph.getVertexCount()][graph.getVertexCount()];
//					String parLine;
//					while ((parLine = br.readLine()) != null) {							
//						String[] entries = parLine.split("\t");
//						if (entries.length > 3) { // skip the last line
//							for (String s: entries) {
//								s = s.trim();
//								String[] entry = s.split(" ");
//								requir[Integer.parseInt(entry[0])][Integer.parseInt(entry[1])] = Double.parseDouble(entry[2]);	
//							}
//						}
//					}
//				}					
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {				
//			e.printStackTrace();
//		}
//	}
	
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
	
	public boolean[][] getZVar() {
		try {
			boolean[][] zval = new boolean[z.length][z.length];
			for (int i = 0 ; i < z.length; i++) {
				for (int j = 0; j < z.length; j++) {
					if (i != j) {
						System.out.print(cplex.getValue(z[i][j]) + " ");						
						zval[i][j] = cplex.getValue(z[i][j]) < 0.5 ? false : true;						
					}
				}
				System.out.println();
			}
			System.out.println("Objective: " + cplex.getObjValue());
			writeZ();
			cplex.end();

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
	


}
