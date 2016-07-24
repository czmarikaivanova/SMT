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
	    public static final String INST_ID = "# instance_ID: ";
	    public static String DELIMITER = "---------";	    
	    static Node[] nodes;
	    static int vertexCount;
	    static int dstCount;
	    static int nodeCount = 8;
		/**
		 * @param args
		 * @throws IloException 
		 */
		public static void main(String[] args) throws IloException {		

			int iter = 1;

			boolean generate = true;
			boolean draw = true;
			for (int i = 0; i < iter; i++) {
				runModel(new SMTModel(input), generate, draw)

			}

			
		}
			
		
		private static void runModel(ILPModel model, boolean generate, boolean draw) {
			Random rnd = new Random();
			int instId;
			ArrayList<Integer> crossList = new ArrayList<Integer>();
			File amplFile;
			if (generate) {
				generatePoints(nodeCount);	
				instId = rnd.nextInt(100000);		
				amplFile = generateAMPLData(instId);
			}
			else {
				createPoints();
				instId = -1;
				amplFile = generateAMPLData(-1);
				try {

					amplFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
			saveInstance(instId);
			model = new SMTModel(amplFile);
			model.populate();	
			model.createModel();
			model.solve();
			boolean[][] z = model.getZVar();
			if (hasCrossing(z)) {
				crossList.add(instId);
			}
			if (draw) {
				draw(z, instId);						
			}			
			System.err.println("Instances with crossing: ");
			for (Integer c: crossList) {
				System.err.println(c + "");	
			}			
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
	            fw.write(Main.INST_ID + instId + "\n");
	            fw.write(Main.DELIMITER + "\n");//appends the string to the file
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

		private static void draw(boolean[][] z, int instId) {
			Visualizer vis = new Visualizer(z, nodes, dstCount);
			//Visualizer vis = new Visualizer(new File("instance.txt"), z, null);			
	        JFrame frame = new JFrame("ID: " + instId);
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.add(vis);
	        frame.setSize(500, 500);
	        frame.setLocationRelativeTo(null);
	        frame.setVisible(true);
	    }		
	    
	    private static File generateAMPLData( int instanceID) {
	        try
	        {
	        	File datafile = new File("amplfiles/ampl " +  new File("amplfiles/").list().length + ".dat");
	            System.out.println("Saving: AMPL input");
	            FileWriter fw = new FileWriter(datafile,false); //the true will append the new data
	            fw.write(INST_ID + instanceID + "\n");
	            String dstStr = "set DESTS :=";
	            String nonDstStr = "set NONDESTS :=";
	            for (int i = 0; i < vertexCount; i++) {
	                if (i < dstCount) {
	                    dstStr += " " + i ;
	                } else {
	                    nonDstStr += " " + i;
	                }
	            }
	            dstStr += " ;\n";
	            nonDstStr += " ;\n";
	            
	            String paramStr = "param requir :=\n";
	            String distancesStr = "";
	            for (int i = 0; i < vertexCount; i++) {
	                for (int j = 0; j < vertexCount; j++) {
	                    distancesStr += " " +i + " " + j + " " + dst(nodes[i].getPoint(), nodes[j].getPoint()) + "\t"; 
	                }
	                distancesStr += "\n";
	            }
	            fw.write(dstStr);
	            fw.write(nonDstStr);
	            fw.write(paramStr);
	            fw.write(distancesStr + ";");
	            fw.close();
	            return datafile;
	        }
	        catch(IOException ioe)
	        {
	            System.err.println("IOException: " + ioe.getMessage());
	            return null;
	        } 
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
				BufferedReader br = new BufferedReader(new FileReader("instances/crossing.txt"));
				String line;
				int cnt = 0;
				try {
					while ((line = br.readLine()) != null) {
						if (line.equals(DELIMITER)) {
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
	    
	    public static float dst(Point v1, Point v2) {
	    	return (float) (Math.pow(v1.getX()-v2.getX(),2) + Math.pow(v1.getY()-v2.getY(),2));
	    }
	      
	    
	}

