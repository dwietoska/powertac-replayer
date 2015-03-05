package org.powertac.replayer.pusher;

import org.powertac.visualizer.push.FinancePusher;

/**
 * Contains all finance data for a pusher.
 * 
 * @author DWietoska
 */
public class FinancePusherReplayer extends FinancePusher {

	/**
	 * Broker index in Highchart.
	 */
	private int index;
	
	/**
	 * Creates a new push object.
	 */
	public FinancePusherReplayer(String name, long millis, double profit,
			double profitDelta, int index) {
		super(name, millis, profit, profitDelta);

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
