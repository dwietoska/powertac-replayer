package org.powertac.replayer.data.dto;

/**
 * Contains data that are required for the wholesale view in the visualizer. 
 * Used by DAO to store this data.
 * 
 * @author DWietoska
 */
public class WholesaleCategoryAttributes {

	/**
	 * Total amount of energy bought in wholesale market
	 */
	private double totalEnergyBought;

	/**
	 * Total amount of energy sold in wholesale market
	 */
	private double totalEnergySold;

	/**
	 * Total amount of money payed for energy in wholesale market
	 */
	private double totalCostFromBuying;

	/**
	 * Total amount of money received for selling energy in wholesale market
	 */
	private double totalRevenueFromSelling;

	/**
	 * Creates new WholesaleCategoryAttributes.
	 */
	public WholesaleCategoryAttributes() {
		super();
	}

	/**
	 * Creates new WholesaleCategoryAttributes.
	 */
	public WholesaleCategoryAttributes(
			WholesaleCategoryAttributes wholesaleCategoryAttribute) {
		
		super();
		this.totalEnergyBought = wholesaleCategoryAttribute
				.getTotalEnergyBought();
		this.totalEnergySold = wholesaleCategoryAttribute.getTotalEnergySold();
		this.totalCostFromBuying = wholesaleCategoryAttribute
				.getTotalCostFromBuying();
		this.totalRevenueFromSelling = wholesaleCategoryAttribute
				.getTotalRevenueFromSelling();
	}

	/**
	 * Creates new WholesaleCategoryAttributes.
	 */
	public WholesaleCategoryAttributes(double totalEnergyBought,
			double totalEnergySold, double totalCostFromBuying,
			double totalRevenueFromSelling) {
		
		super();
		this.totalEnergyBought = totalEnergyBought;
		this.totalEnergySold = totalEnergySold;
		this.totalCostFromBuying = totalCostFromBuying;
		this.totalRevenueFromSelling = totalRevenueFromSelling;
	}

	/** Getter and Setter and toString. */
	public double getTotalEnergyBought() {
		return totalEnergyBought;
	}

	public void addEnergyBought(double energy) {
		this.totalEnergyBought += energy;
	}

	public double getTotalEnergySold() {
		return totalEnergySold;
	}

	public void addEnergySold(double energy) {
		this.totalEnergySold += energy;
	}

	public double getTotalCostFromBuying() {
		return totalCostFromBuying;
	}

	public void addCostFromBuying(double cost) {
		this.totalCostFromBuying += cost;
	}

	public double getTotalRevenueFromSelling() {
		return totalRevenueFromSelling;
	}

	public void addRevenueFromSelling(double revenue) {
		this.totalRevenueFromSelling += revenue;
	}

	@Override
	public String toString() {
		return "WholesaleCategoryAttributes [totalEnergyBought="
				+ totalEnergyBought + ", totalEnergySold=" + totalEnergySold
				+ ", totalCostFromBuying=" + totalCostFromBuying
				+ ", totalRevenueFromSelling=" + totalRevenueFromSelling + "]";
	}
}
