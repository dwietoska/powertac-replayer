package org.powertac.replayer.pusher;

import org.powertac.visualizer.push.TariffMarketPusher;

/**
 * Contains all tariff market data for a pusher.
 * 
 * @author DWietoska
 */
public class TariffMarketPusherReplayer extends TariffMarketPusher {

	/**
	 * Broker index in Highchart.
	 */
	private int index;
	
	/**
	 * Creates a new push object.
	 */
	public TariffMarketPusherReplayer(String name, long millis, double profit,
			double energy, int customerCount, double profitDelta,
			double energyDelta, int customerCountDelta, int index) {
		
		super(name, millis, profit, energy, customerCount, profitDelta, 
				energyDelta, customerCountDelta);
		this.index = index;
	}
	
	/** Getter and Setter */
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
