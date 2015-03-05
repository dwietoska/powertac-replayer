package org.powertac.replayer.data.dto;

/**
 * Contains data that are required for the finance view in the visualizer. 
 * Used by DAO to store this data.
 * 
 * @author DWietoska
 */
public class FinanceCategoryAttributes {

	/**
	 * Profit.
	 */
	private double profit;
	
	/**
	 * Creates new FinanceCategoryAttributes.
	 */
	public FinanceCategoryAttributes() {
		
	}
	
	/**
	 * Creates new FinanceCategoryAttributes.
	 * 
	 * @param profit Profit
	 */
	public FinanceCategoryAttributes(double profit) {
		super();
		this.profit = profit;
	}

	/** Getter and Setter and toString. */
	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	@Override
	public String toString() {
		return "FinanceCategoryAttributes [profit=" + profit + "]";
	}
}
