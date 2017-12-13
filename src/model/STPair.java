package model;

import java.util.Comparator;

public class STPair {
	private int s;
	private int t;
	private double dst;
	private double diff;
	private boolean added;
	
//	public STPair(int s, int t, double diff) {
//		this.s = s;
//		this.t = t;
//		this.diff = diff;
//		this.added = false;
//	}
//	
	public STPair(int s, int t, double diff, double dst) {
		this.s = s;
		this.t = t;
		this.diff = diff;
		this.dst = dst;
		this.added = false;
	}

	public void setAdded(boolean added) {
		this.added = added;
	}
	
	public boolean getAdded() {
		return added;
	}
	
	public void setDiff(double diff) {
		this.diff = diff;
	}
	
	public int getS() {
		return s;
	}

	public int getT() {
		return t;
	}

	public double getDiff() {
		return diff;
	}
	
	/**
	 * compare two ST pairs according to their violation of max flow
	 * @return
	 */
	public static Comparator<STPair> getFlowViolationComparator() {
		return new Comparator<STPair>() {

			@Override
			public int compare(STPair o1, STPair o2) {
				if (o1.diff == o2.diff) {
					return 0;
				}
				return o1.diff < o2.diff ? 1 : -1;
			}
			
			@Override
			public String toString() {
				return "Flow violation";
			}
		};
	}
	
	/**
	 * Compare two ST pairs according to the distance between S and T
	 * @return
	 */
	public static Comparator<STPair> getDistanceComparator() {
		return new Comparator<STPair>() {

			@Override
			public int compare(STPair o1, STPair o2) {
				if (o1.dst == o2.dst) {
					return 0;
				}
				return o1.dst < o2.dst ? 1 : -1;
			}

			@Override
			public String toString() {
				return "Distance";
			}

		};
	}	
	
	public String toString() {
		return "" + s + "-" + t + ": " + diff + "(" + dst + ")"; 
	}

}
