package org.powertac.replayer.data.dto;

/**
 * Contains data that are required for the balancing view in the visualizer. 
 * Used by DAO to store this data.
 * 
 * @author DWietoska
 */
public class BalancingCategoryAttributes {
	
	/**
	 * Balancing energy.
	 */
	private double energy;
	
	/**
	 * Balancing profit.
	 */
	private double profit;

	/**
	 * Creates new BalancingCategoryAttributes.
	 */
	public BalancingCategoryAttributes() {
		super();
	}
	
	/**
	 * Creates new BalancingCategoryAttributes.
	 * 
	 * @param energy Energy
	 * @param profit Profit
	 */
	public BalancingCategoryAttributes(double energy, double profit) {
		super();
		this.energy = energy;
		this.profit = profit;
	}

	/**
	 * Creates new BalancingCategoryAttributes.
	 * 
	 * @param balancingCategoryAttributes BalancingCategoryAttribute.
	 */
	public BalancingCategoryAttributes(BalancingCategoryAttributes 
			balancingCategoryAttribute) {
		super();
		this.energy = balancingCategoryAttribute.getEnergy();
		this.profit = balancingCategoryAttribute.getProfit();
	}
	
	/** Getter and Setter and toString. */
	public double getEnergy() {
		return energy;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public double getProfit() {
		return profit;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	@Override
	public String toString() {
		return "BalancingCategoryAttributes [energy=" + energy + ", profit="
				+ profit + "]";
	}
}
