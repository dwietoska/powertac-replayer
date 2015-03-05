package org.powertac.replayer.data.dto;

/**
 * Contains data that are required for the tariff view in the visualizer. 
 * Used by DAO to store this data.
 * 
 * @author DWietoska
 */
public class TariffCategoryAttributes {

	  /** 
	   * Total sold energy in tariff market 
	   */
	  private double totalSoldEnergy;
	  
	  /** 
	   * Total bought energy in tariff market 
	   */
	  private double totalBoughtEnergy;
	  
	  /**
	   * Total amount of money used in tariff market = sum of 
	   * absolute charge from TariffTransactions.
	   */
	  private double totalMoneyFlow;
	  
	  /** 
	   * Number of customers who are/were broker clients 
	   */
	  private int gainedCustomers;
	  
	  /** 
	   * Number of customers who left broker 
	   */
	  private int lostCustomers;
	  
	  /** 
	   * Number of times consumers used energy 
	   */
	  private long consumptionConsumers;
	  
	  /** 
	   * Amount of money received from selling energy in tariff market 
	   */
	  private double totalMoneyFromSold;
	  
	  /**
	   * Number of customers.
	   */
	  private int customerCount;

	/**
	 * Creates new TariffCategoryAttributes.
	 */
	  public TariffCategoryAttributes() {
		  
	  }

	/**
	 * Creates new TariffCategoryAttributes.
	 */
	public TariffCategoryAttributes(
			TariffCategoryAttributes tariffCategoryAttribute) {
		
		super();
		
		this.totalSoldEnergy = tariffCategoryAttribute
				.getTotalSoldEnergy();
		this.totalBoughtEnergy = tariffCategoryAttribute
				.getTotalBoughtEnergy();
		this.totalMoneyFlow = tariffCategoryAttribute
				.getTotalMoneyFlow();
		this.gainedCustomers = tariffCategoryAttribute
				.getGainedCustomers();
		this.lostCustomers = tariffCategoryAttribute
				.getLostCustomers();
		this.consumptionConsumers = tariffCategoryAttribute
				.getConsumptionConsumers();
		this.totalMoneyFromSold = tariffCategoryAttribute
				.getTotalMoneyFromSold();
		this.customerCount = tariffCategoryAttribute
				.getCustomerCount();
	}

	/**
	 * Creates new TariffCategoryAttributes.
	 */
	public TariffCategoryAttributes(double totalSoldEnergy,
			double totalBoughtEnergy, double totalMoneyFlow,
			int gainedCustomers, int lostCustomers, long consumptionConsumers,
			double totalMoneyFromSold, int customerCount) {
		
		super();
		this.totalSoldEnergy = totalSoldEnergy;
		this.totalBoughtEnergy = totalBoughtEnergy;
		this.totalMoneyFlow = totalMoneyFlow;
		this.gainedCustomers = gainedCustomers;
		this.lostCustomers = lostCustomers;
		this.consumptionConsumers = consumptionConsumers;
		this.totalMoneyFromSold = totalMoneyFromSold;
		this.customerCount = customerCount;
	}

	/** Getter and Setter and toString. */
	public double getTotalSoldEnergy() {
		return totalSoldEnergy;
	}

	public void setTotalSoldEnergy(double totalSoldEnergy) {
		this.totalSoldEnergy = totalSoldEnergy;
	}

	public double getTotalBoughtEnergy() {
		return totalBoughtEnergy;
	}

	public void setTotalBoughtEnergy(double totalBoughtEnergy) {
		this.totalBoughtEnergy = totalBoughtEnergy;
	}

	public double getTotalMoneyFlow() {
		return totalMoneyFlow;
	}

	public void setTotalMoneyFlow(double totalMoneyFlow) {
		this.totalMoneyFlow = totalMoneyFlow;
	}

	public int getGainedCustomers() {
		return gainedCustomers;
	}

	public void setGainedCustomers(int gainedCustomers) {
		this.gainedCustomers = gainedCustomers;
	}

	public int getLostCustomers() {
		return lostCustomers;
	}

	public void setLostCustomers(int lostCustomers) {
		this.lostCustomers = lostCustomers;
	}

	public long getConsumptionConsumers() {
		return consumptionConsumers;
	}

	public void setConsumptionConsumers(long consumptionConsumers) {
		this.consumptionConsumers = consumptionConsumers;
	}

	public double getTotalMoneyFromSold() {
		return totalMoneyFromSold;
	}

	public void setTotalMoneyFromSold(double totalMoneyFromSold) {
		this.totalMoneyFromSold = totalMoneyFromSold;
	}

	public int getCustomerCount() {
		return customerCount;
	}

	public void setCustomerCount(int customerCount) {
		this.customerCount = customerCount;
	}

	public void addCustomerCount(int customerCount) {
		 this.customerCount += customerCount;
	}
	
	public void addCustomers(int gainedCustomers) {
		this.gainedCustomers = +gainedCustomers;
	}

	public void addConsumptionConsumers(long consumptionConsumers) {
		this.consumptionConsumers += consumptionConsumers;
	}

	public void addSoldEnergy(double soldEnergy) {
		this.totalSoldEnergy += soldEnergy;
	}

	public void addMoneyFromSold(double moneyFromSold) {
		this.totalMoneyFromSold += moneyFromSold;
	}

	public void addBoughtEnergy(double boughtEnergy) {
		this.totalBoughtEnergy += boughtEnergy;
	}

	public void addCharge(double charge) {
		this.totalMoneyFlow += charge;
	}

	@Override
	public String toString() {
		return "TariffCategoryAttributes [totalSoldEnergy=" + totalSoldEnergy
				+ ", totalBoughtEnergy=" + totalBoughtEnergy
				+ ", totalMoneyFlow=" + totalMoneyFlow + ", gainedCustomers="
				+ gainedCustomers + ", lostCustomers=" + lostCustomers
				+ ", consumptionConsumers=" + consumptionConsumers
				+ ", totalMoneyFromSold=" + totalMoneyFromSold
				+ ", customerCount=" + customerCount + "]";
	}
}
