package org.powertac.replayer.pusher;

/**
 * Contains all customer model data for pusher.
 * 
 * @author DWietoska
 */
public class CustomerModelPusher {

	/**
	 * Millis.
	 */
	private long millis;
	
	/**
	 * Charge.
	 */
	private double charge;
	
	/**
	 * KWh.
	 */
	private double kWh;
	
	/**
	 * Creates a new push object.
	 * 
	 * @param millis Millis
	 * @param charge Charge
	 * @param kWh KWh
	 */
	public CustomerModelPusher(long millis, double charge, 
			double kWh) {
		super();
		this.millis = millis;
		this.charge = charge;
		this.kWh = kWh;
	}
	
	/** Getter and Setter */
	public long getMillis() {
		return millis;
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}

	public double getCharge() {
		return charge;
	}

	public void setCharge(double charge) {
		this.charge = charge;
	}

	public double getkWh() {
		return kWh;
	}

	public void setkWh(double kWh) {
		this.kWh = kWh;
	}
}
