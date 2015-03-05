package org.powertac.replayer.pusher;

import org.powertac.visualizer.push.WholesaleMarketPusher;

/**
 * Contains all wholesale data for a pusher.
 * 
 * @author DWietoska
 */
public class WholesaleMarketPusherReplayer extends WholesaleMarketPusher {

	/**
	 * Broker index in Highchart.
	 */
	private int index;

	/**
	 * Creates a new push object.
	 */
	public WholesaleMarketPusherReplayer(String name, long millis,
			double profitDelta, double energyDelta, double profit, 
			double energy, int index) {
		
		super(name, millis, profitDelta, energyDelta, profit, energy);
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
