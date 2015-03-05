package org.powertac.replayer.data.dto;

/**
 * Contains data that are required for grading in the visualizer. 
 * Used by DAO to store this data.
 * 
 * @author DWietoska
 */
public class GradingAttributes {

	/**
	 * Tariff market attribute (totalSoldEnergyTariffMarket) for grading.
	 */
	private double totalSoldEnergyTariffMarket;
	
	/**
	 * Tariff market attribute (totalBoughtEnergyTariffMarket) for grading.
	 */
	private double totalBoughtEnergyTariffMarket;
	
	/**
	 * Tariff market attribute (totalMoneyFlowTariffMarket) for grading.
	 */
	private double totalMoneyFlowTariffMarket;
	
	/**
	 * Tariff market attribute (totalDistributionTariffMarket) for grading.
	 */
	private double totalDistributionTariffMarket;
	
	/**
	 * Wholesale market attribute (totalBoughtEnergyWholesaleMarket) 
	 * for grading.
	 */
	private double totalBoughtEnergyWholesaleMarket;
	
	/**
	 * Wholesale market attribute (totalSoldEnergyWholesaleMarket) 
	 * for grading.
	 */
	private double totalSoldEnergyWholesaleMarket;
	
	/**
	 * Wholesale market attribute (totalMoneyFromSellingWholesaleMarket) 
	 * for grading.
	 */
	private double totalMoneyFromSellingWholesaleMarket;
	
	/**
	 * Wholesale market attribute (totalMoneyFromBuyingWholesaleMarket) 
	 * for grading.
	 */
	private double totalMoneyFromBuyingWholesaleMarket;

	/**
	 * Creates new GradingAttributes.
	 */
	public GradingAttributes() {

	}

	/**
	 * Creates new GradingAttributes.
	 * 
	 * @param gradingAttribute GradingAttribute
	 */
	public GradingAttributes(GradingAttributes gradingAttribute) {
		
		super();
		this.totalSoldEnergyTariffMarket = gradingAttribute
				.getTotalSoldEnergyTariffMarket();
		this.totalBoughtEnergyTariffMarket = gradingAttribute
				.getTotalBoughtEnergyTariffMarket();
		this.totalMoneyFlowTariffMarket = gradingAttribute
				.getTotalMoneyFlow();
		this.totalDistributionTariffMarket = gradingAttribute
				.getTotalDistribution();
		this.totalBoughtEnergyWholesaleMarket = gradingAttribute
				.getTotalBoughtEnergyWholesaleMarket();
		this.totalSoldEnergyWholesaleMarket = gradingAttribute
				.getTotalSoldEnergyWholesaleMarket();
		this.totalMoneyFromSellingWholesaleMarket = gradingAttribute
				.getTotalMoneyFromSellingWholesaleMarket();
		this.totalMoneyFromBuyingWholesaleMarket = gradingAttribute
				.getTotalMoneyFromBuyingWholesaleMarket();
	}
	
	/**
	 * Creates new GradingAttributes.
	 */
	public GradingAttributes(double totalSoldEnergyTariffMarket,
			double totalBoughtEnergyTariffMarket,
			double totalMoneyFlowTariffMarket,
			double totalDistributionTariffMarket,
			double totalBoughtEnergyWholesaleMarket,
			double totalSoldEnergyWholesaleMarket,
			double totalMoneyFromSellingWholesaleMarket,
			double totalMoneyFromBuyingWholesaleMarket) {
		
		super();
		this.totalSoldEnergyTariffMarket = 
				totalSoldEnergyTariffMarket;
		this.totalBoughtEnergyTariffMarket = 
				totalBoughtEnergyTariffMarket;
		this.totalMoneyFlowTariffMarket = 
				totalMoneyFlowTariffMarket;
		this.totalDistributionTariffMarket = 
				totalDistributionTariffMarket;
		this.totalBoughtEnergyWholesaleMarket = 
				totalBoughtEnergyWholesaleMarket;
		this.totalSoldEnergyWholesaleMarket = 
				totalSoldEnergyWholesaleMarket;
		this.totalMoneyFromSellingWholesaleMarket = 
				totalMoneyFromSellingWholesaleMarket;
		this.totalMoneyFromBuyingWholesaleMarket = 
				totalMoneyFromBuyingWholesaleMarket;
	}

	/** Adder Methods. */
	public void addCharge(double charge) {
		this.totalMoneyFlowTariffMarket += charge;
	}
	
	public void addSoldEnergyTariffMarket(double 
			soldEnergyTariffMarket) {
		this.totalSoldEnergyTariffMarket += soldEnergyTariffMarket;
	}
	
	public void addBoughtEnergyTariffMarket(double boughtEnergyTariffMarket) {
		this.totalBoughtEnergyTariffMarket += boughtEnergyTariffMarket;
	}
	
	public void addEnergyDistribution(double energy) {
		this.totalDistributionTariffMarket += energy;
	}
	
	public void addBoughtEnergyWholesaleMarket(double energy) {
		this.totalBoughtEnergyWholesaleMarket += energy;
	}
	
	public void addSoldEnergyWholesaleMarket(double energy) {
		this.totalSoldEnergyWholesaleMarket += energy;
	}
	
	public void addMoneyFromSellingWholesaleMarket(double money) {
		this.totalMoneyFromSellingWholesaleMarket += money;
	}
	
	public void addMoneyFromBuyingWholesaleMarket(double money) {
		this.totalMoneyFromBuyingWholesaleMarket += money;
	}
	
	/** Getter and Setter and toString. */
	public double getTotalMoneyFlow() {
		return totalMoneyFlowTariffMarket;
	}

	public double getTotalSoldEnergyTariffMarket() {
		return totalSoldEnergyTariffMarket;
	}

	public double getTotalBoughtEnergyTariffMarket() {
		return totalBoughtEnergyTariffMarket;
	}

	public double getTotalDistribution() {
		return totalDistributionTariffMarket;
	}

	public double getTotalBoughtEnergyWholesaleMarket() {
		return totalBoughtEnergyWholesaleMarket;
	}

	public double getTotalSoldEnergyWholesaleMarket() {
		return totalSoldEnergyWholesaleMarket;
	}

	public double getTotalMoneyFromSellingWholesaleMarket() {
		return totalMoneyFromSellingWholesaleMarket;
	}

	public double getTotalMoneyFromBuyingWholesaleMarket() {
		return totalMoneyFromBuyingWholesaleMarket;
	}

	/**
	 * Ascertains tariffGrade.
	 *  
	 * @param moneyFlow moneyFlow
	 * @param consumptionConsumers consumptionConsumers
	 * @param moneySoldEnergy moneySoldEnergy
	 * @param boughtEnergy boughtEnergy
	 * @param soldEnergy soldEnergy
	 * @param customerCount customerCount
	 * @param lostCustomers lostCustomers
	 * @return tariffGrade
	 */
	public double getTariffGrade(double moneyFlow, long consumptionConsumers,
			double moneySoldEnergy, double boughtEnergy, double soldEnergy,
			int customerCount, int lostCustomers) {

		double gradeProfit = totalMoneyFlowTariffMarket != 0 ? moneyFlow
				/ totalMoneyFlowTariffMarket : 0;

		double gradeTariffSellShare = totalBoughtEnergyTariffMarket != 0 
				? boughtEnergy / totalBoughtEnergyTariffMarket
				: 0;
		double gradeTariffBuyShare = totalSoldEnergyTariffMarket != 0 
				? soldEnergy / totalSoldEnergyTariffMarket
				: 0;
		return 100 / 3 * gradeProfit + 100 / 3 * gradeTariffSellShare + 100 / 3
				* gradeTariffBuyShare;
	}

	/**
	 * Ascertains distributionGrade.
	 * 
	 * @param energy energy
	 * @return distributionGrade
	 */
	public double getDistributionGrade(double energy) {
		
		return totalDistributionTariffMarket != 0 ? energy
				/ totalDistributionTariffMarket * 100 : 0;
	}

	/**
	 * Ascertains wholesaleGrade.
	 * 
	 * @param totalRevenue totalRevenue
	 * @param totalCost totalCost
	 * @param energyBought energyBought
	 * @param energySold energySold
	 * @return wholesaleGrade
	 */
	public double getWholesaleGrade(double totalRevenue, double totalCost,
			double energyBought, double energySold) {
		
		double totalAggregateCost = totalBoughtEnergyWholesaleMarket != 0 
				? energyBought
						/ totalBoughtEnergyWholesaleMarket
						* totalMoneyFromBuyingWholesaleMarket
				: 0;
		double totalAggregateRevenue = totalSoldEnergyWholesaleMarket != 0 
				? energySold
						/ totalSoldEnergyWholesaleMarket
						* totalMoneyFromSellingWholesaleMarket
				: 0;
		double wholesaleGradeCost = 50;
		double percentageCost = totalAggregateCost != 0 ? Math.abs(totalCost)
				/ Math.abs(totalAggregateCost) : 0;
		if (percentageCost > 1) {
			wholesaleGradeCost -= (percentageCost - 1) * 100 / 2;
		} else if (percentageCost < 1) {
			wholesaleGradeCost += (1 - percentageCost) * 100 / 2;
		} else {
			// wholesaleGradeCost is 50 in this case
		}

		double wholesaleGradeRevenue = 50;
		double percentageRevenue = totalAggregateRevenue != 0 ? Math
				.abs(totalRevenue) / Math.abs(totalAggregateRevenue) : 0;
		if (percentageRevenue > 1) {
			wholesaleGradeRevenue += (percentageRevenue - 1) * 100 / 2;
		} else if (percentageRevenue < 1) {
			wholesaleGradeRevenue -= (1 - percentageRevenue) * 100 / 2;
		} else {
			// wholesaleGradeRevenue is 50 in this case
		}
		return (wholesaleGradeCost / 2 + wholesaleGradeRevenue / 2);
	}

	/**
	 * Ascertains balancingGrade.
	 * 
	 * @param balancedEnergy balancedEnergy
	 * @param energyDelivered energyDelivered
	 * @param cost cost
	 * @return balancingGrade
	 */
	public double getBalancingGrade(double balancedEnergy,
			double energyDelivered, double cost) {
		
		double imbalanceRatio = energyDelivered != 0 ? balancedEnergy
				/ energyDelivered : 0;
		double costPerkWh = balancedEnergy != 0 ? cost / balancedEnergy : 0;

		double gradeRatio = 0;
		if (Math.abs(imbalanceRatio) <= 1) {
			gradeRatio = (1 - Math.abs(imbalanceRatio)) * 100;
		}

		double gradeCostPerkWh = 50;
		if (costPerkWh < 0 && costPerkWh > -0.05) {
			gradeCostPerkWh += costPerkWh * 1000;
		} else if (costPerkWh <= -0.05) {
			gradeCostPerkWh = 0;
		} else if (costPerkWh > 0 && costPerkWh < 0.05) {
			gradeCostPerkWh += costPerkWh * 1000;
		} else if (costPerkWh >= 0.05) {
			gradeCostPerkWh = 50;
		}

		return gradeRatio / 2 + gradeCostPerkWh / 2;
	}

	/**
	 * Ascertains imbalanceRatio.
	 * 
	 * @param balancedEnergy balancedEnergy
	 * @param energyDelivered energyDelivered
	 * @return imbalanceRatio
	 */
	public double getImbalanceRatio(double balancedEnergy,
			double energyDelivered) {
		
		return energyDelivered != 0 ? balancedEnergy / energyDelivered : 0;
	}

	@Override
	public String toString() {
		return "GradingAttributes [totalSoldEnergyTariffMarket="
				+ totalSoldEnergyTariffMarket
				+ ", totalBoughtEnergyTariffMarket="
				+ totalBoughtEnergyTariffMarket
				+ ", totalMoneyFlowTariffMarket=" + totalMoneyFlowTariffMarket
				+ ", totalDistributionTariffMarket="
				+ totalDistributionTariffMarket
				+ ", totalBoughtEnergyWholesaleMarket="
				+ totalBoughtEnergyWholesaleMarket
				+ ", totalSoldEnergyWholesaleMarket="
				+ totalSoldEnergyWholesaleMarket
				+ ", totalMoneyFromSellingWholesaleMarket="
				+ totalMoneyFromSellingWholesaleMarket
				+ ", totalMoneyFromBuyingWholesaleMarket="
				+ totalMoneyFromBuyingWholesaleMarket + "]";
	}
}
