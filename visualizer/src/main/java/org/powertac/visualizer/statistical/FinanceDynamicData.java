package org.powertac.visualizer.statistical;

/**
 * Tracks the finance data for one timeslot of a single broker. 
 * @author Jurica Babic
 *
 */
public class FinanceDynamicData {
	private double startingProfit;
	private double profitDelta;
	private double profit;
	private int tsIndex;
	
	public FinanceDynamicData(double startingBalance, int tsIndex) {
		this.startingProfit = startingBalance;
		this.tsIndex = tsIndex;		
	}
	
	// Eigefuegt
	public FinanceDynamicData(double startingProfit, double profitDelta,
			double profit, int tsIndex) {
		super();
		this.startingProfit = startingProfit;
		this.profitDelta = profitDelta;
		this.profit = profit;
		this.tsIndex = tsIndex;
	}

	
	public void updateProfit(double balance){
		profitDelta = balance - startingProfit;
		this.profit = balance;
	}
	
	public double getProfit() {
		return profit;
	}
	/**
	 * @return profit for one timeslot
	 */
	public double getProfitDelta() {
		return profitDelta;
	}
	public int getTsIndex() {
		return tsIndex;
	}
	
	// Eigefuegt
	public double getStartingProfit() {
		return startingProfit;
	}

	// Eigefuegt
	@Override
	public String toString() {
		return "FinanceDynamicData [startingProfit=" + startingProfit
				+ ", profitDelta=" + profitDelta + ", profit=" + profit
				+ ", tsIndex=" + tsIndex + "]";
	}
}
