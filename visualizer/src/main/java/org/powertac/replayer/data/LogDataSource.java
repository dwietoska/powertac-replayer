package org.powertac.replayer.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.powertac.replayer.GameInitialization;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

/**
 * Provides methods for processing the log file.
 * 
 * @author DWietoska
 */
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LogDataSource implements GameInitialization {
	
	/**
	 * Log file.
	 */
	private BufferedReader file;

    /**
     * Specifies if the file is closed.
     */
    private boolean isFileClose;
    
    /**
     * Default constructor.
     */
    public LogDataSource() {
    	
    }
    
	/**
	 * Reset for new replaying.
	 */
	@Override
	public void newGame() {

		this.file = null;
		this.isFileClose = false;
	}
	
	/**
	 * Init method.
	 * 
	 * @param filename File name
	 * @throws FileNotFoundException File not exists.
	 */
	public void init(String filename) throws FileNotFoundException {

        isFileClose = false;
	}
	
	/**
	 * Init method.
	 * 
	 * @param logFile Log file
	 * @throws FileNotFoundException File not exists.
	 */
	public void init(File logFile) throws FileNotFoundException {

		this.file = new BufferedReader(new FileReader(logFile)); 
		
        isFileClose = false;
	}

	/**
	 * Read next line from file.
	 * 
	 * @return Next line
	 * @throws IOException Exception
	 */
    public String readNextLineFromLog() throws IOException {
    	
    	String line = null;

    	if (!isFileClose) {
    		
    		line = file.readLine();
    	} 

        return line;
    }
	
    /**
     * Closes the file.
     * 
     * @throws IOException Exception
     */
	public void closeFile() throws IOException {
		isFileClose = true;
		this.file.close();
	}

	/** Getter and Setter. */
	public boolean isFileClose() {
		return isFileClose;
	}

	public void setFileClose(boolean isFileClose) {
		this.isFileClose = isFileClose;
	}
}
