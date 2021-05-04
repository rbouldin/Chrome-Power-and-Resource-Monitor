/** 
 *  LogFileHandler.java
 *
 *  VERSION: 2021.05.03
 *  AUTHORS: Rae Bouldin
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

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
//	public final int ALL = 4;
	
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
		
	}
	
	public void turnOnFullLogging() {
		this.fullLoggingIsOn = true;
		if (fullLog == null) {
			fullLog = new LogFile("LOG-" + sessionID + ".txt");
		}
	}
	
	public void turnOnErrorLogging() {
		this.errorLoggingIsOn = true;
		if (errorLog == null) {
			errorLog = new LogFile("LOG-" + sessionID + "-Errors.txt");
		}
	}
	
	public void turnOnNativeLogging() {
		this.nativeLoggingIsOn = true;
		if (nativeLog == null) {
			nativeLog = new LogFile("LOG-" + sessionID + "-Native.txt");
		}
	}
	
	public void turnOnServerLogging() {
		this.serverLoggingIsOn = true;
		if (serverLog == null) {
			serverLog = new LogFile("LOG-" + sessionID + "-Server.txt");
		}
	}
	
	public void turnOnUserLogging() {
		this.userLoggingIsOn = true;
		if (userLog == null) {
			userLog = new LogFile("CHROME_MONITOR_LOG-" + sessionID + ".txt");
		}
	}
	
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
	
	private void log(boolean logTypeIsOn, LogFile logType, String message) {
		if (logTypeIsOn && logType != null) {
			logType.write(message);
		}
	}
	
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
	 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
	 */
	private class LogFile {
		
		private FileWriter logFile;
		private String fileName;
		private boolean isOpen;
		private boolean writingIsEnabled;
		
		public LogFile(String fileName) {
			this();
			this.fileName = fileName;
		}
		
		
		public LogFile() {
			this.logFile = null;
			this.fileName = null;
			this.isOpen = false;
			this.writingIsEnabled = false;
		}
		
		public boolean isOpen() {
			return this.isOpen;
		}
		
		public boolean writingIsEnabled() {
			return writingIsEnabled;
		}
		
		public void enableWriting() {
			writingIsEnabled = true;
		}
		
		public void disableWriting() {
			writingIsEnabled = false;
		}
		
		
		public boolean open() {
			if ( !this.isOpen ) {
				try {
					logFile = new FileWriter(fileName);
					isOpen = true;
					writingIsEnabled = true;
				} catch (IOException e) {
					System.err.println(e.toString());
				}
			}
			return isOpen;
		}
		
		public boolean write(String line) {
			if ( this.writingIsEnabled && this.isOpen ) {
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
		
		public boolean close() {
			if ( this.isOpen ) {
				try {
					logFile.close();
					isOpen = false;
					writingIsEnabled = false;
				} catch (IOException e) {
					System.err.println(e.toString());
				}
			}
			return !isOpen;
		}
		
	}

	
	
}
