package org.powertac.visualizer.statistical;

/**
 * Data for one timeslot from broker's perspective.
 * 
 * @author Jurica Babic
 * 
 */
public class DynamicData {

	private double energy;
	private double profit;
	private double energyDelta;
	private double profitDelta;
	private int tsIndex;

	public DynamicData(DynamicData data) {
		energy = data.getEnergy();
		profit = data.getProfit();
		energyDelta = data.getEnergyDelta();
		profitDelta = data.getProfitDelta();
		tsIndex = data.getTsIndex();
	}
	
	public DynamicData(int tsIndex, double energy, double profit) {
		this.energy = energy;
		this.profit = profit;
		this.tsIndex = tsIndex;
	}

	public DynamicData(double energy, double profit, double energyDelta,
			double profitDelta, int tsIndex) {
		super();
		this.energy = energy;
		this.profit = profit;
		this.energyDelta = energyDelta;
		this.profitDelta = profitDelta;
		this.tsIndex = tsIndex;
	}

	public void update(double energy, double cash) {
		energyDelta += energy;
		profitDelta += cash;
	}

	public double getEnergy() {
		return energy;
	}
	
	public double getEnergyDelta() {
		return energyDelta;
	}

	public double getProfit() {
		return profit;
	}

	public double getProfitDelta() {
		return profitDelta;
	}

	public int getTsIndex() {
		return tsIndex;
	}
	
	public void setEnergy(double energy) {
		this.energy = energy;
	}
	
	public void setProfit(double profit) {
		this.profit = profit;
	}

	public void setEnergyDelta(double energyDelta) {
		this.energyDelta = energyDelta;
	}


	public void setProfitDelta(double profitDelta) {
		this.profitDelta = profitDelta;
	}

	@Override
	public String toString() {
		return "DynamicData [energy=" + energy + ", profit=" + profit
				+ ", energyDelta=" + energyDelta + ", profitDelta="
				+ profitDelta + ", tsIndex=" + tsIndex + "]";
	}
}
