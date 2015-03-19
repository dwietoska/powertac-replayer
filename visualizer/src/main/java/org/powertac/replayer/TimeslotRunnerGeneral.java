package org.powertac.replayer;

import org.powertac.replayer.tsSimulation.SimulationClockControlReplayer;

/**
 * Contains general attributes and methods for the thread which send or set 
 * messages and data in one time slot.
 * 
 * @author DWietoska
 *
 */
public abstract class TimeslotRunnerGeneral {

	/**
	 * Specifies the time, in milliseconds, when the next message will 
	 * send (After reloading a page).
	 */
	protected static final long WAIT_FIRST_TICK_RELOAD_PAGE = 4000;
	
	/**
	 * Simulation Clock.
	 */
	protected SimulationClockControlReplayer clock;
	
	/**
	 * Specifies whether the time slot runner is running.
	 */
	protected boolean isRunning;
    
	/**
	 * True if a page was reloaded.
	 */
	protected boolean isReloadPage; 
    
	/**
	 * Safe time slot.
	 */
	protected int safeTimeslot; 
    
	/**
	 * The current time slot.
	 */
	protected int currentTimeslot;
    
	/**
	 * Specifies whether the time slot runner is cancelled
	 */
	protected volatile boolean cancel;
    
	/**
	 * The current clock rate.
	 */
	protected long tickInterval;
    
	/**
	 * Specifies whether the clock rate was changed
	 */
	protected boolean tickIntervalChanged;
    
	/**
	 * The previous time slot.
	 */
	protected int nextTick;
	
	/**
	 * Stops sending messages in a time slot. Called
     * when a time slot was changed.
	 */
    public void notifyTick() {
    	clock.cancelTimer();
    	clock.notifyTick();
    }
    
    /**
     * Stops the thread which sends messages. Called
     * from pause, stop button and from PhaseListener.
     */
    public synchronized void cancelTimer() {
    	cancel = true;
    	clock.cancelTimer();
    	clock.notifyTick();
    }
    
    /** Getter and setter methods **/
	public int getCurrentTimeslot() {
		return currentTimeslot;
	}

	public void setCurrentTimeslot(int currentTimeslot) {
		this.currentTimeslot = currentTimeslot;
	}

	public void setTickInterval(long clockRate) {
		this.tickInterval = clockRate;
		this.tickIntervalChanged = true;
	}

	public long getTickInterval() {
		return tickInterval;
	}

	public int getSafeTimeslot() {
		return safeTimeslot;
	}

	public void setSafeTimeslot(int safeTimeslot) {
		this.safeTimeslot = safeTimeslot;
	}

	public synchronized boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
}
