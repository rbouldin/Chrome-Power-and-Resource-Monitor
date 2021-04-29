/** 
 *  Main.java
 *
 *  VERSION: 2021.04.11
 *  AUTHORS: Rae Bouldin, Zinan Guo
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {

		// Native message commands
		final String POST_USER_DATA = "POST user";
		final String GET_MONITOR_DATA = "GET MonitorRecord";
		final String GET_SUGGESTIONS = "GET suggestions";
		final String GET_SYSTEM_INFO = "GET sysInfo";
		final String STOP_MONITORING = "STOP monitoring";

		// Parse which run options are enabled, if any.
		RunOptions options = new RunOptions(args);

		// 
		Monitor monitor = new Monitor();

		// Create a new MonitorLog for this monitoring session.
		MonitorLog log = new MonitorLog();

		// Create a LogFileHandler to log messages related to this monitoring session. 
		// Log files will be created and populated only if the correct run options are enabled.
		LogFileHandler fileLogger = new LogFileHandler(log.getSessionDate(), log.getSessionID());
		if (options.LOGGING_ENABLED) {
			fileLogger.setFullLogging(true);
		}
		if (options.ERROR_LOGGING_ENABLED) {
			fileLogger.setErrorLogging(true);
		}
		if (options.NATIVE_LOGGING_ENABLED) {
			fileLogger.setNativeLogging(true);
		}
		if (options.SERVER_LOGGING_ENABLED) {
			fileLogger.setServerLogging(true);
		}
		fileLogger.startLogs();

		String nativeEnabled = String.valueOf(options.NATIVE_INPUT_ENABLED || options.NATIVE_OUTPUT_ENABLED)
				.toUpperCase();
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

		// If Native Message input is enabled, the program will accept messages related to 
		// monitoring from Standard Input until the STOP_MONITORING message is read.
		if (options.NATIVE_INPUT_ENABLED) {

			// The first message received from Standard Input should be "connected" to establish 
			// that messaging is working as expected.
			String nativeMessage = NativeMessage.read(System.in);
			String message = NativeMessage.getJSONValue(nativeMessage, "message");
			fileLogger.logNativeMessage("Received: " + nativeMessage);
			if (message == null || !message.equalsIgnoreCase("connected")) {
				System.err.println(
						"The first message received by Standard Input '" + nativeMessage + "' was unexpected.");
				fileLogger.logError(
						"The first message received by Standard Input '" + nativeMessage + "' was unexpected.");
			}

			// The next message received from Standard Input should contain information on the user.
			// {"message":"POST user", "user_id":"", "suggestions":[], "tabs":"2"}
			nativeMessage = NativeMessage.read(System.in);
			message = NativeMessage.getJSONValue(nativeMessage, "message");
			fileLogger.logNativeMessage("Received: " + nativeMessage);
			if (message == null || !POST_USER_DATA.equalsIgnoreCase(message)) {
				System.err.println(
						"The second message received by Standard Input '" + nativeMessage + "' was unexpected.");
				fileLogger.logError(
						"The second message received by Standard Input '" + nativeMessage + "' was unexpected.");
			} else {
				// Populate the MonitorLog with the user info read from Standard Input.
				String userID = NativeMessage.getJSONValue(nativeMessage, "user_id");
				if (!log.setUserID(userID)) {
					System.err.println("Something went wrong setting the MonitorLog user_ID field.");
					fileLogger.logError("Something went wrong setting the MonitorLog user_ID field.");
				}
				String suggestions = NativeMessage.getJSONValue(nativeMessage, "suggestions");
				if (!log.setSuggestions(suggestions)) {
					System.err.println("Something went wrong setting the MonitorLog settings field.");
					fileLogger.logError("Something went wrong setting the MonitorLog suggestions field.");
				}
				String tabs = NativeMessage.getJSONValue(nativeMessage, "tabs");
				if (!log.setNumTabs(tabs)) {
					System.err.println("Something went wrong setting the MonitorLog tabs field.");
					fileLogger.logError("Something went wrong setting the MonitorLog tabs field.");
				}
			}

			// Read from Standard Input until the STOP_MONITORING message is received.
			while (!STOP_MONITORING.equalsIgnoreCase(message)) {

				// Read Native Message from Standard Input
				nativeMessage = NativeMessage.read(System.in);
				message = NativeMessage.getJSONValue(nativeMessage, "message");
				fileLogger.logNativeMessage("Received: " + nativeMessage);

				// Parse nativeMessage for the actual content sent.
				if (GET_MONITOR_DATA.equalsIgnoreCase(message)) {
					// Get data from the Monitor as a new MonitorRecord object 
					// and convert the record to JSON format.
					MonitorRecord newRecord = monitor.getData();
					String recordAsJSON = newRecord.toJSON();
					// Append record data to log file.
					log.appendRecord(newRecord, recordAsJSON);

					// Send Native Message to Standard Output
					if (options.NATIVE_OUTPUT_ENABLED) {
						NativeMessage.send(recordAsJSON);
						fileLogger.logNativeMessage("Sent: " + recordAsJSON);
					}
				}

				else if (GET_SYSTEM_INFO.equalsIgnoreCase(message)) {
					SystemInformation info = monitor.getSysinfo();
					String infoAsJSON = info.toJSON();
					// Append record data to log file.
					//log.appendRecord(info, recordAsJSON);

					// Send Native Message to Standard Output
					if (options.NATIVE_OUTPUT_ENABLED) {
						NativeMessage.send(infoAsJSON);
						fileLogger.logNativeMessage("Sent: " + infoAsJSON);
					}
				}

			}

		}
		// If NATIVE_INPUT_ENABLED is false for Native Messaging, run the 
		// program until a specified number of records are logged.
		else {

			int remainingRecords = options.NUM_RECORDS;
			while (remainingRecords > 0) {
				// Get data from the Monitor as a new MonitorRecord object 
				// and convert the record to JSON format.
				MonitorRecord newRecord = monitor.getData();
				String recordAsJSON = newRecord.toJSON();
				// Append record data to log file.
				log.appendRecord(newRecord, recordAsJSON);

				// Send Native Message to Standard Output
				if (options.NATIVE_OUTPUT_ENABLED) {
					// Send Native Message to Standard Output
					NativeMessage.send(recordAsJSON);
					fileLogger.logNativeMessage("Sent: " + recordAsJSON);
				}

				remainingRecords--;
			}

		}

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
				String suggestionRequest = "{\"user\":" + log.getUserID() + ",\"batch\":\"3\"}";
				suggestions = server.get(suggestionRequest);
				fileLogger.logServerMessage("Sent: GET " + "suggestions");
				fileLogger.logServerMessage("Received: " + suggestions);
			} else {
				System.err.println();
				System.err.println(serverResponse);
			}

			TimeUnit.SECONDS.sleep(5);
			//			server.closeConnection();

		}

		// Send suggestions through Native Messaging
		if (options.NATIVE_INPUT_ENABLED) {
			String nativeMessage = NativeMessage.read(System.in);
			String message = NativeMessage.getJSONValue(nativeMessage, "message");
			fileLogger.logNativeMessage("Received: " + nativeMessage);
			if (message == null || !GET_SUGGESTIONS.equalsIgnoreCase(message)) {
				System.err.println("The message received by Standard Input '" + nativeMessage + "' was unexpected.");
				fileLogger.logError("The message received by Standard Input '" + nativeMessage + "' was unexpected.");
			} else if (options.NATIVE_OUTPUT_ENABLED) {
				NativeMessage.send(suggestions);
				fileLogger.logNativeMessage("Sent: " + suggestions);
			}
		} else if (options.NATIVE_OUTPUT_ENABLED) {
			NativeMessage.send(suggestions);
			fileLogger.logNativeMessage("Sent: " + suggestions);
		}

		// Sleep before attempting to delete or close any files, to give the 
		// last run enough time to finish executing.
		TimeUnit.SECONDS.sleep(5);
		//		Monitor.deleteOutputFiles();
		fileLogger.closeLogs();

	}

}
