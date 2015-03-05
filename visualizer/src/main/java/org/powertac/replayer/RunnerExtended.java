package org.powertac.replayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.powertac.replayer.data.LogDataImpl;
import org.powertac.replayer.logtool.ErrorReadDomainObject;
import org.powertac.replayer.pusher.PushServiceReplayerNew;
import org.powertac.replayer.tsSimulation.SimulationClockControlReplayer;
import org.powertac.replayer.utils.Helper;
import org.powertac.replayer.visualizer.LogParametersBean;
import org.powertac.visualizer.VisualizerApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Implements the extended mode for replaying. Uses current replaying data structure.
 * 
 * @author DWietoska
 */
@Service("RunnerExtended")
@Scope(value = "prototype")
public class RunnerExtended extends RunnerGeneral {
	
	/**
	 * Access to spring bean.
	 */
	@Autowired
	private LogParametersBean logParametersBean;

	/**
	 * Processed and contains all data for replayer.
	 */
	@Autowired
	private LogDataImpl logDao;
	
	/**
	 * Send time slot messages.
	 */
	@Autowired
	private PushServiceReplayerNew pushServiceReplayer;
		
	/**
	 * True when a time slot have changed.
	 */
    private boolean tsChanged;
    
	/**
	 * New time slot.
	 */
    private int newTimeslot;
    
	/**
	 * Specifies whether all objects was read.
	 */
	private volatile boolean isReadAllObjects;
	
	/**
	 * First time slot number. 
	 */
	private int startNumberTimeslot;
	
	/**
	 * Number of time slots.
	 */
	private volatile int numberOfTimeslots;
	
	/**
	 * Creates a new extended runner.
	 */
    public RunnerExtended() {
    	
    	this.tsChanged = false;
    }
	
	/**
	 * Restart a selected game.
	 * 
	 * @param file The selected file
	 * @param clockRate Clock rate
	 * @throws ErrorReadDomainObject Error
	 */
	@Override
	public String run(File logFile, double clockRate) 
			throws ErrorReadDomainObject {
		
		String message = null;
		
		try {

			// A new game was started. Reset all beans.
			List<GameInitialization> games = VisualizerApplicationContext
					   .listBeansOfType(GameInitialization.class);
			
			for (GameInitialization game: games) {
				
				game.newGame();
			}
			
			logDao.registerListenerObjects(); 

			logDao.initDataSource(logFile);
			
			// Read all messages until "Sim Start" was read.
			logDao.readNextObject();
			
			while (!logDao.isSimStart()) {
				
				logDao.readNextObject();
			}

			// Read all messages until "Sim End" was read.
			logDao.readAllObjects();

			closeFile();
			
			// Set helper variables.
			isReadAllObjects = false;
			this.numberOfTimeslots = logDao.getNumberOfTimeslots();
			
			this.startNumberTimeslot = logDao.getStartNumberTimeslot();
			this.newTimeslot = logDao.getStartNumberTimeslot();
			
			this.pushServiceReplayer.setStartNumberTs(logDao
					.getStartNumberTimeslot());

			this.logParametersBean.setTimeslotMinValue(logDao
					.getStartNumberTimeslot());
			this.logParametersBean.setTimeslotMaxValue(logDao
					.getNumberOfTimeslots());		
			this.logParametersBean.setTimeslot(logDao
					.getStartNumberTimeslot());
		
			// Create runner which sends messages.
			createRunner((long) (clockRate * Helper.MILLIS),
					logDao.getStartNumberTimeslot(), false);
		} catch (FileNotFoundException e) {
			message = "Log-Datei wurde nicht gefunden";
			throw new ErrorReadDomainObject("File not found");
		}

		return message;
	}

	/**
	 * A new timeslot was selected. Sends push messages to
	 * Highchart. Only for extended mode.
	 * 
	 * @param oldTimeslot The previous timeslot
	 * @param timeslot The new timeslot
	 */
    @Override
	public void updateView(int oldTimeslot, int timeslot) {

		this.tsChanged = true;
	
		this.newTimeslot = timeslot + 1;	

		this.logTimeslotRunner.notifyTick();
		
		this.sendPushMessages(oldTimeslot, timeslot);
	}
    
	/**
	 * Creates the TimeslotRunnerGeneral thread.
	 * 
	 * @param tickInterval The clock rate
	 * @param timeslot The current timeslot
	 * @param isReloadPage True if the page was reloaded
	 */
	@Override
    public void createRunner(long tickInterval, int timeslot, 
    		boolean isReloadPage) {

    	logTimeslotRunner = new TimeslotRunnerExtendedMode(tickInterval, 
    			timeslot, isReloadPage);

        Thread t = new Thread((TimeslotRunnerExtendedMode) logTimeslotRunner);
        t.start();
    }
    
	/**
	 * Closes log file.
	 */
	public void closeFile() {
		
		try {
			
			logDao.closeDataSource();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * Thread which sends messages to client.
	 * 
	 * @author DWietoska
	 */
    public class TimeslotRunnerExtendedMode extends TimeslotRunnerGeneral 
    		implements Runnable {
    	
    	/**
    	 * ServletRequestAttributes.
    	 */
    	private ServletRequestAttributes attributes;
    	 
    	/**
    	 * New runner.
    	 */
        public TimeslotRunnerExtendedMode() {
        	
        }
       
        /**
         * New runner.
         * 
         * @param clockRate Clock-rate
         * @param timeslot Current time slot
         * @param isReloadPage Page reloaded
         */
        public TimeslotRunnerExtendedMode(long clockRate, int timeslot, 
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
            		.getInstance(startNumberTimeslot);
            
			if (isReloadPage && tickInterval <= 5000) {

				clock.setTickInterval(WAIT_FIRST_TICK_RELOAD_PAGE);
				
				tickIntervalChanged = true;
			} else {

				clock.setTickInterval(tickInterval);
			}
            
            clock.setNextTick(nextTick);
            clock.setExecutionTime(0);
            clock.scheduleTick();

			isRunning = true;

			while (( isReadAllObjects || currentTimeslot <= numberOfTimeslots) 
					&& !cancel) {

				safeTimeslot = currentTimeslot;
				tsChanged = false;
				clock.waitForTick(currentTimeslot);
				
				if (!tsChanged && !cancel) {
					
					try {
						
						clock.setExecutionTime(sendTimeslotMessages(currentTimeslot));
					} catch (Exception e) {
					}
				}

				if (!cancel) {

					if (!tsChanged) {
						
						currentTimeslot += 1;
					} else {
						
						currentTimeslot = newTimeslot;
						clock.setNextTick(newTimeslot - 1);
					}
					
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
     * Runs a time slot for simulation.
     *
     * @return The execution time.
     */
    private long sendTimeslotMessages(int timeslot) {

        long start;
        long end;

        start = new Date().getTime();

		this.logParametersBean.setTimeslot(timeslot);

        this.sendPushMessages(timeslot - 1, timeslot);

        end = new Date().getTime();
        return end - start;
    }
	
    /**
     * Getter TimeslotRunnerExtendedMode.
     */
	@Override
    public TimeslotRunnerExtendedMode getLogTimeslotRunner() {
		
    	return (TimeslotRunnerExtendedMode) logTimeslotRunner;
    }
	
	/** Getter and setter methods **/
	public boolean isReadAllObjects() {
		return isReadAllObjects;
	}

	public void setReadAllObjects(boolean isReadAllObjects) {
		this.isReadAllObjects = isReadAllObjects;
	}
	
	public int getNumberOfTimeslots() {
		return numberOfTimeslots;
	}

	public void setNumberOfTimeslots(int numberOfTimeslots) {
		this.numberOfTimeslots = numberOfTimeslots;
	}
}

