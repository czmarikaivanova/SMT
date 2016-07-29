package smt;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import logs.ViLogger;
import model.ILPModel;
import model.MEBModel;
import model.SMTModel;
import model.SMTModelLP;
	
	public class Main {

	    static Node[] nodes;
	    static int vertexCount;
	    static int dstCount;
	    static int nodeCount =12;
	    
	    private static ILPModel model;
		/**
		 * @param args
		 * @throws IloException 
		 */
		public static void main(String[] args) throws IloException {		

			int iter = 1;
			ArrayList<Integer> crossList = new ArrayList<Integer>();
			boolean generate = false;
			boolean draw = true;
			Random rnd = new Random();
			int instId = rnd.nextInt(100000);	
			for (int i = 0; i < iter; i++) {
				File amplFile = prepareAMPL(generate, instId);
				model = new MEBModel(amplFile);
				model.populate();	
				model.createModel();
				model.solve();
				boolean[][] z = model.getZVar();
				if (hasCrossing(z)) {
					crossList.add(instId);
				}
				if (draw) {
					if (model instanceof MEBModel) {
						draw(z, instId, true);						
					}
					else {
						draw(z, instId, false);
					}
						
				}			
				System.err.println("Instances with crossing: ");
				for (Integer c: crossList) {
					System.err.println(c + "");	
				}	
			}				
		}
			
		
		private static File prepareAMPL(boolean generate, int instId) {
			File amplFile;
			if (generate) {
				generatePoints(vertexCount);		
				amplFile = model.generateAMPLData(instId, vertexCount, dstCount, nodes);
			}
			else {
				createPoints();
				instId = -1;
				amplFile = model.generateAMPLData(-1, vertexCount, dstCount, nodes);		
			}
			saveInstance(instId);
			return amplFile;	
		}
		
	    private static boolean hasCrossing(boolean[][] z) {
			for (int i = 0; i < z.length; i++) {
				for (int j = 0; j < z[i].length; j++) {
					if (z[i][j]) {
						for (int k = i + 1; k <z.length; k++) {
							for (int l = j + 1; l < z[k].length; l++) {
								if (z[k][l]) {
									if (edgesProperlyIntersect(nodes[i].getPoint(), nodes[j].getPoint(), nodes[k].getPoint(), nodes[l].getPoint())) {
										return true;
									}
								}
							}
						}
					}
				}
			}
			return false;
		}

		private static boolean edgesProperlyIntersect(Point p1, Point p2, Point p3, Point p4) {
	        //determine if the lines intersect
	        boolean intersects = Line2D.linesIntersect(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, p4.x, p4.y);
	        //determines if the lines share an endpoint
	        boolean shareAnyPoint = shareAnyPoint(p1, p2, p3, p4);
	        if (intersects && !shareAnyPoint) {
	            return true;
	        } 
	        return false;
		}

		private static boolean shareAnyPoint(Point p1, Point p2, Point p3, Point p4) {
			   if (isPointOnTheLine(p1, p2, p3)) return true;
			    else if (isPointOnTheLine(p1, p2, p4)) return true;
			    else if (isPointOnTheLine(p3, p4, p1)) return true;
			    else if (isPointOnTheLine(p3, p4, p2)) return true;
			    else return false;

		}

		private static boolean isPointOnTheLine(Point p1, Point p2, Point p) {

			    //handle special case where the line is vertical
			    if (p2.x == p1.x) {
			        if(p1.x == p.x) return true;
			        else return false;
			    }
			    double m = (p2.y - p1.y) / (p2.x - p1.x);


			    if ((p.y - p1.y) == m * (p.x - p1.x)) return true;
			    else return false;

		}

		private static void saveInstance(int instId) {
			File instFile = new File("instances/instance" +  new File("instances/").list().length + ".txt");
	        try
	        {
	            System.out.println("Saving: instance");
	            FileWriter fw = new FileWriter(instFile,true); //the true will append the new data
	            fw.write("\n");
	            fw.write(Constants.INST_ID + instId + "\n");
	            fw.write(Constants.DELIMITER + "\n");//appends the string to the file
	            fw.write(vertexCount + "\n");//appends the string to the file
	            fw.write(dstCount + "\n");//appends the string to the file
	            for (Node v: nodes) {
	                fw.write(v.getPoint().getX() + " " + v.getPoint().getY());
	                fw.write("\n");
	            }
	            fw.close();
	        }
	        catch(IOException ioe)
	        {
	            System.err.println("IOException: " + ioe.getMessage());
	        }   			
			
		}

		private static void draw(boolean[][] z, int instId, boolean useArrows) {
			Visualizer vis = new Visualizer(z, nodes, dstCount, useArrows);
			//Visualizer vis = new Visualizer(new File("instance.txt"), z, null);			
	        JFrame frame = new JFrame("ID: " + instId);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.add(vis);
	        frame.setSize(Visualizer.WINDOW_SIZE, Visualizer.WINDOW_SIZE);
	        frame.setLocationRelativeTo(null);
	        frame.setVisible(true);
	    }		
	    
   
	
	    private static void generatePoints(int vcnt) {
	    	vertexCount = vcnt;
	    	dstCount = vcnt;
	    	nodes = new Node[vertexCount];
	    	for (int i = 0; i < vertexCount; i++) {
	    		nodes[i] = new Node(i);
	    	}
	    	orderNeighbours();
	    }
	    
	    private static void orderNeighbours() {
	    	for (Node n: nodes) {
	    		n.orderNeighbours(nodes);
	    	}
	    	writeDebug();
	    	
	    }
	    
	    private static void writeDebug() {
			for (int i = 0; i < nodes.length; i++) {
				System.out.print(i + ": (");
				for (Node nb : nodes[i].orderedNeighbours) {
					System.out.print(" " + nb.id);
				}
				System.out.println();
			}
			
		}

		private static void createPoints() {
	    	try {
				BufferedReader br = new BufferedReader(new FileReader("instances/instance3.txt"));
				String line;
				int cnt = 0;
				try {
					while ((line = br.readLine()) != null) {
						if (line.equals(Constants.DELIMITER)) {
							vertexCount = Integer.parseInt(br.readLine());
							dstCount = Integer.parseInt(br.readLine());
							nodes = new Node[vertexCount];
							String[] twoParts;
							for (int i = 0; i < vertexCount; i++) {
								twoParts = br.readLine().split(" ");	 																	
		 						nodes[cnt] = new Node(i, new Point(Math.round(Float.parseFloat(twoParts[0])), Math.round(Float.parseFloat(twoParts[1]))), true);
		 						cnt++; // WHY?
							}
						}
					}	
					orderNeighbours();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }	    

	      
	    
	}

