package org.powertac.replayer.logtool;

/**
 * Error class for the log file-processes.
 * 
 * @author DWietoska
 */
public class ErrorReadDomainObject extends Exception {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Line in logfile.
	 */
	private String line;

	/**
	 * Create new Error class.
	 * 
	 * @param message The message
	 */
	public ErrorReadDomainObject(String message) {
		super(message);
	}
	
	/**
	 * Create new Error class.
	 * 
	 * @param line The line
	 * @param message The message
	 */
	public ErrorReadDomainObject(String line, String message) {
		super(message);
		this.line = line;
	}
	
	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}
}
