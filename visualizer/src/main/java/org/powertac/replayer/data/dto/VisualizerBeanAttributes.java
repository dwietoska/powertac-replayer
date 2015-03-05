package org.powertac.replayer.data.dto;

import org.powertac.common.Competition;
import org.powertac.common.msg.TimeslotComplete;
import org.powertac.common.msg.TimeslotUpdate;

/**
 * Contains data that are required for the visualizer. 
 * Used by DAO to store this data.
 * 
 * @author DWietoska
 */
public class VisualizerBeanAttributes {

	/**
	 * Competition object.
	 */
	private Competition competition;
	
	/**
	 * Previous time slot update object.
	 */
	private TimeslotUpdate oldTimeslotUpdate;
	
	/**
	 * Time slot update object.
	 */
	private TimeslotUpdate timeslotUpdate;
	
	/**
	 * Time slot number.
	 */
	private int currentTimeslotSerialNumber;
	
	/**
	 * Time slot number in millis.
	 */
	private long currentMillis;
	
	/**
	 * TimeslotComplete object.
	 */
	private TimeslotComplete timeslotComplete;
	
	/**
	 * Creates new VisualizerBeanAttributes.
	 */
	public VisualizerBeanAttributes() {
		super();
	}
	
	/**
	 * Creates new VisualizerBeanAttributes.
	 */
	public VisualizerBeanAttributes(VisualizerBeanAttributes 
			visualizerBeanAttribute) {
		
		super();
		this.competition = visualizerBeanAttribute
				.getCompetition();
		this.oldTimeslotUpdate = visualizerBeanAttribute
				.getOldTimeslotUpdate();
		this.timeslotUpdate = visualizerBeanAttribute
				.getTimeslotUpdate();
		this.currentTimeslotSerialNumber = visualizerBeanAttribute
				.getCurrentTimeslotSerialNumber();
		this.currentMillis = visualizerBeanAttribute
				.getCurrentMillis();
		this.timeslotComplete = visualizerBeanAttribute
				.getTimeslotComplete();
	}
	
	/**
	 * Creates new VisualizerBeanAttributes.
	 */
	public VisualizerBeanAttributes(Competition competition) {
		
		super();
		this.competition = competition;
	}
	
	/**
	 * Creates new VisualizerBeanAttributes.
	 */
	public VisualizerBeanAttributes(Competition competition,
			TimeslotUpdate oldTimeslotUpdate, TimeslotUpdate timeslotUpdate,
			int currentTimeslotSerialNumber, long currentMillis,
			TimeslotComplete timeslotComplete) {
		
		super();
		this.competition = competition;
		this.oldTimeslotUpdate = oldTimeslotUpdate;
		this.timeslotUpdate = timeslotUpdate;
		this.currentTimeslotSerialNumber = currentTimeslotSerialNumber;
		this.currentMillis = currentMillis;
		this.timeslotComplete = timeslotComplete;
	}

	/** Getter and Setter and toString. */
	public Competition getCompetition() {
		return competition;
	}

	public void setCompetition(Competition competition) {
		this.competition = competition;
	}

	public TimeslotUpdate getOldTimeslotUpdate() {
		return oldTimeslotUpdate;
	}

	public void setOldTimeslotUpdate(TimeslotUpdate oldTimeslotUpdate) {
		this.oldTimeslotUpdate = oldTimeslotUpdate;
	}

	public TimeslotUpdate getTimeslotUpdate() {
		return timeslotUpdate;
	}

	public void setTimeslotUpdate(TimeslotUpdate timeslotUpdate) {
		this.timeslotUpdate = timeslotUpdate;
	}

	public int getCurrentTimeslotSerialNumber() {
		return currentTimeslotSerialNumber;
	}

	public void setCurrentTimeslotSerialNumber(int 
			currentTimeslotSerialNumber) {
		this.currentTimeslotSerialNumber = currentTimeslotSerialNumber;
	}

	public long getCurrentMillis() {
		return currentMillis;
	}

	public void setCurrentMillis(long currentMillis) {
		this.currentMillis = currentMillis;
	}

	public TimeslotComplete getTimeslotComplete() {
		return timeslotComplete;
	}

	public void setTimeslotComplete(TimeslotComplete timeslotComplete) {
		this.timeslotComplete = timeslotComplete;
	}

	@Override
	public String toString() {
		return "VisualizerBeanAttributes [competition=" + competition
				+ ", oldTimeslotUpdate=" + oldTimeslotUpdate
				+ ", timeslotUpdate=" + timeslotUpdate
				+ ", currentTimeslotSerialNumber="
				+ currentTimeslotSerialNumber + ", currentMillis="
				+ currentMillis + ", timeslotComplete=" + timeslotComplete
				+ "]";
	}
}
