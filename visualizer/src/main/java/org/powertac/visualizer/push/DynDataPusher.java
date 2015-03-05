package org.powertac.visualizer.push;


public class DynDataPusher {

	private String name;
	private long millis;
	private double  profit;
	private double  energy;
	private double  profitDelta;
	private double  energyDelta;

	public DynDataPusher(String name, long millis, double  profit, double  energy, double  profitDelta, double  energyDelta) {
		this.name = name;
		this.millis = millis;
		this.profit = profit;
		this.energy = energy;
		this.profitDelta = profitDelta;
		this.energyDelta = energyDelta;
	}

	public String getName() {
		return name;
	}

	public long getMillis() {
		return millis;
	}

	public double getProfit() {
		return profit;
	}

	public double getEnergy() {
		return energy;
	}

	public double getProfitDelta() {
		return profitDelta;
	}

	public double getEnergyDelta() {
		return energyDelta;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}

	public void setProfit(double profit) {
		this.profit = profit;
	}

	public void setEnergy(double energy) {
		this.energy = energy;
	}

	public void setProfitDelta(double profitDelta) {
		this.profitDelta = profitDelta;
	}

	public void setEnergyDelta(double energyDelta) {
		this.energyDelta = energyDelta;
	}

	@Override
	public String toString() {
		return "DynDataPusher [name=" + name + ", millis=" + millis
				+ ", profit=" + profit + ", energy=" + energy
				+ ", profitDelta=" + profitDelta + ", energyDelta="
				+ energyDelta + "]";
	}
}
