package org.powertac.replayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.powertac.common.msg.TimeslotUpdate;
import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.logtool.ErrorReadDomainObject;
import org.powertac.replayer.pusher.PushServiceReplayerNew;
import org.powertac.replayer.tsSimulation.SimulationClockControlReplayer;
import org.powertac.replayer.utils.Helper;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.VisualizerApplicationContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Implements the normal mode for replaying. Uses replaying data structure.
 * 
 * @author DWietoska
 */
@Service("RunnerNormal")
@Scope(value = "session") // prototype
public class RunnerNormal extends RunnerGeneral implements DisposableBean {

	/**
	 * Access to LogDAOImplNormal.
	 */
	@Autowired
	private LogDataImpl logDao;
	
	/**
	 * Access to LogParametersBean.
	 */
	@Autowired
	private LogParametersBean logParametersBean;
	
	/**
	 * Access to push service.
	 */
	@Autowired
	private PushServiceReplayerNew pushServiceReplayer;
	
	/**
	 * Creates a new normal runner.
	 */
    public RunnerNormal() {
    }
    
	/**
	 * Restart a selected game.
	 * 
	 * @param file The selected file
	 * @param clockRate Clock rate
	 * @throws ErrorReadDomainObject Error
	 */
    @Override
    public String run(File file, double clockRate) 
    		throws ErrorReadDomainObject {

		String message = null;
		
		try {

			// A new game was started. Reset all beans.
			List<GameInitialization> games = VisualizerApplicationContext
					   .listBeansOfType(GameInitialization.class);
			
			for (GameInitialization game: games) {
				
				game.newGame();
			}
			
			
			logDao.initDataSource(file);
			logDao.registerListenerObjects(); 
			
			// Read all messages until "Sim Start" was read.
			initialization();

			this.pushServiceReplayer.setStartNumberTs(logDao
					.getStartNumberTimeslot());
			
			// Create runner which sends messages.
			createRunner((long) (clockRate * Helper.MILLIS), 
					logDao.getStartNumberTimeslot(), false);

		} catch (FileNotFoundException e) {
			message = "Log file not found";
			throw new ErrorReadDomainObject("File not found");
		}
		
		return message;
    }

    /**
     * Reads log file until message "SimStart" is read.
     * 
     * @throws ErrorReadDomainObject Error
     */
    public void initialization() throws ErrorReadDomainObject {
    	
        logDao.readNextObject();	
        
		while (!logDao.isSimStart()) { 
			
			logDao.readNextObject();
		}
    }
    
	/**
	 * A new timeslot was selected. Sets the new data and sends push messages to
	 * Highchart. Only for extended mode.
	 * 
	 * @param oldTimeslot The previous timeslot
	 * @param timeslot The new timeslot
	 */
    @Override
	public void updateView(int oldTimeslot, int timeslot) {
    	// Not implemented.
    }
    
	/**
	 * Creates the TimeslotRunnerGeneral thread.
	 * 
	 * @param tickInterval The clock rate
	 * @param timeslot The current timeslot
	 * @param isReloadPage True if the page was reloaded
	 */
    public void createRunner(long tickInterval, int timeslot, 
    		boolean isReloadPage) {
    	
    	logTimeslotRunner = new TimeslotRunnerNormalMode(tickInterval, 
    			timeslot, isReloadPage);
    	
        Thread t = new Thread((TimeslotRunnerNormalMode) logTimeslotRunner);
        t.start();
    }
    
	/**
	 * Reads all data for one time slot.
	 * 
	 * @param time slot Time slot
	 * @throws ErrorReadDomainObject Error
	 */
	public void readAllDataForTimeslot(int timeslot) 
			throws ErrorReadDomainObject {
		        
        if (timeslot == logDao.getStartNumberTimeslot()) { 

        	// For first time slot messages must be ignored.
    		while (!(logDao.getNextObject().getClass() 
    				== TimeslotUpdate.class));
        }
        
        // Read next time slot.
        logDao.readNextObject();
        
		while (!logDao.isTimeslotComplete() && !logDao.isSimEnd() 
				&& !logDao.isFileClose()) {
			
			logDao.readNextObject();
		}
	}
	
	/**
	 * Sends push messages for Highchart between time slot "oldTimeslot" 
	 * and "timeslot".
	 * 
	 * @param oldTimeslot Previous time slot.
	 * @param timeslot New time slot.
	 */
	public void sendPushMessages(int oldTimeslot, int timeslot) {
		
		this.pushServiceReplayer.pushTimeslotsForCurrentView(new int[] 
				{oldTimeslot, timeslot});
	}
	
	/**
	 * Closes log file.
	 */
	public void closeFile() {
		
		try {
			
			logDao.closeDataSource();
		} catch (IOException e) {
		}
	}
	
	/**
	 * Pause the "TimeslotRunnerGeneral" thread.
	 */
	@Override
	public void pauseReplayer() {

    	if (this.logTimeslotRunner != null) {
        	
    		this.logTimeslotRunner.cancelTimer();
    	}
	}
	
	/**
	 * Stops the "TimeslotRunnerGeneral" thread.
	 */
	@Override
    public void stopRun() {
		
    	pauseReplayer();
    	closeFile();
    }
	
	/**
	 * Thread which reads messages for time slots and sends messages to client.
	 * 
	 * @author DWietoska
	 */
    public class TimeslotRunnerNormalMode extends TimeslotRunnerGeneral 
    		implements Runnable {

    	/**
    	 * ServletRequestAttributes.
    	 */
    	private ServletRequestAttributes attributes;
    	
    	/**
    	 * New runner.
    	 */
    	public TimeslotRunnerNormalMode() {
    		
    	}
    	
        /**
         * New runner.
         * 
         * @param clockRate Clock-rate
         * @param timeslot Current time slot
         * @param isReloadPage Page reloaded
         */
        public TimeslotRunnerNormalMode(long clockRate, int timeslot, 
        		boolean isReloadPage) {

        	attributes = (ServletRequestAttributes) RequestContextHolder
        			.getRequestAttributes();
        	this.tickInterval = clockRate;
        	this.currentTimeslot = timeslot;
        	this.nextTick = timeslot - 1;
        	this.cancel = false;
        	this.tickIntervalChanged = false;
        	this.isReloadPage = isReloadPage;
        }
        
        /**
         * Create simulation clock control.
         */
		@Override
		public void run() {

			RequestContextHolder.setRequestAttributes(attributes);
			
			clock = SimulationClockControlReplayer
					.getInstance(logDao.getStartNumberTimeslot());

			if (isReloadPage && tickInterval <= 4000) {

				clock.setTickInterval(WAIT_FIRST_TICK_RELOAD_PAGE);

				tickIntervalChanged = true;
			} else {

				clock.setTickInterval(tickInterval);
			}

			clock.setNextTick(nextTick);
			clock.setExecutionTime(0);
			clock.scheduleTick();

			isRunning = true;

			while (!logDao.isSimEnd() && !cancel) {

				safeTimeslot = currentTimeslot;
				clock.waitForTick(currentTimeslot);

				if (!cancel) {

					try {

						clock.setExecutionTime(sendTimeslotMessages(currentTimeslot));
						currentTimeslot += 1;
					} catch (Exception e) {
					}
				}
				
				if (!cancel) {
					
					if (tickIntervalChanged) {
						
						clock.setTickInterval(tickInterval);
						tickIntervalChanged = false;
					}
					
					clock.complete();
				}
			}

			isRunning = false;
			
			RequestContextHolder.resetRequestAttributes();
		}
    }
    
    /**
     * Runs a time slot of the simulation
     *
     * @return The execution time
     * @throws ErrorReadDomainObject 
     */
    private long sendTimeslotMessages(int timeslot) 
    		throws ErrorReadDomainObject {

        long start;
        long end;

        start = new Date().getTime();

        this.readAllDataForTimeslot(timeslot);

		this.logParametersBean.setTimeslot(timeslot);
        
		this.sendPushMessages(timeslot - 1, timeslot);
		
        end = new Date().getTime();
        return end - start;
    }

    /**
     * Getter TimeslotRunnerNormalMode.
     */
	@Override
    public TimeslotRunnerNormalMode getLogTimeslotRunner() {
    	return (TimeslotRunnerNormalMode) logTimeslotRunner;
    }

	@Override
	public void destroy() throws Exception {
		closeFile();
	}

	@Override
	public void runInit(File logFile, double clockRate)
			throws ErrorReadDomainObject {
	}
}
