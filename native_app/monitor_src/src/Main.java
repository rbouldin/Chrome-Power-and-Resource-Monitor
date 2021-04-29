/** 
 *  Main.java
 *
 *  VERSION: 2021.04.29
 *  AUTHORS: Rae Bouldin, Zinan Guo
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

import java.io.IOException;

public class Main {
	
	/** Native message commands */
	private static final String POST_USER_DATA = "POST user";
	private static final String GET_MONITOR_DATA = "GET MonitorRecord";
	private static final String GET_SUGGESTIONS = "GET suggestions";
	private static final String STOP_MONITORING = "STOP monitoring";
	private static final String EXIT_NATIVE = "EXIT NATIVE";
	
	private static final String GET_SYSTEM_INFO = "GET sysInfo";
	
	/** Holds the options enabled for the program run (e.g. enable/disable 
	 *  native messaging, server messaging) */
	private static RunOptions options;
	/** The current monitoring session. */
	private static Monitor monitor;
	/** Holds information on the current monitoring session. */
	private static MonitorLog log;
	/** Logs messages related to this monitoring session. Log files will be 
	 *  created and populated only if the correct run options are enabled. */
	private static LogFileHandler fileLogger;

	public static void main(String[] args) throws IOException, InterruptedException {
		
		/* -----------------------------------------------------------------
		 *                           INITIAL SETUP                          
		 * ----------------------------------------------------------------- */
		// Initialize variables related for this monitoring session.
		options = new RunOptions(args); 
		monitor = new Monitor();
		log = new MonitorLog();
		fileLogger = new LogFileHandler(log.getSessionDate(), log.getSessionID());
		
		// Check the file logging options enabled, and start the applicable logs.
		if (options.LOGGING_ENABLED) { fileLogger.setFullLogging(true); }
		if (options.ERROR_LOGGING_ENABLED) { fileLogger.setErrorLogging(true); }
		if (options.NATIVE_LOGGING_ENABLED) { fileLogger.setNativeLogging(true); }
		if (options.SERVER_LOGGING_ENABLED) { fileLogger.setServerLogging(true); }
		fileLogger.startLogs();
		
		// Write the run options to the default file log if logging is enabled for it.
		if (options.LOGGING_ENABLED) {
			String nativeEnabled = String.valueOf(options.NATIVE_INPUT_ENABLED || options.NATIVE_OUTPUT_ENABLED).toUpperCase();
			fileLogger.log(fileLogger.DEFAULT, " NATIVE MESSAGING   " + nativeEnabled);
			if (!options.NATIVE_INPUT_ENABLED || !options.NATIVE_OUTPUT_ENABLED) {
				fileLogger.log(fileLogger.DEFAULT, "  > native input    " + options.NATIVE_INPUT_ENABLED);
				fileLogger.log(fileLogger.DEFAULT, "  > native output   " + options.NATIVE_OUTPUT_ENABLED);
			}
			String serverEnabled = String.valueOf(options.SERVER_MESSAGING_ENABLED).toUpperCase();
			fileLogger.log(fileLogger.DEFAULT, " SERVER MESSAGING   " + serverEnabled);
			if (!options.NATIVE_INPUT_ENABLED) {
				fileLogger.log(fileLogger.DEFAULT, " RECORDS            " + options.NUM_RECORDS);
			}
			fileLogger.log(fileLogger.DEFAULT, "------------------------------------------------");
		}
		
		/* -----------------------------------------------------------------
		 *                         START MONITORING                         
		 * ----------------------------------------------------------------- */
		// If Native Message input is ENABLED, the program will accept messages related to 
		// monitoring from Standard Input until the STOP_MONITORING message is read.
		if (options.NATIVE_INPUT_ENABLED) {
			
			// The first message received from Standard Input should be "connected" to establish 
			// that messaging is working as expected.
			String nativeMessage = "";
			String message = "";
			
			// Read from Standard Input until the STOP_MONITORING message is received.
			while ( !EXIT_NATIVE.equalsIgnoreCase(message) && 
					!STOP_MONITORING.equalsIgnoreCase(message) ) {
				
				// Read Native Message from Standard Input
				nativeMessage = NativeMessage.read(System.in);
				message = NativeMessage.getJSONValue(nativeMessage, "message");
					fileLogger.logNativeMessage("Received: " + nativeMessage);
				
				// Parse nativeMessage for the actual content sent.
				if (message == null) {
					System.err.println("Unexpected message received by Standard Input '" + nativeMessage + "'.");
					fileLogger.logError("Unexpected message received by Standard Input '" + nativeMessage + "'.");
					break;
				} 
				else if ( GET_MONITOR_DATA.equalsIgnoreCase(message) ) {
					getMonitorData();
				}
				else if ( GET_SUGGESTIONS.equalsIgnoreCase(message) ) {
					getSuggestionsFromServer();
				}
				else if (GET_SYSTEM_INFO.equalsIgnoreCase(message)) {
					SystemInformation info = monitor.getSysinfo();
					String infoAsJSON = info.toJSON();
					// Send Native Message to Standard Output
					if (options.NATIVE_OUTPUT_ENABLED) {
						NativeMessage.send(infoAsJSON);
						fileLogger.logNativeMessage("Sent: " + infoAsJSON);
					}
				}
				else if ( POST_USER_DATA.equalsIgnoreCase(message) ) {
					parseUserData(nativeMessage);
				}
				else if ( message.equalsIgnoreCase("connected") ) {
					fileLogger.log(fileLogger.DEFAULT, "Connection Established");
				}
				else if ( STOP_MONITORING.equalsIgnoreCase(message) ) {
					fileLogger.log(fileLogger.DEFAULT, "Detected Signal STOP MONITORING");
				}
				else if ( EXIT_NATIVE.equalsIgnoreCase(message) ) {
					fileLogger.log(fileLogger.DEFAULT, "Detected Signal EXIT NATIVE");
					
				}
				else {
					System.err.println("Unexpected message received by Standard Input '" + nativeMessage + "'.");
					fileLogger.logError("Unexpected message received by Standard Input '" + nativeMessage + "'.");
				}
				
			}
			
		} 
		// If Native Message input is DISABLED, the program will run monitoring until a specified 
		// number of records are logged.
		else {
			for (int r = 0; r < options.NUM_RECORDS; r++) {
				getMonitorData();
			}
			getSuggestionsFromServer();
		}
		
		// Close the log file FileWriters before exiting.
		fileLogger.closeLogs();

	}
	
	/** Given a JSON formatted string with a POST user request, parse the 
	 *  string for user data and update the log for the current monitoring 
	 *  session to include it. */
	private static void parseUserData(String nativeMessage) {
		
		// Populate the MonitorLog with the user info read from Standard Input.
		// {"message":"POST user", "user_id":"", "suggestions":[], "tabs":"1"}
		String userID = NativeMessage.getJSONValue(nativeMessage, "user_id");
		if ( !log.setUserID(userID) ) {
			System.err.println("Something went wrong setting the MonitorLog user_ID field.");
			fileLogger.logError("Something went wrong setting the MonitorLog user_ID field.");
		}
		String suggestions = NativeMessage.getJSONValue(nativeMessage, "suggestions");
		if ( !log.setSuggestions(suggestions) ) {
			System.err.println("Something went wrong setting the MonitorLog settings field.");
			fileLogger.logError("Something went wrong setting the MonitorLog suggestions field.");
		}
		String tabs = NativeMessage.getJSONValue(nativeMessage, "tabs");
		if ( !log.setNumTabs(tabs) ) {
			System.err.println("Something went wrong setting the MonitorLog tabs field.");
			fileLogger.logError("Something went wrong setting the MonitorLog tabs field.");
		}
		
	}
	
	/** Get a new point of data for this monitoring session and update the 
	 *  current log to include that point of data. Send the data point to 
	 *  Native Output, if it is enabled. */
	private static void getMonitorData() throws IOException {
		
		// Get data from the Monitor as a new MonitorRecord object 
		// and convert the record to JSON format.
		MonitorRecord newRecord = monitor.getData();
		// Append record data to log file.
		log.appendRecord(newRecord);
		
		// Send Native Message to Standard Output
		if (options.NATIVE_OUTPUT_ENABLED) {
			String recordAsJSON = newRecord.toJSON();
			NativeMessage.send(recordAsJSON);
				fileLogger.logNativeMessage("Sent: " + recordAsJSON);
		}
		
	}
	
	/** POST the current log for this monitoring session to the server. 
	 *  If the POST is successful, then GET suggestions from the server. 
	 *  Then send the suggestions to Native Output, if it is enabled. */
	private static void getSuggestionsFromServer() throws IOException {
		
		// Send session log to the server.
		// Server: IP = "52.91.154.176", Port = "8000"
		String suggestions = "[]";
		if (options.SERVER_MESSAGING_ENABLED) {
			String serverGetURL = "http://52.91.154.176:8000/suggestion/";
			String serverPostURL = "http://52.91.154.176:8000/data/";
			ServerHandler server = new ServerHandler(serverGetURL, serverPostURL);
			String sessionLog = log.toJSON();
			String serverResponse = server.postJSON(sessionLog);
				fileLogger.logServerMessage("Sent: POST " + sessionLog);
				fileLogger.logServerMessage("Received: " + serverResponse);
			if (serverResponse.equals("OK")) {
				String suggestionRequest = "{\"user\":" + log.getUserID() + ",\"batch\":\"" + log.getBatch() + "\"}";
				suggestions = server.get(suggestionRequest);
					fileLogger.logServerMessage("Sent: GET " + "suggestions");
					fileLogger.logServerMessage("Received: " + suggestions);
			} else {
				System.err.println();
				System.err.println(serverResponse);
			}
		}
		if (options.NATIVE_OUTPUT_ENABLED) {
			NativeMessage.send(suggestions);
				fileLogger.logNativeMessage("Sent: " + suggestions);
		}
		
	}

}
