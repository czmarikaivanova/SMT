package smt;
import ilog.concert.IloException;
	
	public class Main {

		/**
		 * @param args
		 * @throws IloException 
		 */
		public static void main(String[] args) throws IloException {
			int n = 21;
			int d = 14;
			int iter = 500;
			if (args.length > 0) {
				n = Integer.parseInt(args[0]);
				d = Integer.parseInt(args[1]);
				iter = Integer.parseInt(args[2]);
			}
			new App(n, d, iter).run();
		}
				    
	}
