package model;

public class STPair implements Comparable<STPair> {
	private int s;
	private int t;
	private double diff;
	private boolean added;
	
	public STPair(int s, int t, double diff) {
		this.s = s;
		this.t = t;
		this.diff = diff;
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

	@Override
	public int compareTo(STPair pair) {
		if (pair.diff == this.diff) {
			return 0;
		}
		return pair.diff > this.diff ? 1 : -1;
	}
	
	public String toString() {
		return "" + s + "-" + t + ": " + diff; 
	}

}
