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
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
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
//@Scope(value = "session") // prototype
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
	 * This method read init data and starts the timeslot thread.
	 * 
	 *  For Solution with async=true (problem not async)   
	 * 
	 * @param file The selected file
	 * @param clockRate Clock rate
	 * @throws ErrorReadDomainObject Error
	 */
 	@Override
 	public void runInit(File logFile, double clockRate) 
 			throws ErrorReadDomainObject {
 		
 		this.tsChanged = false;
 		this.newTimeslot = 0;
 		this.isReadAllObjects = false;
 		
 		try {

 			// New game is started. Reset all beans.
 			List<GameInitialization> games = VisualizerApplicationContext
 					   .listBeansOfType(GameInitialization.class);
 			for (GameInitialization game: games) {
 				game.newGame();
 			}
 			
// 			logDao.clearAllRegisterListenerObjects();
 			logDao.registerListenerObjects(); 

 			logDao.initDataSource(logFile);
 			
 			// Init bis Sim Start
 			logDao.readNextObject();
 			while (!logDao.isSimStart()) {
 				logDao.readNextObject();
 			}
 	        
 			isReadAllObjects = true;
 			this.numberOfTimeslots = logDao.getStartNumberTimeslot();
 			this.startNumberTimeslot = logDao.getStartNumberTimeslot();
 			this.newTimeslot = logDao.getStartNumberTimeslot();
 			this.pushServiceReplayer.setStartNumberTs(logDao
 					.getStartNumberTimeslot());
 			this.logParametersBean.setTimeslotMinValue(logDao
 					.getStartNumberTimeslot());
 			this.logParametersBean.setTimeslotMaxValue(logDao.getCompetition()
 					.getExpectedTimeslotCount() + logDao
 					.getStartNumberTimeslot());
 			this.logParametersBean.setTimeslot(logDao
 					.getStartNumberTimeslot());
 			
 			createRunner((long) (clockRate * Helper.MILLIS),
 					logDao.getStartNumberTimeslot(), false); 
 		} catch (FileNotFoundException e) {
 			throw new ErrorReadDomainObject("File not found");
 		}
 	}
 	
	/**
	 * This method starts the background thread which reads and saves 
	 * all data.
	 * 
	 * @param file The selected file
	 * @param clockRate Clock rate
	 * @throws ErrorReadDomainObject Error
	 */
 	@Override
 	public String run(File logFile, double clockRate) 
 			throws ErrorReadDomainObject {

 		String message = null;

// 		logDao.readAllObjects();
// 		// logParametersBean
// 		// .setTimeslotMaxValue(logDao.getEndNumberTimeslot());
// 		closeFile();
// 		isReadAllObjects = false;
// 		this.numberOfTimeslots = logDao.getNumberOfTimeslots();
 //
// 		this.logParametersBean.setTimeslotMaxValue(logDao
// 				.getNumberOfTimeslots());

         Thread t = new Thread(new ReadAllObjects());
         t.start();
 		
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
    	
//    	/**
//    	 * ServletRequestAttributes.
//    	 */
//    	private ServletRequestAttributes attributes;
    	 
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

//        	attributes = (ServletRequestAttributes) RequestContextHolder
//        			.currentRequestAttributes();
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
			
//			synchronized (object) {
//				RequestContextHolder.setRequestAttributes(attributes, true);
//			}

            clock = SimulationClockControlReplayer
            		.getInstance(startNumberTimeslot);
            
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
//			RequestContextHolder.resetRequestAttributes();
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
	
//	/**
//	 * Synchronization object.
//	 */
//	private Object object = new Object();
	
	/**
	 * Reads and processes all data from log file.
	 * 
	 * @author DWietoska
	 */
	public class ReadAllObjects extends Thread {
		
//		private ServletRequestAttributes attributes;
		
		public ReadAllObjects() {
//			attributes = (ServletRequestAttributes) RequestContextHolder
//					.currentRequestAttributes();
		}
		
		@Override
		public void run() {

//			synchronized(object) {
//				RequestContextHolder.setRequestAttributes(attributes, true);
//			}

//			String sessionId = attributes.getSessionId();
			
			try {
				
				logDao.readAllObjects();
				
				// Send message
				PushContext pushContext = PushContextFactory.getDefault()
						.getPushContext();
				pushContext.push("/dataComplete", "");
			} catch (ErrorReadDomainObject e) {

				String errorMessage = "";
				
				if (e.getLine() != null) {
					errorMessage = "Line: " + e.getLine() + " \n ";
				}
				
				errorMessage += e.getMessage();
				
				PushContext pushContext = PushContextFactory.getDefault()
						.getPushContext();		
				pushContext.push("errorReadAllObjects", 
						errorMessage);
//				pushContext.push("errorReadAllObjects" + sessionId, 
//						errorMessage);
			}
			// logParametersBean
			// .setTimeslotMaxValue(logDao.getEndNumberTimeslot());
			closeFile();
			isReadAllObjects = false;
			numberOfTimeslots = logDao.getNumberOfTimeslots();

			logParametersBean.setTimeslotMaxValue(logDao
					.getNumberOfTimeslots());
			
//			RequestContextHolder.resetRequestAttributes();
		}
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
	
//----------------------------------------------------------    
//	/**
//	 * Restart a selected game. Previous solution.
//	 * At first this method reads init data and then all data.
//	 * At last this method starts the time slot thread.
//	 * 
//	 * @param file The selected file
//	 * @param clockRate Clock rate
//	 * @throws ErrorReadDomainObject Error
//	 */
//	@Override
//	public String run(File logFile, double clockRate) 
//			throws ErrorReadDomainObject {
//		
//		String message = null;
//		
//		try {
//
//			// A new game was started. Reset all beans.
//			List<GameInitialization> games = VisualizerApplicationContext
//					   .listBeansOfType(GameInitialization.class);
//			
//			for (GameInitialization game: games) {
//				
//				game.newGame();
//			}
//			
//			logDao.registerListenerObjects(); 
//
//			logDao.initDataSource(logFile);
//			
//			// Read all messages until "Sim Start" was read.
//			logDao.readNextObject();
//			
//			while (!logDao.isSimStart()) {
//				
//				logDao.readNextObject();
//			}
//
//			// Read all messages until "Sim End" was read.
//			logDao.readAllObjects();
//
//			closeFile();
//			
//			// Set helper variables.
//			isReadAllObjects = false;
//			this.numberOfTimeslots = logDao.getNumberOfTimeslots();
//			
//			this.startNumberTimeslot = logDao.getStartNumberTimeslot();
//			this.newTimeslot = logDao.getStartNumberTimeslot();
//			
//			this.pushServiceReplayer.setStartNumberTs(logDao
//					.getStartNumberTimeslot());
//
//			this.logParametersBean.setTimeslotMinValue(logDao
//					.getStartNumberTimeslot());
//			this.logParametersBean.setTimeslotMaxValue(logDao
//					.getNumberOfTimeslots());		
//			this.logParametersBean.setTimeslot(logDao
//					.getStartNumberTimeslot());
//		
//			// Create runner which sends messages.
//			createRunner((long) (clockRate * Helper.MILLIS),
//					logDao.getStartNumberTimeslot(), false);
//		} catch (FileNotFoundException e) {
//			message = "Log-Datei wurde nicht gefunden";
//			throw new ErrorReadDomainObject("File not found");
//		}
//
//		return message;
//	}
//----------------------------------------------------------    
	
//	@Override
//	public String run(File logFile, double clockRate) {
//
//		String message = null;
//System.out.println("Runner Extended 4 run()");
//		try {
//
//			logDao.clearAllRegisterListenerObjects();
//			logDao.registerListenerObjects(); // Klassen registrieren fuer
//												// Extended, da in Normal
//			// clearListener
//
//			logDao.initDataSource(logFile);
//			logDao.readAllObjects();
//
//			this.newTimeslot = logDao.getStartNumberTimeslot();
//			this.pushServiceReplayer.setStartNumberTs(logDao
//					.getStartNumberTimeslot());
//			
//			// Start Nummer erst ermittelt, nachdem Daten init. wurden,
//			// da vor SimStart immer ein TS Update.
//			// So muss die Datei nicht oefters eingelesen werden.
//			this.logParametersBean.setTimeslotMinValue(logDao
//					.getStartNumberTimeslot());
//			this.logParametersBean.setTimeslotMaxValue(logDao
//					.getEndNumberTimeslot());
//			this.logParametersBean.setTimeslot(logDao
//					.getEndNumberTimeslot());
//			
//			// Bis SimStart gelesen, nun können Timeslot Nachrichten
//			// gesendet werden.
//			createRunner((long) (clockRate * Helper.MILLIS),
//					logDao.getStartNumberTimeslot(), false); // Helper.STARTNUMBER_TIMESLOT
//		} catch (FileNotFoundException e) {
//			message = "Log-Datei wurde nicht gefunden";
//		}
//
//		return message;
//	}
	
//	/**
//	 * Reads and processes all data from log file.
//	 * 
//	 * @author DWietoska
//	 */
//	public class ReadAllObjects extends Thread {
//		
////		private ServletRequestAttributes attributes;
//		
//		public ReadAllObjects() {
//		}
//
////		@PostConstruct
////	    public void init() {
////	        // Grab current thread local request attributes.
////	        // These are available because we are not in the new 
////	        // thread yet.
////	        attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
////	    }
//		
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			System.out.println("Vor ReadAllObjects");
////			RequestContextHolder.setRequestAttributes(attributes);
//			isReadAllObjects = true;
//			try {
//				logDao.readAllObjects();
//			} catch (ErrorReadDomainObject e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			System.out.println("Fertig ReadAllObjects");
//			System.out.println(logDao.getEndNumberTimeslot());
//			logParametersBean
//					.setTimeslotMaxValue(logDao.getEndNumberTimeslot());
//			System.out.println("Vor CloseFile");
//			closeFile();
//			System.out.println("Ready!");
//			isReadAllObjects = false;
////			RequestContextHolder.resetRequestAttributes();
//		}
//	}
}

