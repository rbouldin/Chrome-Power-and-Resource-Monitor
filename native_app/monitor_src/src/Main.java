/** 
 *  Main.java
 *
 *  VERSION: 2021.05.03
 *  AUTHORS: Rae Bouldin, Zinan Guo
 *  
 *  DESCRIPTION:
 *    Launch a monitoring instance using standard input and output (i.e. native
 *    messaging).
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

import java.io.IOException;

public class Main {
	
	/** -----------------------------------------------------------------------
	 *                          Native Message Commands                        
	 *  -----------------------------------------------------------------------
	 *  String commands that can be sent through Native Messaging to prompt the
	 *  Native Application to take a certain action and/or send some requested 
	 *  data back through Native Messaging. These commands should be sent as a 
	 *  JSON string that has been converted to byte representation. Before 
	 *  being converted to byte representation, The JSON String should have the 
	 *  following format to indicate that it should be read as a Native Message 
	 *  Command: {"message":"Native_Message_Command"}. The String may contain 
	 *  additional data as needed. Excess/unknown data will be ignored as long 
	 *  as the command is recognized. The list recognized commands are as seen 
	 *  in the following constants: 
	 *  ----------------------------------------------------------------------- */
	private static final String POST_RUN_OPTIONS = "POST RunOptions";
	private static final String POST_USER_DATA = "POST user";
	
	private static final String GET_MONITOR_DATA = "GET MonitorRecord";
	private static final String GET_SUGGESTIONS = "GET suggestions";
	private static final String GET_SYSTEM_INFO = "GET sysInfo";
	
	private static final String GENERATE_USER_LOG = "LOG";
	
	private static final String EXIT_NATIVE = "EXIT NATIVE";
	
	/** -----------------------------------------------------------------------
	 *                             Program Variables                           
	 *  ----------------------------------------------------------------------- */
	/** Holds the options enabled for the program run (e.g. enable/disable 
	 *  native messaging, server messaging) */
	private static RunOptions options;
	/** The current monitoring session. */
	private static Monitor monitor;
	/** Holds information on the current monitoring session. */
	private static MonitorLog log;
	/** Logs messages related to this monitoring session. Log files will be 
	 *  created and populated only if the correct run options are enabled. */
	private static LogFileManager fileManager;

	public static void main(String[] args) throws IOException, InterruptedException {
		
		/* -----------------------------------------------------------------
		 *                           INITIAL SETUP                          
		 * ----------------------------------------------------------------- */
		// Initialize variables related for this monitoring session.
		options = new RunOptions(args); 
		monitor = new Monitor();
		log = new MonitorLog();
		fileManager = new LogFileManager(log.getSessionDate(), log.getSessionID());
		
		// Check the file logging options enabled, and start the applicable logs.
		if (options.LOGGING_ENABLED) { fileManager.turnOnFullLogging(); }
		if (options.ERROR_LOGGING_ENABLED) { fileManager.turnOnErrorLogging(); }
		if (options.NATIVE_LOGGING_ENABLED) { fileManager.turnOnNativeLogging(); }
		if (options.SERVER_LOGGING_ENABLED) { fileManager.turnOnServerLogging(); }
		if (options.USER_LOGGING_ENABLED) { fileManager.turnOnUserLogging(); }
		fileManager.startLogs();
		
		// Write the run options to the default file log if logging is enabled for it.
		if (options.LOGGING_ENABLED) {
			logRunOptions();
			fileManager.log(fileManager.DEFAULT, "------------------------------------------------");
		}
		
		/* -----------------------------------------------------------------
		 *                         START MONITORING                         
		 * ----------------------------------------------------------------- */
		// If Native Message input is ENABLED, the program will accept messages related to 
		// monitoring from Standard Input until the EXIT_NATIVE message is read.
		if (options.NATIVE_INPUT_ENABLED) {
			
			// The first message received from Standard Input should be "connected" to establish 
			// that messaging is working as expected.
			String nativeMessage = "";
			String message = "";
			
			// Read from Standard Input until the EXIT_NATIVE message is received.
			while ( !EXIT_NATIVE.equalsIgnoreCase(message) ) {
				
				// Read Native Message from Standard Input
				nativeMessage = NativeMessage.read(System.in);
				message = NativeMessage.getJSONValue(nativeMessage, "message");
					fileManager.log(fileManager.NATIVE, " ==>  " + nativeMessage);
				
				// Parse nativeMessage for the actual content sent.
				if (message == null) {
					if (options.NATIVE_OUTPUT_ENABLED) {
						String nativeErr = "{\"message\":\"error: unrecognized message format " + nativeMessage + "\"}";
						NativeMessage.send(nativeErr);
							fileManager.log(fileManager.NATIVE, " <==  " + nativeErr);
					}
					System.err.println("Unexpected message received by Standard Input '" + nativeMessage + "'.");
					fileManager.log(fileManager.ERROR, "Unexpected message received by Standard Input '" + nativeMessage + "'.");
					break;
				} 
				else if ( GET_MONITOR_DATA.equalsIgnoreCase(message) ) {
					getMonitorData();
				}
				else if ( GET_SUGGESTIONS.equalsIgnoreCase(message) ) {
					getSuggestionsFromServer();
				}
				else if ( GET_SYSTEM_INFO.equalsIgnoreCase(message) ) {
					SystemInformation info = monitor.getSysinfo();
					String infoAsJSON = info.toJSON();
					// Send Native Message to Standard Output
					if (options.NATIVE_OUTPUT_ENABLED) {
						NativeMessage.send(infoAsJSON);
						fileManager.log(fileManager.NATIVE, " <==  " + infoAsJSON);
					}
				}
				else if ( POST_RUN_OPTIONS.equalsIgnoreCase(message) ) {
					changeRunOptions(nativeMessage);
				}
				else if ( POST_USER_DATA.equalsIgnoreCase(message) ) {
					parseUserData(nativeMessage);
				}
				else if ( GENERATE_USER_LOG.equalsIgnoreCase(message) && !options.USER_LOGGING_ENABLED ) {
					options.USER_LOGGING_ENABLED = true;
					fileManager.turnOnUserLogging();
					fileManager.startLogs();
				}
				else if ( message.equalsIgnoreCase("connected") ) {
					fileManager.log(fileManager.DEFAULT, "Connection Established");
				}
				else if ( EXIT_NATIVE.equalsIgnoreCase(message) ) {
					fileManager.log(fileManager.DEFAULT, "Detected Signal EXIT NATIVE");
					
				}
				else {
					if (options.NATIVE_OUTPUT_ENABLED) {
						String nativeErr = "{\"message\":\"error: unrecognized command " + message + "\"}";
						NativeMessage.send(nativeErr);
							fileManager.log(fileManager.NATIVE, " <==  " + nativeErr);
					}
					System.err.println("Unexpected message received by Standard Input '" + nativeMessage + "'.");
					fileManager.log(fileManager.ERROR, "Unexpected message received by Standard Input '" + nativeMessage + "'.");
				}
				
			}
			
		} 
//		// If Native Message input is DISABLED, the program will run monitoring until a specified 
//		// number of records are logged.
		else {
			String data = "{\"message\":\"POST user\",\"user_id\":\"test\",\"suggestions\":[],\"tabs\":\"1\"}";
			parseUserData(data);
			for (int r = 0; r < options.NUM_RECORDS; r++) {
				getMonitorData();
			}
			getSuggestionsFromServer();
		}
		
		// Close the log file FileWriters before exiting.
		fileManager.closeLogs();

	}
	
	
	
	/** -----------------------------------------------------------------------
	 *                              Helper Methods                             
	 *  ----------------------------------------------------------------------- */
	
	
	/**
	 *  Uses a consistent format to print the run options that are currently 
	 *  enabled to the default file log if logging is enabled for it.
	 */
	private static void logRunOptions() {
		
		// Log whether Native Messaging Input/Output is enabled.
		String nativeEnabled = 
				String.valueOf(options.NATIVE_INPUT_ENABLED || options.NATIVE_OUTPUT_ENABLED).toUpperCase();
		fileManager.log(fileManager.DEFAULT, " NATIVE MESSAGING   " + nativeEnabled);
		if (!options.NATIVE_INPUT_ENABLED || !options.NATIVE_OUTPUT_ENABLED) {
			fileManager.log(fileManager.DEFAULT, "  > native input    " + options.NATIVE_INPUT_ENABLED);
			fileManager.log(fileManager.DEFAULT, "  > native output   " + options.NATIVE_OUTPUT_ENABLED);
		}
		
		// Log whether Server Messaging is enabled.
		String serverEnabled = String.valueOf(options.SERVER_MESSAGING_ENABLED).toUpperCase();
		fileManager.log(fileManager.DEFAULT, " SERVER MESSAGING   " + serverEnabled);
		
		// Log how many records we are recording, if Native Input is not being used.
		if (!options.NATIVE_INPUT_ENABLED) {
			fileManager.log(fileManager.DEFAULT, " RECORDS            " + options.NUM_RECORDS);
		}
		
	}
	
	
	/** Given a JSON formatted string with a POST RunOptions request, parse 
	 *  the string for the new run options and update the RunOptions for this 
	 *  monitoring session. */
	private static void changeRunOptions(String nativeMessage) {
		
		// Set new run options
		String newOptions = NativeMessage.getJSONValue(nativeMessage, "content");
		String[] newArgs = newOptions.split(" ");
		options.parseOptions(newArgs);
		
		// Check the file logging options enabled, and start the applicable logs.
		if (options.LOGGING_ENABLED) { fileManager.turnOnFullLogging(); }
		if (options.ERROR_LOGGING_ENABLED) { fileManager.turnOnErrorLogging(); }
		if (options.NATIVE_LOGGING_ENABLED) { fileManager.turnOnNativeLogging(); }
		if (options.SERVER_LOGGING_ENABLED) { fileManager.turnOnServerLogging(); }
		if (options.USER_LOGGING_ENABLED) { fileManager.turnOnUserLogging(); }
		fileManager.startLogs();
		
		// Write the updated run options to the default file log if logging is enabled for it.
		if (options.LOGGING_ENABLED) {
			fileManager.log(fileManager.DEFAULT, "Detected a change in the Run Options:");
			fileManager.log(fileManager.DEFAULT, newOptions);
			fileManager.log(fileManager.DEFAULT, "------------------------------------------------");
			logRunOptions();
			fileManager.log(fileManager.DEFAULT, "------------------------------------------------");
		}
		
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
			fileManager.log(fileManager.ERROR, "Something went wrong setting the MonitorLog user_ID field.");
		}
		String suggestions = NativeMessage.getJSONValue(nativeMessage, "suggestions");
		if ( !log.setSuggestions(suggestions) ) {
			System.err.println("Something went wrong setting the MonitorLog settings field.");
			fileManager.log(fileManager.ERROR, "Something went wrong setting the MonitorLog suggestions field.");
		}
		String tabs = NativeMessage.getJSONValue(nativeMessage, "tabs");
		if ( !log.setNumTabs(tabs) ) {
			System.err.println("Something went wrong setting the MonitorLog tabs field.");
			fileManager.log(fileManager.ERROR, "Something went wrong setting the MonitorLog tabs field.");
		}
		
	}
	
	
	/** Get a new point of data for this monitoring session and update the 
	 *  current log to include that point of data. Send the data point to 
	 *  Native Output, if it is enabled. */
	private static void getMonitorData() throws IOException {
		
		// Get data from the Monitor as a new MonitorRecord object and convert 
		// the record to JSON format.
		MonitorRecord newRecord = monitor.getData();
		
		// Append record data to the MonitorLog.
		log.appendRecord(newRecord);
		
		// Send Native Message to Standard Output
		if (options.NATIVE_OUTPUT_ENABLED) {
			String recordAsJSON = newRecord.toJSON();
			NativeMessage.send(recordAsJSON);
				fileManager.log(fileManager.NATIVE, " <==  " + recordAsJSON);
		}
		
		if (options.USER_LOGGING_ENABLED) {
			
			String logData = String.format("  %10s  %10s  %10s  %10s      %10s  %10s  %10s  %10s  ", 
					formatUserLogData(newRecord.getChromeCpuPower()),
					formatUserLogData(newRecord.getChromeCpuUsage()),
					formatUserLogData(newRecord.getChromeGpuUsage()),
					formatUserLogData(newRecord.getChromeMemUsage()),
					formatUserLogData(newRecord.getSystemCpuPower()),
					formatUserLogData(newRecord.getSystemCpuUsage()),
					formatUserLogData(newRecord.getSystemGpuUsage()),
					formatUserLogData(newRecord.getSystemMemUsage())
					);
			
			fileManager.log(fileManager.USER, logData);
			
		}
		
	}
	
	private static String formatUserLogData(double value) {
		if ( !Double.isNaN(value) ) {
			return String.format("%.4f", value);
		}
		return "       ";
		
	}
	
	
	/** POST the current log for this monitoring session to the server. 
	 *  If the POST is successful, then GET suggestions from the server. 
	 *  Then send the suggestions to Native Output, if it is enabled. */
	private static void getSuggestionsFromServer() throws IOException {
		
		// Set a default message in case something goes wrong trying to communicate with the server
		// (e.g. connection refused, or unexpected message received).
		String suggestions = "{\"suggestions\":[],\"suggestion_msg\":\"No suggestions could be generated. Something went wrong communicating with the server.\"}";
		
		if (options.SERVER_MESSAGING_ENABLED) {
			
			// Connect to the server.
			// Server: IP = "52.91.154.176", Port = "8000"
			String serverGetURL = "http://52.91.154.176:8000/suggestions/";
			String serverPostURL = "http://52.91.154.176:8000/data/";
			ServerHandler server = new ServerHandler(serverGetURL, serverPostURL);
			
			// Post session log to the server.
			String sessionLog = log.toJSON();
			String serverResponse = server.postJSON(sessionLog);
				fileManager.log(fileManager.SERVER, " <==  POST " + sessionLog);
				fileManager.log(fileManager.SERVER, " ==>  " + serverResponse);
			
			// If the server responded with "OK", the session log was processed successfully.
			if (serverResponse.contains("\"Status\":\"OK")) {
				
				// Get suggestions from the server based on the current session.
				String suggestionRequest = "{\"user\":" + log.getUserID() + ",\"batch\":" + log.getBatch() + "}";
				String suggestionResponse = server.get(suggestionRequest);
					fileManager.log(fileManager.SERVER, " <==  GET " + suggestionRequest);
					fileManager.log(fileManager.SERVER, " ==>  " + suggestionResponse);
					
				// If the server response is a JSON containing "suggestion_msg" field, the get request was successful.
				if (suggestionResponse.contains("\"suggestion_msg\"")) {
					suggestions = suggestionResponse;
				}
				
			} 
			else {
				
				System.err.println();
				System.err.println(serverResponse);
				
			}
		}
		else {
			suggestions = "{\"suggestions\":[],\"suggestion_msg\":\"No suggestions could be generated. Server communication is disabled.\"}";
		}
		
		// Send the suggestions as a Native Message if Native Output is enabled.
		if (options.NATIVE_OUTPUT_ENABLED) {
			NativeMessage.send(suggestions);
				fileManager.log(fileManager.NATIVE, " <==  " + suggestions);
		}
		
		if (options.USER_LOGGING_ENABLED) {
			String suggestionMsg = NativeMessage.getJSONValue(suggestions, "suggestion_msg");
			fileManager.log(fileManager.USER, " ------------------------------------------------    ------------------------------------------------ ");
			fileManager.log(fileManager.USER, "");
			fileManager.log(fileManager.USER, "SUGGESTION");
			fileManager.log(fileManager.USER, suggestionMsg);
		}
		
	}

	
}