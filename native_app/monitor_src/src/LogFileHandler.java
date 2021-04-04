/** 
 *  LogFileHandler.java
 *
 *  VERSION: 2021.04.04
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
	
	private final String NATIVE_FILENAME = "NativeMessageLog";
	private FileWriter nativeLog;
	
	private final String SERVER_FILENAME = "ServerMessageLog";
	private FileWriter serverLog;
	
	public LogFileHandler() throws IOException {
		nativeLog = null;
		serverLog = null;
	}
	
	public void startNativeLog() {
		nativeLog = startLog(NATIVE_FILENAME, "NATIVE MESSAGE LOG");
	}
	
	public void startServerLog() {
		serverLog = startLog(SERVER_FILENAME, "SERVER MESSAGE LOG");
	}
	
	public boolean logNativeMessage(String message) {
		return writeToLog(nativeLog, message);
	}
	
	public boolean logServerMessage(String message) {
		return writeToLog(serverLog, message);
	}
	
	public boolean closeNativeLog() {
		return closeLog(nativeLog);
	}
	
	public boolean closeServerLog() {
		return closeLog(serverLog);
	}
	
	private FileWriter startLog(String fileName, String logTitle) {
		
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat fileDateFormat = new SimpleDateFormat("-YYYY-MM-dd-HHmmss");
		SimpleDateFormat fwDateFormat = new SimpleDateFormat("dd/MM/YYYY");
        SimpleDateFormat fwTimeFormat = new SimpleDateFormat("HH:KKa");
        
		String fullFileName = fileName + fileDateFormat.format(date) + ".txt";
		String title = String.format(" %s  (%s  %s)\n", logTitle, fwTimeFormat.format(date), fwDateFormat.format(date));
        
		try {
			FileWriter fw = new FileWriter(fullFileName);
			fw.write("===========================================\n");
			fw.write(title);
			fw.write("===========================================\n");
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
