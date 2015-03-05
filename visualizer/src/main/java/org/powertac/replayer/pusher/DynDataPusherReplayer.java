package org.powertac.replayer.pusher;

import org.powertac.visualizer.push.DynDataPusher;

/**
 * Contains all dynamic data for a pusher.
 * 
 * @author DWietoska
 */
public class DynDataPusherReplayer extends DynDataPusher {

	/**
	 * Broker index in Highchart.
	 */
	private int index;
	
	/**
	 * Creates a new push object.
	 */
	public DynDataPusherReplayer(String name, long millis, double profit,
			double energy, double profitDelta, double energyDelta, int index) {
		
		super(name, millis, profit, energy, profitDelta, energyDelta);
		// TODO Auto-generated constructor stub
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
