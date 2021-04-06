/** 
 *  LogFileHandler.java
 *
 *  VERSION: 2021.04.05
 *  AUTHORS: Rae Bouldin, Zinan Guo
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LogFileHandler {
	
	public final int ERROR = 1;
	public final int NATIVE = 2;
	public final int SERVER = 3;
	
	private Date date;
	private String sessionID;
	
	private final String FILE_PREFIX = "LOG";
	private final String ERROR_FILENAME  = "Errors";
	private final String NATIVE_FILENAME = "NativeMessaging";
	private final String SERVER_FILENAME = "Server";
	
	private boolean fullLoggingIsOn;
	private boolean errorLoggingIsOn;
	private boolean nativeLoggingIsOn;
	private boolean serverLoggingIsOn;
	
	private FileWriter fullLog;
	private FileWriter errorLog;
	private FileWriter nativeLog;
	private FileWriter serverLog;
	
	public LogFileHandler(Date date, String sessionID) throws IOException {
		this();
		this.date = date;
		this.sessionID = sessionID.replaceAll("\"", "");
	}
	
	public LogFileHandler() throws IOException {
		
		this.date = Calendar.getInstance().getTime();
		this.sessionID = null;
		
		this.fullLoggingIsOn = false;
		this.errorLoggingIsOn = false;
		this.nativeLoggingIsOn = false;
		this.serverLoggingIsOn = false;
		
		this.fullLog = null;
		this.errorLog = null;
		this.nativeLog = null;
		this.serverLog = null;
		
	}
	
	public void setFullLogging(boolean on_off) {
		this.fullLoggingIsOn = on_off;
	}
	
	public void setErrorLogging(boolean on_off) {
		this.errorLoggingIsOn = on_off;
	}
	
	public void setNativeLogging(boolean on_off) {
		this.nativeLoggingIsOn = on_off;
	}
	
	public void setServerLogging(boolean on_off) {
		this.serverLoggingIsOn = on_off;
	}
	
	public void startLogs() {
		if (fullLoggingIsOn) { 
			String fileName = FILE_PREFIX + "-" + sessionID + ".txt";
			fullLog = startLog(fileName, "CHROME MONITOR LOG");
		}
		if (errorLoggingIsOn) { 
			errorLog = startLog(formatFileName(ERROR_FILENAME), "ERROR LOG");
		}
		if (nativeLoggingIsOn) { 
			nativeLog = startLog(formatFileName(NATIVE_FILENAME), "NATIVE MESSAGE LOG");
		}
		if (serverLoggingIsOn) { 
			serverLog = startLog(formatFileName(SERVER_FILENAME), "SERVER MESSAGE LOG");
		}
	}
	
	public void log(int type, String message) {
		switch (type) {
			case ERROR: logError(message);
				break;
			case NATIVE: logNativeMessage(message);
				break;
			case SERVER: logServerMessage(message);
				break;
			default:
				break;
		}
	}
	
	public void logError(String message) {
		if (fullLoggingIsOn) { 
			writeToLog(fullLog, message); 
		}
		if (errorLoggingIsOn) { 
			writeToLog(errorLog, message); 
		}
	}
	
	public void logNativeMessage(String message) {
		if (fullLoggingIsOn) { 
			writeToLog(fullLog, "NATIVE " + message); 
		}
		if (nativeLoggingIsOn) { 
			writeToLog(nativeLog, message); 
		}
	}
	
	public void logServerMessage(String message) {
		if (fullLoggingIsOn) { 
			writeToLog(fullLog, "SERVER " + message); 
		}
		if (serverLoggingIsOn) { 
			writeToLog(serverLog, message); 
		}
	}
	
	public boolean closeLogs() {
		boolean successful = true;
		Date endDate = Calendar.getInstance().getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:KKa z");
		String closeMessage = String.format("Logging ended %s %s\n", dateFormat.format(endDate), timeFormat.format(endDate));
		if (fullLoggingIsOn) {
			writeToLog(fullLog, "");
			writeToLog(fullLog, closeMessage);
			successful = closeLog(fullLog) && successful;
		}
		if (errorLoggingIsOn) { 
			writeToLog(errorLog, "");
			writeToLog(errorLog, closeMessage);
			successful = closeLog(errorLog) && successful;
		}
		if (nativeLoggingIsOn) { 
			writeToLog(nativeLog, "");
			writeToLog(nativeLog, closeMessage);
			successful = closeLog(nativeLog) && successful;
		}
		if (serverLoggingIsOn) { 
			writeToLog(serverLog, "");
			writeToLog(serverLog, closeMessage);
			successful = closeLog(serverLog) && successful;
		}
		return successful;
	}
	
	private String formatFileName(String logName) {
		return FILE_PREFIX + "-" + sessionID + "-" + logName + ".txt";
	}
	
	private FileWriter startLog(String fileName, String logTitle) {
		SimpleDateFormat fwDateFormat = new SimpleDateFormat("MM/dd/YYYY");
        SimpleDateFormat fwTimeFormat = new SimpleDateFormat("hh:KKa z");
		String title = String.format(
				" %s   (%s  %s)\n", 
				logTitle, fwDateFormat.format(date), fwTimeFormat.format(date));
        
		try {
			FileWriter fw = new FileWriter(fileName);
			fw.write("================================================\n");
			fw.write(title);
			fw.write("================================================\n");
			return fw;
		} catch (IOException e) {
			System.err.println(e.toString());
			return null;
		}
		
	}
	
	private boolean writeToLog(FileWriter fw, String message) {
		try {
			fw.write(message + "\r\n");
			return true;
		} catch (IOException e) {
			System.err.println(e.toString());
			return false;
		}
	}
	
	private boolean closeLog(FileWriter fw) {
		try {
			fw.close();
			return true;
		} catch (IOException e) {
			System.err.println(e.toString());
			return false;
		}
	}

}
