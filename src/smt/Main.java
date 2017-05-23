package smt;
import ilog.concert.IloException;
	
	public class Main {

		/**
		 * @param args
		 * @throws IloException 
		 */
		public static void main(String[] args) throws IloException {		
			App app = new App( null);
//			App app = new App( "saved_inst/smt-x2-stronger-f1-vi.txt");
			app.run();
		}
				    
	}