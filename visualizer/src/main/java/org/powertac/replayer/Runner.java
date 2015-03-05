package org.powertac.replayer;

import java.io.File;

import org.powertac.replayer.logtool.ErrorReadDomainObject;

/**
 * Defines methods to repeat games. The visualizer can call every method.
 * 
 * @author DWietoska
 */
public interface Runner {
	public String run(File logFile, double clockRate) 
			throws ErrorReadDomainObject;
	public void updateView(int oldTimeslot, int timeslot);
	public void createRunner(long tickInterval, int timeslot, 
			boolean isReloadPage);
	public void stopRun();
	public void clockRateReplayer(double clockRate);
	public void pauseReplayer();
	public void continueReplayer(double clockRate, int currentTimeslot);
	public TimeslotRunnerGeneral getLogTimeslotRunner();
}
