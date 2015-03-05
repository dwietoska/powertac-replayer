package org.powertac.replayer.data.dto;

import org.powertac.common.ClearedTrade;

/**
 * Contains data that are required for the wholesale average view in the 
 * visualizer. Used by DAO to store this data.
 * 
 * @author DWietoska
 */
public class ClearedTradeObject {

	/**
	 * Time slot in millis.
	 */
	private long millis;
	
	/**
	 * ClearedTrade object.
	 */
	private ClearedTrade clearedTrade;

	/**
	 * Creates new ClearedTradeObject.
	 */
	public ClearedTradeObject() {
		super();
	}
	
	/**
	 * Creates new ClearedTradeObject.
	 * 
	 * @param millis Millis.
	 * @param clearedTrade ClearedTrade object.
	 */
	public ClearedTradeObject(long millis, ClearedTrade clearedTrade) {
		super();
		this.millis = millis;
		this.clearedTrade = clearedTrade;
	}

	/** Getter and Setter and toString. */
	public long getMillis() {
		return millis;
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}

	public ClearedTrade getClearedTrade() {
		return clearedTrade;
	}

	public void setClearedTrade(ClearedTrade clearedTrade) {
		this.clearedTrade = clearedTrade;
	}

	@Override
	public String toString() {
		return "ClearedTradeObject [millis=" + millis + ", clearedTrade="
				+ clearedTrade + "]";
	}
}
