/** 
 *  LogFileManager.java
 *
 *  VERSION: 2021.05.03
 *  AUTHORS: Rae Bouldin
 *  
 *  DESCRIPTION:
 *    The LogFileManager manages each of the five possible log files. It tracks
 *    which log files are opened and closed, and manages writing to these files.
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LogFileManager {
	
	public final int DEFAULT = 0;
	public final int ERROR = 1;
	public final int NATIVE = 2;
	public final int SERVER = 3;
	public final int USER = 4;
	
	private Date date;
	private String sessionID;
	
	private boolean fullLoggingIsOn;
	private boolean errorLoggingIsOn;
	private boolean nativeLoggingIsOn;
	private boolean serverLoggingIsOn;
	private boolean userLoggingIsOn;
	
	private LogFile fullLog;
	private LogFile errorLog;
	private LogFile nativeLog;
	private LogFile serverLog;
	private LogFile userLog;
	
	private File logDirectory;
	private File developerLogDirectory;
	
	public LogFileManager(Date date, String sessionID) throws IOException {
		this();
		this.date = date;
		this.sessionID = sessionID.replaceAll("\"", "");
	}
	
	public LogFileManager() throws IOException {
		
		this.date = Calendar.getInstance().getTime();
		this.sessionID = null;
		
		this.fullLoggingIsOn = false;
		this.errorLoggingIsOn = false;
		this.nativeLoggingIsOn = false;
		this.serverLoggingIsOn = false;
		this.userLoggingIsOn = false;
		
		this.fullLog = null;
		this.errorLog = null;
		this.nativeLog = null;
		this.serverLog = null;
		this.userLog = null;
		
		this.logDirectory = new File("LOGS/");
		this.developerLogDirectory = new File("LOGS/dev/");
		
	}
	
	/** Turn on full logging so that messages can be written to the full log 
	 *  file. Note that once logging is turned on, it cannot be turned off. */
	public void turnOnFullLogging() {
		this.fullLoggingIsOn = true;
		if (fullLog == null) {
			fullLog = createLogFile(developerLogDirectory, "LOG-" + sessionID + ".txt");
		}
	}
	
	/** Turn on error logging so that messages can be written to the error log 
	 *  file. Note that once logging is turned on, it cannot be turned off. */
	public void turnOnErrorLogging() {
		this.errorLoggingIsOn = true;
		if (errorLog == null) {
			errorLog = createLogFile(developerLogDirectory, "LOG-" + sessionID + "-Errors.txt");
		}
	}
	
	/** Turn on native logging so that messages can be written to the native 
	 *  log file. Note that once logging is turned on, it cannot be turned off. */
	public void turnOnNativeLogging() {
		this.nativeLoggingIsOn = true;
		if (nativeLog == null) {
			nativeLog = createLogFile(developerLogDirectory, "LOG-" + sessionID + "-Native.txt");
		}
	}
	
	/** Turn on server logging so that messages can be written to the server 
	 *  log file. Note that once logging is turned on, it cannot be turned off. */
	public void turnOnServerLogging() {
		this.serverLoggingIsOn = true;
		if (serverLog == null) {
			serverLog = createLogFile(developerLogDirectory, "LOG-" + sessionID + "-Server.txt");
		}
	}
	
	/** Turn on user logging so that messages can be written to the user log 
	 *  file. Note that once logging is turned on, it cannot be turned off. */
	public void turnOnUserLogging() {
		this.userLoggingIsOn = true;
		if (userLog == null) {
			userLog = createLogFile(logDirectory, "CHROME MONITOR LOG-" + sessionID + ".txt");
		}
	}
	
	/** Create a LogFile in the specified directory, making sure the directory 
	 *  exists first and creating it if it doesn't exist. */
	private LogFile createLogFile(File dir, String fileName) {
		if (! dir.exists()){
			dir.mkdirs();
	    }
		return new LogFile(dir.getPath() + "/" + fileName);
	}
	
	/** Try to open all the appropriate LogFiles managed by this LogFileManager
	 *  and write a header statement for each that is opened. */
	public void startLogs() {
		if (fullLoggingIsOn && !fullLog.isOpen) {
			fullLog.open();
			writeHeader(fullLog, "CHROME MONITOR LOG");
		}
		if (errorLoggingIsOn && !errorLog.isOpen) {
			errorLog.open();
			writeHeader(errorLog, "ERROR LOG");
		}
		if (nativeLoggingIsOn && !nativeLog.isOpen()) {
			nativeLog.open();
			writeHeader(nativeLog, "NATIVE MESSAGE LOG");
		}
		if (serverLoggingIsOn && !serverLog.isOpen()) {
			serverLog.open();
			writeHeader(serverLog, "SERVER MESSAGE LOG");
		}
		if (userLoggingIsOn && !userLog.isOpen()) {
			userLog.open();
			writeHeader(userLog, "CHROME MONITOR LOG");
			log(userLoggingIsOn, userLog, "");
			log(userLoggingIsOn, userLog, " ------------------------------------------------    ------------------------------------------------ ");
			log(userLoggingIsOn, userLog, "                      CHROME                                              SYSTEM                      ");
			log(userLoggingIsOn, userLog, " ------------------------------------------------    ------------------------------------------------ ");
			log(userLoggingIsOn, userLog, String.format("  %10s  %10s  %10s  %10s      %10s  %10s  %10s  %10s  ", "CPU", "CPU", "GPU", "Memory", "CPU", "CPU", "GPU", "Memory"));
			log(userLoggingIsOn, userLog, String.format("  %10s  %10s  %10s  %10s      %10s  %10s  %10s  %10s  ", "Power", "Usage", "Usage", "Usage", "Power", "Usage", "Usage", "Usage"));
		}
	}
	
	/** Write a formatted header displaying the headerText to the specified 
	 *  LogFile. */
	private void writeHeader(LogFile log, String headerText) {
		if (log != null) {
			SimpleDateFormat fwDateFormat = new SimpleDateFormat("MM/dd/YYYY");
	        SimpleDateFormat fwTimeFormat = new SimpleDateFormat("hh:mma z");
			String title = String.format(
					" %s   (%s  %s)", 
					headerText, fwDateFormat.format(this.date), fwTimeFormat.format(this.date));
	        
			log.write("================================================");
			log.write(title);
			log.write("================================================");
		}
	}
	
	/** Log a message to a log file by specifying the type 
	 *  (e.g. DEFAULT, ERROR, NATIVE, SERVER, or USER). */
	public void log(int type, String message) {
		String fullMessage = message;
		switch (type) {
			case ERROR: 
				log(errorLoggingIsOn, errorLog, message);
				break;
			case NATIVE: 
				fullMessage = " NATIVE " + message;
				log(nativeLoggingIsOn, nativeLog, fullMessage);
				break;
			case SERVER: 
				fullMessage = " SERVER " + message;
				log(serverLoggingIsOn, serverLog, fullMessage);
				break;
			case USER:
				log(userLoggingIsOn, userLog, fullMessage);
				break;
			default:
				break;
		}
		if (type != USER) {
			log(fullLoggingIsOn, fullLog, fullMessage);
		}
	}
	
	/** Helper method which includes the right checks to perform before trying 
	 *  to write to a log file. */
	private void log(boolean logTypeIsOn, LogFile logType, String message) {
		if (logTypeIsOn && logType != null) {
			logType.write(message);
		}
	}
	
	/** Try to close all of the LogFiles managed by this LogFileManager. 
	 *  Return true if successful; false otherwise. */
	public boolean closeLogs() {
		boolean successful = true;
		Date endDate = Calendar.getInstance().getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mma z");
		String closeMessage = String.format("Logging ended %s %s\n", dateFormat.format(endDate), timeFormat.format(endDate));
		if (fullLog != null && fullLog.isOpen()) {
			fullLog.write("");
			fullLog.write(closeMessage);
			successful = fullLog.close() && successful;
		}
		if (errorLog != null && errorLog.isOpen()) { 
			errorLog.write("");
			errorLog.write(closeMessage);
			successful = errorLog.close() && successful;
		}
		if (nativeLog != null && nativeLog.isOpen()) { 
			nativeLog.write("");
			nativeLog.write(closeMessage);
			successful = nativeLog.close() && successful;
		}
		if (serverLog != null && serverLog.isOpen()) { 
			serverLog.write("");
			serverLog.write(closeMessage);
			successful = serverLog.close() && successful;
		}
		if (userLog != null && userLog.isOpen()) { 
			userLog.write("");
			userLog.write(closeMessage);
			successful = userLog.close() && successful;
		}
		return successful;
	}
	
	
	
	/** 
	 *  LogFile
	 *
	 *  VERSION: 2021.05.03
	 *  AUTHORS: Rae Bouldin
	 *  
	 *  DESCRIPTION:
	 *    Represents one Log File which can be managed in the LogFileManager.
	 * 
	 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
	 */
	private class LogFile {
		
		private FileWriter logFile;
		private String fileName;
		private boolean isOpen;
		
		public LogFile(String fileName) {
			this.logFile = null;
			this.fileName = fileName;
			this.isOpen = false;
		}
		
		public boolean isOpen() {
			return this.isOpen;
		}
		
		/** Try to open the file for this LogFile. Return true if successful 
		 *  or if the file was already open; false otherwise. */
		public boolean open() {
			if ( !this.isOpen ) {
				try {
					logFile = new FileWriter(fileName);
					isOpen = true;
				} catch (IOException e) {
					System.err.println(e.toString());
				}
			}
			return isOpen;
		}
		
		/** Try to write to the file for this LogFile. Return true if 
		 *  successful; false otherwise. */
		public boolean write(String line) {
			if ( this.isOpen ) {
				try {
					logFile.write(line + "\r\n");
					return true;
				} catch (IOException e) {
					System.err.println(e.toString());
					return false;
				}
			} 
			else {
				return false;
			}
		}
		
		/** Try to close the file for this LogFile. Return true if successful 
		 *  or if the file was already closed; false otherwise. */
		public boolean close() {
			if ( this.isOpen ) {
				try {
					logFile.close();
					isOpen = false;
				} catch (IOException e) {
					System.err.println(e.toString());
				}
			}
			return !isOpen;
		}
		
	}

	
	
}
