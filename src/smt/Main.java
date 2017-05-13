package smt;
import ilog.concert.IloException;
	
	public class Main {

		/**
		 * @param args
		 * @throws IloException 
		 */
		public static void main(String[] args) throws IloException {		
		    int vertexCount = 14;
		    int dstCount = 7;
		    boolean draw = false;
			int iter = 10;  
			App app = new App(vertexCount, dstCount, draw, null, iter);
//			App app = new App(vertexCount, dstCount, draw, "saved_inst/basic_pf2_diff.txt", iter);
			app.run();
		}
				    
	}