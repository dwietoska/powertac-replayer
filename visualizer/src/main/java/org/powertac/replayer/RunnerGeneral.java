package org.powertac.replayer;

import java.io.File;

import org.powertac.replayer.logtool.ErrorReadDomainObject;
import org.powertac.replayer.utils.Helper;

/**
 * This class defines methods to repeat games.
 * 
 * @author DWietoska
 */
public abstract class RunnerGeneral implements Runner {

	/**
	 * This method read init data and starts the timeslot thread.
	 * 
	 * @param file The selected file
	 * @param clockRate Clock rate
	 * @throws ErrorReadDomainObject Error
	 */
	@Override
	public abstract void runInit(File logFile, double clockRate) 
			throws ErrorReadDomainObject;
	
	/**
	 * Thread which sends messages in every timeslot.
	 */
	protected TimeslotRunnerGeneral logTimeslotRunner;
	
	/**
	 * Restart a selected game.
	 * 
	 * @param file The selected file
	 * @param clockRate Clock rate
	 */
	@Override
	public abstract String run(File file, double clockRate) 
			throws ErrorReadDomainObject;

	/**
	 * A new timeslot was selected. Sets the new data and sends push messages 
	 * to Highchart. Only for extended mode.
	 * 
	 * @param oldTimeslot The previous timeslot
	 * @param timeslot The new timeslot
	 */
	@Override
	public abstract void updateView(int oldTimeslot, int timeslot);
	
	/**
	 * Creates the TimeslotRunnerGeneral thread.
	 * 
	 * @param tickInterval The clock rate
	 * @param timeslot The current timeslot
	 * @param isReloadPage True if the page was reloaded
	 */
	@Override
	public abstract void createRunner(long tickInterval, int timeslot,
			boolean isReloadPage);
	
	/**
	 * Stops the TimeslotRunnerGeneral thread.
	 */
	@Override
    public void stopRun() {
    	
    	if (this.logTimeslotRunner != null) {
    	
    		this.logTimeslotRunner.cancelTimer();
    	}
    }
	
	/**
	 * Sets a new clock rate.
	 * 
	 * @param clockRate The clock rate
	 */
	@Override
	public void clockRateReplayer(double clockRate) {
		
		if (this.logTimeslotRunner != null) {
		
			this.logTimeslotRunner.setTickInterval((long) (clockRate * 1000));
		}
	}
	
	/**
	 * Pause the TimeslotRunnerGeneral thread.
	 */
	@Override
	public void pauseReplayer() {

		this.stopRun();
	}
	
	/**
	 * Continue the TimeslotRunnerGeneral thread.
	 * 
	 * @param clockRate The new clockRate
	 * @param currentTimeslot The new timeslot
	 */
	@Override
	public void continueReplayer(double clockRate, int currentTimeslot) {

		createRunner((long) (clockRate * Helper.MILLIS), currentTimeslot + 1,
				false);
	}
}
