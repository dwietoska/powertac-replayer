package org.powertac.replayer.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.powertac.common.Competition;
import org.powertac.common.msg.SimEnd;
import org.powertac.common.msg.SimStart;
import org.powertac.replayer.logtool.ErrorReadDomainObject;
import org.powertac.replayer.logtool.ReplayerDomainObjectReader;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides methods to process lines from log files.
 * 
 * @author DWietoska
 */
public abstract class LogData {
	
	/**
	 * Access to ReplayerDomainObjectReader.
	 */
	@Autowired
	protected ReplayerDomainObjectReader domainObjectReader;
	
	/**
	 * Access to LogDataSource.
	 */
	@Autowired
	protected LogDataSource logDataSource;
	
	/**
	 * Competition.
	 */
	protected Competition competition;

	/**
	 * Specifies if message "SimStart" has been read.
	 */
	protected boolean isSimStart;
	
	/**
	 * Sim Start message.
	 */
	protected SimStart simStart;
	
	/**
	 * Specifies if message "SimEnd" has been read.
	 */
	protected volatile boolean isSimEnd;
	
	/**
	 * Sim End message.
	 */
	protected SimEnd simEnd;
	
	/**
	 * Specifies if current time slot was completed.
	 */
	protected boolean timeslotComplete;
	
	/**
	 * Number of time slots.
	 */
	protected volatile int numberOfTimeslots;
	
	/**
	 * Start number from all time slots.
	 */
	protected int startNumberTimeslot;
	
	/**
	 * End number from all time slots.
	 */
	protected int endNumberTimeslot;
	
	/**
	 * Default constructor.
	 */
	public LogData() {
	}
	
	/**
	 * Delegates filename to LogDataSource.
	 * 
	 * @param filename File name
	 * @throws FileNotFoundException Exception
	 */
	public void initDataSource(String filename) throws FileNotFoundException {
		logDataSource.init(filename);
	}
	
	/**
	 * Delegates file to LogDataSource.
	 * 
	 * @param file File
	 * @throws FileNotFoundException Exception
	 */
	public void initDataSource(File logFile) throws FileNotFoundException {
		logDataSource.init(logFile);
	}
	
	/**
	 * Processes a read line.
	 */
	public abstract void registerListenerObjects();
	
	/**
	 * Removes all listener.
	 */
	public void clearAllRegisterListenerObjects() {
		domainObjectReader.clearObjectListener();
	}
		
	/**
	 * Reads next line from file and gives it to domain object reader.
	 */
	public void readNextObject() throws ErrorReadDomainObject {

		String line = null;
		
		try {
			
			line = logDataSource.readNextLineFromLog();
			
			if (line != null) {
			
				domainObjectReader.readObject(line);
			}
		} catch (IOException e) {

			throw new ErrorReadDomainObject("IOException: " + e.getMessage());
		} catch (Exception e) {
			
			throw new ErrorReadDomainObject(line, e.getMessage());
		}
	}
	
	/**
	 * Reads and returns next line from file.
	 */
	public Object getNextObject() throws ErrorReadDomainObject {

		Object domainObject = null;
		String line = null;

		try {
			line = logDataSource.readNextLineFromLog();
			
			if (line != null) {
			
				domainObject = domainObjectReader.readObject(line);
			}
		} catch (IOException e) {

			throw new ErrorReadDomainObject("IOException: " + e.getMessage());
		} catch (Exception e) {
			
			throw new ErrorReadDomainObject(line, e.getMessage());
		}
		
		return domainObject;
	}
	
	/**
	 * Closes file.
	 */
	public void closeDataSource() throws IOException {
    	
		logDataSource.closeFile();
	}
	
	/**
	 * Specifies if file has been closed.
	 * 
	 * @return true or false
	 */
	public boolean isFileClose() {
		
		return logDataSource.isFileClose();
	}
	
	/** Getter and Setter. **/
	public ReplayerDomainObjectReader getDomainObjectReader() {
		return domainObjectReader;
	}

	public void setDomainObjectReader(ReplayerDomainObjectReader domainObjectReader) {
		this.domainObjectReader = domainObjectReader;
	}

	public LogDataSource getLogDataSource() {
		return logDataSource;
	}

	public void setLogDataSource(LogDataSource logDataSource) {
		this.logDataSource = logDataSource;
	}

	public Competition getCompetition() {
		return competition;
	}

	public void setCompetition(Competition competition) {
		this.competition = competition;
	}

	public boolean isSimStart() {
		return isSimStart;
	}

	public void setSimStart(boolean isSimStart) {
		this.isSimStart = isSimStart;
	}

	public SimStart getSimStart() {
		return simStart;
	}

	public void setSimStart(SimStart simStart) {
		this.simStart = simStart;
	}

	public boolean isSimEnd() {
		return isSimEnd;
	}

	public void setSimEnd(boolean isSimEnd) {
		this.isSimEnd = isSimEnd;
	}

	public SimEnd getSimEnd() {
		return simEnd;
	}

	public void setSimEnd(SimEnd simEnd) {
		this.simEnd = simEnd;
	}

	public boolean isTimeslotComplete() {
		return timeslotComplete;
	}

	public void setTimeslotComplete(boolean timeslotComplete) {
		this.timeslotComplete = timeslotComplete;
	}

	public int getNumberOfTimeslots() {
		return numberOfTimeslots;
	}

	public void setNumberOfTimeslots(int numberOfTimeslots) {
		this.numberOfTimeslots = numberOfTimeslots;
	}

	public int getStartNumberTimeslot() {
		return startNumberTimeslot;
	}

	public void setStartNumberTimeslot(int startNumberTimeslot) {
		this.startNumberTimeslot = startNumberTimeslot;
	}

	public int getEndNumberTimeslot() {
		return endNumberTimeslot;
	}

	public void setEndNumberTimeslot(int endNumberTimeslot) {
		this.endNumberTimeslot = endNumberTimeslot;
	}
}
