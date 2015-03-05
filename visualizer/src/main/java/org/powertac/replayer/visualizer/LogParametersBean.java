package org.powertac.replayer.visualizer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpSession;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.powertac.replayer.Runner;
import org.powertac.replayer.StrategyFactory;
import org.powertac.replayer.logtool.ErrorReadDomainObject;
import org.powertac.replayer.utils.Helper;
import org.powertac.replayer.utils.Mode;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

/**
 * Holds all Data for replaying view.
 * 
 * @author DWietoska
 */
@Service
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class LogParametersBean implements Serializable {

	/**
	 * serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constant for normal mode.
	 */
	public static final String MODE_NORMAL = "normal";

	/**
	 * Constant for extended mode.
	 */
	public static final String MODE_EXTENDED = "extended";
	
	/**
	 * Url webapp.
	 */
	public static final String URL_REPLAYER = "/visualizer/app/";
	
	/**
	 * Url for log file.
	 */
	private String logUrl;
	
	/**
	 * Contains the session id.
	 */
	private String sessionId;
	
	/**
	 * Contains the choosen mode.
	 */
	private boolean isReplayerMode = true;
	
	/**
	 * Strategy Pattern. Contains methods for normal and extended mode.
	 */
	@Autowired
	private StrategyFactory runnerStrategyFactory;

	/**
	 * Contains the current clock rate.
	 */
	private double tsClockRate;

	/**
	 * Specifies whether the break button should appear.
	 */
	private boolean showBreakButton;

	/**
	 * Specifies whether replaying games is running.
	 */
	private boolean isReplayerRunning;

	/**
	 * Specifies whether all data are loading.
	 */
	private boolean isAllDataLoad;
	
	/**
	 * Contains the current mode. Default is normal mode.
	 */
	private String mode = "normal";

	/**
	 * Contains the choosen mode.
	 */
	private Mode choosenMode = Mode.NORMAL;

	/**
	 * Current timeslot of the slider.
	 */
	private volatile int timeslot;

	/**
	 * Previous timeslot of the slider.
	 */
	private int oldTimeslot;

	/**
	 * Max. timeslot value
	 */
	private Integer timeslotMaxValue;

	/**
	 * Min. timeslot value
	 */
	private Integer timeslotMinValue;

	/**
	 * Says if the extended mode was selected.
	 */
	private boolean isExtendedMode = false;
	  
	/**
	 * Contains the choosen file for replaying.
	 */
	private File chooseFile;
	
	/**
	 * Name from choosen file.
	 */
	private String chooseFilename;
	
	/**
	 * Name from current view.
	 */
	private String choosenViewName;
	
	/**
	 * Specifies the percentage of time slots have been loaded.
	 */
	private volatile int progress;
	
	/**
	 * Contains the last loaded time slot number.
	 */
	private volatile int lastLoadedTimeslotNumber;
	
	/**
	 * Strategy-Pattern. Contains normal or extended runner.
	 */
	private Runner currentRunner;
	
	/**
	 * Contains true, if the file is uploaded.
	 */
	private boolean isFileUploaded;
	
	/**
	 * Buffer for downloading files.
	 */
	final static int BUFFER = 2048;
	
	/**
	 * File extension from log files.
	 */
	public final static String END_OF_FILE_STATE = ".state";
	
	/**
	 * File extension from log files.
	 */
	public final static String END_OF_FILE_TAR = ".tar.gz";
	
	/**
	 * Specifies whether PrimeFaces-Component SelectOneButton should disable.
	 */
	private boolean disableSelectOneButton;
	
	/**
	 * Specifies whether PrimeFaces-Component Spinner should disable.
	 */
	private boolean disableSpinner;

	/**
	 * Specifies whether PrimeFaces-Component FileUpload should disable.
	 */
	private boolean disableFileUpload;
	
	/**
	 * Contains the url that has been passed by the replayer Webflow.
	 */
	private String urlString;
	
	/**
	 * Construct new LogParametersBean.
	 */
	public LogParametersBean() {
		
		this.showBreakButton = false;
		this.isAllDataLoad = false;
		this.isReplayerRunning = false;
		this.disableSelectOneButton = false;
		this.disableSpinner = true;
		this.disableFileUpload = false;
		this.tsClockRate = Helper.DEFAULT_TS_CLOCK_RATE;
	}
	
	/**
	 * Called when flow replayer is running. This method takes the
	 * get param and save it and sets variables for enable and disable.
	 * 
	 * @param urlString The given url
	 */
	public void setLogFileUrl(String urlString) {
		
		if (!this.getisReplayerRunning() && urlString != null && 
				!urlString.isEmpty()) {

			this.urlString = urlString;
			
			shutDown();
			
			// Sets variables for enable and disable.
			this.showBreakButton = false;
			this.isAllDataLoad = false;
			this.disableSelectOneButton = true;
			this.disableSpinner = true;
			this.disableFileUpload = true;
			
			this.isExtendedMode = true;
			this.choosenMode = Mode.EXTENTED;
			this.mode = "extended";
			
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage("anotherkey", 
					new FacesMessage(FacesMessage.SEVERITY_INFO, 
					"Info", 
					"File will load. This process may take several minutes"));
		}
	}
	
	/**
	 * Called when flow replayer is running. This method loads the tar archive
	 * or state file.
	 */
	public void createLogFile() {
		
		if (!this.getisReplayerRunning() && urlString != null && 
				!urlString.isEmpty()) {

			if (!(urlString.endsWith(END_OF_FILE_STATE) || 
					urlString.endsWith(END_OF_FILE_TAR))) {
					
				FacesContext context = FacesContext.getCurrentInstance();	
				context.addMessage(null, 
						new FacesMessage(FacesMessage.SEVERITY_WARN, 
						"Warning", "Unsupported File"));
				
				this.showBreakButton = false;
				this.isAllDataLoad = false;			
				this.disableSelectOneButton = false;
				this.disableSpinner = true;
				this.disableFileUpload = false;
				
				this.urlString = null;	
				return;
			} 
			
			try {

				FacesContext fc = FacesContext.getCurrentInstance();
				HttpSession session = (HttpSession) fc.getExternalContext()
						.getSession(false);
				this.sessionId = session.getId();

				URL url = new URL(urlString);
				URLConnection con = url.openConnection();

				File file = null;
				
				if (urlString.endsWith(END_OF_FILE_STATE)) {
					
					file = new File(this.sessionId + END_OF_FILE_STATE);
				} else {
					
					file = new File(this.sessionId + END_OF_FILE_TAR);
				}
				
				BufferedInputStream bis = new BufferedInputStream(
						con.getInputStream());
				FileOutputStream fos1 = new FileOutputStream(file.getName());
				BufferedOutputStream bos = new BufferedOutputStream(fos1);

				int i;

				while ((i = bis.read()) != -1) {
					
					bos.write(i);
				}

				bis.close();
				bos.flush();
				bos.close();
				
				if (urlString.endsWith(END_OF_FILE_STATE)) {

					String[] nameContent = urlString.split("/");
					processStateFile(nameContent[nameContent.length - 1], 
							new FileInputStream(file));
				} else {
					
					processTarGzArchiv(new FileInputStream(file));
				}
		
				file.delete();

				this.isFileUploaded = true;
				this.urlString = null;
				run();
				
			} catch (MalformedURLException e) {	
				handleErrorCreateStateFile();
			} catch (FileNotFoundException e) {			
				handleErrorCreateStateFile();
			} catch (IOException e) {
				handleErrorCreateStateFile();
			}
		}
	}
	
	/**
	 * Invoked when Webflow Replayer has an error.
	 */
	public void handleErrorCreateStateFile() {
		
		this.isFileUploaded = false;
		
		this.showBreakButton = false;
		this.isAllDataLoad = false;
		this.disableSelectOneButton = false;
		this.disableSpinner = true;
		this.disableFileUpload = false;
		this.isReplayerRunning = false;
		this.urlString = null;
		
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, 
				new FacesMessage(FacesMessage.SEVERITY_ERROR, 
				"Error", "File could not be loaded"));
	}
	
    /**
     * Called when a new replaying file was selected. 
     * Uploaded the selected file.
     */
	public void handleFileUpload(FileUploadEvent event) {

		if (this.getisReplayerRunning()) {
			return;
		}

		this.isFileUploaded = false;

		if (event.getFile().getFileName().endsWith(END_OF_FILE_STATE)) {

			// State-File was uploaded.
			try {
		
				processStateFile(event.getFile().getFileName(), event.getFile()
						.getInputstream());
				this.isFileUploaded = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (event.getFile().getFileName().endsWith(END_OF_FILE_TAR)) {

			// Tar-Archive was uploaded.
			
			try {
				
				processTarGzArchiv(event.getFile().getInputstream());
				isFileUploaded = true;
			} catch (MalformedURLException e) {
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * Processes the state-file. Creates a new file (The filename 
	 * consists of the session-id) which contains the state-file.
	 * 
	 * @param filename 
	 * @param inputStream Stream for state-file
	 * @throws IOException Stream could not be processed.
	 */
	public void processStateFile(String filename, InputStream inputStream) 
			throws IOException {
		
		this.chooseFilename = filename;

		OutputStream outputStream = null;
		this.chooseFile = null;

		this.chooseFile = new File(Helper.PATH_LOG_FILES + sessionId
				+ END_OF_FILE_STATE);

		outputStream = new FileOutputStream(this.chooseFile);

		int read = 0;
		byte[] bytes = new byte[1024];

		while ((read = inputStream.read(bytes)) != -1) {
			
			outputStream.write(bytes, 0, read);
		}

		if (inputStream != null) {

			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (outputStream != null) {

			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Processes the log archive. Only .state files are loaded and saved
	 * in directory log. The name consists of session-id and .state extension.
	 * 
	 * @param input Tar-archive as input stream.
	 * @throws MalformedURLException Error.
	 * @throws FileNotFoundException File not found.
	 * @throws IOException Stream could not be processed.
	 */
	public void processTarGzArchiv(InputStream input) 
			throws MalformedURLException, FileNotFoundException, IOException {

		BufferedInputStream in = new BufferedInputStream(input);
		GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in);
		TarArchiveInputStream tarIn = new TarArchiveInputStream(gzIn);

		TarArchiveEntry entry = null;

		// Reads the tar entries using the getNextEntry method.
		while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {

			String[] packages = entry.getName().split("/");
			String filename = packages[packages.length - 1];

			// Ascertains state-file from tar-archive.
			if (!Pattern.matches("init.state", filename)
					&& Pattern.matches("[\\w\\p{Punct}]+\\.state", filename)) {

				int count;
				byte data[] = new byte[BUFFER];

				this.chooseFilename = filename;
				this.chooseFile = new File(Helper.PATH_LOG_FILES
						+ this.sessionId + END_OF_FILE_STATE); // this.chooseFilename

				FileOutputStream fos = new FileOutputStream(
						this.getChooseFile());
				BufferedOutputStream dest = new BufferedOutputStream(fos,
						BUFFER);

				while ((count = tarIn.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}

				dest.close();
			}
		}

		// Close the input stream
		tarIn.close();
	}
	
	/**
	 * Messages for file upload.
	 */
	public void messageFileUpload() {
		
		FacesContext context = FacesContext.getCurrentInstance();
		
		if (this.isFileUploaded) {
			
			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, "Info", 
					"The file has been uploaded successfully"));
		} else {
			
			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_WARN, "Warning", 
					"File was not uploaded (Only .state and .tar.gz "
					+ "allowed)"));
		}
	}
	
	/**
	 * Called when FileUpload starts and before method run executes.
	 */
	public void actionBeforeReplaying() {

		if (this.getisReplayerRunning()) {
			return;
		}

		this.showBreakButton = false;
		this.isAllDataLoad = false;
		
		shutDown();
		
		this.disableSelectOneButton = true;
		this.disableSpinner = true;
		this.disableFileUpload = true;
	}
	
	/**
	 * Executes the selected game.
	 */
	public void run() {

		if (this.getisReplayerRunning()) {
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_WARN, "Warning", 
					"A game is running already"));
			return;
		}

		if (!this.isFileUploaded) {

			this.isAllDataLoad = false;	
			this.disableSelectOneButton = false;
			this.disableSpinner = true;
			this.disableFileUpload = false;
			return;
		}

		if (this.getChooseFile() == null) {

			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_WARN, "Warning", 
					"No File is choosen"));
			return;
		}
		
		// Strategy Pattern
		this.currentRunner = runnerStrategyFactory.getStrategy(this
				.getChoosenMode().toString());
		
		try {
			
			// Run
			this.currentRunner.run(this.getChooseFile(), 
					this.getTsClockRate());
			
			// Sets variables for enable and disable.
			this.setReplayerRunning(true);
			this.showBreakButton = true;
			this.isAllDataLoad = true;
			this.disableSpinner = false;
			this.disableFileUpload = true;
						
			// Send message
			PushContext pushContext = PushContextFactory.getDefault()
					.getPushContext();
					
			if (pushContext != null) {
				
				pushContext.push("/dataComplete" + sessionId, "");
			}
			
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_INFO, "Info", 
					"The game started successfully."));
		} catch (ErrorReadDomainObject exception) {
			
			// Sets Variables for enable and disable.
			this.disableSpinner = true;
			this.disableFileUpload = false;

			// Error-Message
			String errorMessage = "";
			
			if (exception.getLine() != null) {
				errorMessage = "Line: " + exception.getLine() + " \n ";
			}
			
			errorMessage += exception.getMessage();
	
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage(
					FacesMessage.SEVERITY_ERROR, "Error", errorMessage));
		}
	}

	/**
	 * Stops the replaying game.
	 */
	public void shutDown() {

		if (this.getisReplayerRunning()) {

			this.currentRunner.stopRun();
			this.setReplayerRunning(false);
			
			if (this.getChoosenMode() == Mode.NORMAL) {
				
				this.lastLoadedTimeslotNumber = 0;
				this.progress = 0;
			}
		}
		
		this.isAllDataLoad = false;	
		this.disableSelectOneButton = false;
		this.showBreakButton = false;
		this.disableSpinner = true;
		this.disableFileUpload = false;
		
		this.isFileUploaded = false;
	}
	
    /**
     * Called when a new mode was selected.
     */
	public void valueChangeChooseMode(ValueChangeEvent e) {
		
		mode = (String) e.getNewValue();

		if (mode != null && mode.equals("extended")) {
	
			isExtendedMode = true;
			choosenMode = Mode.EXTENTED;
		} else {
			
			isExtendedMode = false;
			choosenMode = Mode.NORMAL;
		}
	}
	
    /**
     * Called when sliding starts.
     */
    public void onSlideStart() {
    	
    	this.pauseReplayer();
    }
    
    /**
     * Called when a new timeslot was selected. It updates the view.
     */
    public void onSlideEnd(SlideEndEvent event) {

    	int slideValue = event.getValue();

    	if (this.lastLoadedTimeslotNumber <= slideValue) {
    		
    		slideValue = this.lastLoadedTimeslotNumber;
    	}

    	this.oldTimeslot = this.timeslot;
    	this.timeslot = slideValue;
    	
    	// Update view
      	this.currentRunner.updateView(this.oldTimeslot, this.timeslot);
    }
    
    /**
     * Called when a new clock rate was selected.
     */
    public void valueChangeClockRate() {
    	
    	// Change clock-rate.
    	this.currentRunner.clockRateReplayer(this.tsClockRate);
    }
    
    /**
     * Called when button "Pause" was clicked.
     */
    public void pauseReplayer() {
    	
    	showBreakButton = false;

    	// Pause
    	this.currentRunner.pauseReplayer();
    }
    
    /**
     * Called when button "Continue" was clicked.
     */
    public void continueReplayer() {

    	showBreakButton = true;    	

    	// Continue
    	this.currentRunner.continueReplayer(this.tsClockRate, this.timeslot);
    }
	
    /** Getter and Setter methods.
     */
    public int getTimeslot() {
        return timeslot;
    }
 
    public void setTimeslot(int timeslot) {
    	this.oldTimeslot = this.timeslot;
        this.timeslot = timeslot;
    }
	
	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public boolean getIsExtendedMode() {
		return isExtendedMode;
	}

	public void setIsExtendedMode(boolean isExtendedMode) {
		this.isExtendedMode = isExtendedMode;
	}

	public File getChooseFile() {
		return chooseFile;
	}

	public void setChooseFile(File chooseFile) {
		this.chooseFile = chooseFile;
	}

	public Integer getTimeslotMaxValue() {
		return timeslotMaxValue;
	}
	
	public void setTimeslotMaxValue(Integer timeslotMaxValue) {
		this.timeslotMaxValue = timeslotMaxValue;
	}
	
	public Integer getTimeslotMinValue() {
		return timeslotMinValue;
	}

	public void setTimeslotMinValue(Integer timeslotMinValue) {
		this.timeslotMinValue = timeslotMinValue;
	}

	public int getOldTimeslot() {
		return oldTimeslot;
	}

	public void setOldTimeslot(int oldTimeslot) {
		this.oldTimeslot = oldTimeslot;
	}

	public boolean getisReplayerRunning() {
		return isReplayerRunning;
	}

	public void setReplayerRunning(boolean isReplayerRunning) {
		this.isReplayerRunning = isReplayerRunning;
	}

	public boolean getisAllDataLoad() {		
		return isAllDataLoad;
	}

	public void setAllDataLoad(boolean isAllDataLoad) {
		this.isAllDataLoad = isAllDataLoad;
	}

	public double getTsClockRate() {
		return tsClockRate;
	}

	public void setTsClockRate(double tsClockRate) {
		this.tsClockRate = tsClockRate;
	}

	public boolean getisShowBreakButton() {
		return showBreakButton;
	}

	public void setShowBreakButton(boolean showBreakButton) {
		this.showBreakButton = showBreakButton;
	}

	public Mode getChoosenMode() {
		return choosenMode;
	}

	public void setChoosenMode(Mode choosenMode) {
		this.choosenMode = choosenMode;
	}

	public String getChooseFilename() {
		return chooseFilename;
	}

	public void setChooseFilename(String chooseFilename) {
		this.chooseFilename = chooseFilename;
	}

	public String getChoosenViewName() {
		return choosenViewName;
	}

	public void setChoosenViewName(String choosenViewName) {
		this.choosenViewName = choosenViewName;
	}

	public String getLogUrl() {
		return logUrl;
	}

	public void setLogUrl(String logUrl) {
		this.logUrl = logUrl;
	}
	
	public int getProgress() {		
		return progress;
	}

	public void setProgress(int progress) {		
		this.progress = progress;
	}

	public int getLastLoadedTimeslotNumber() {
		return lastLoadedTimeslotNumber;
	}

	public void setLastLoadedTimeslotNumber(int lastLoadedTimeslotNumber) {
		this.lastLoadedTimeslotNumber = lastLoadedTimeslotNumber;
	}

	public Runner getCurrentRunner() {
		return currentRunner;
	}

	public void setCurrentRunner(Runner currentRunner) {
		this.currentRunner = currentRunner;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {	
		this.sessionId = sessionId;
	}

	public boolean getIsReplayerMode() {
		return isReplayerMode;
	}

	public void setIsReplayerMode(boolean isReplayerMode) {
		this.isReplayerMode = isReplayerMode;
	}
		
	public void setReplayerMode(String isReplayerMode) {	
		this.isReplayerMode = Boolean.valueOf(isReplayerMode);
	}
	
	public boolean isDisableSpinner() {
		return disableSpinner;
	}

	public void setDisableSpinner(boolean disableSpinner) {
		this.disableSpinner = disableSpinner;
	}

	public boolean isDisableSelectOneButton() {
		return disableSelectOneButton;
	}

	public void setDisableSelectOneButton(boolean disableSelectOneButton) {
		this.disableSelectOneButton = disableSelectOneButton;
	}
	
	public boolean isDisableFileUpload() {
		return disableFileUpload;
	}

	public void setDisableFileUpload(boolean disableFileUpload) {
		this.disableFileUpload = disableFileUpload;
	}
}
