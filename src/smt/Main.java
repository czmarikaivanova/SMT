package smt;
import ilog.concert.IloException;
	
	public class Main {

		/**
		 * @param args
		 * @throws IloException 
		 */
		public static void main(String[] args) throws IloException {
			int n = 14;
			int d = 10;
			int iter = 50;
			if (args.length > 0) {
				n = Integer.parseInt(args[0]);
				d = Integer.parseInt(args[1]);
				iter = Integer.parseInt(args[2]);
			}
			new App(n, d, iter).run();
		}
				    
	}
