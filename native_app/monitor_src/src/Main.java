/** 
 *  Main.java
 *
 *  VERSION: 2021.04.03
 *  AUTHORS: Rae Bouldin, Zinan Guo
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		final String STOP_MONITORING_PHRASE = "stop monitoring";
		String nativeMessage = "";
		RunOptions options = new RunOptions(args);
		
		LogFileHandler messageLogger = new LogFileHandler();
		if (options.NATIVE_LOGGING_ENABLED) {
			messageLogger.startNativeLog();
		}
		
		// The initial updateData() will create the CSV output files and 
		// sleeping for 4 seconds will ensure the files have enough time to 
		// generate. Not sleeping for long enough may create FileNotFound 
		// Exceptions.
//		Monitor.updateData();
//		TimeUnit.SECONDS.sleep(5);
		
		// Read the first Native Message from Standard Input. 
		// The first message should be "connected" to establish that messaging is working.
		if (options.NATIVE_INPUT_ENABLED) {
			
			nativeMessage = NativeMessage.read(System.in);
			
			if (options.NATIVE_LOGGING_ENABLED) {
				messageLogger.logNativeMessage("Recieved message: " + nativeMessage);
			}
			
			String value = NativeMessage.getJSONValue(nativeMessage, "message");
			if (!value.equalsIgnoreCase("connected")) {
				System.err.println("Unexpected native message: '" + value + "'");
			}
			
		}
		
		MonitorLog log = new MonitorLog("bobby", "3", "[]", "2");
		int i = options.NUM_RECORDS;
		while (i > 0) {
			// Read Native Message from Standard Input
			if (options.NATIVE_INPUT_ENABLED) {
				nativeMessage = NativeMessage.read(System.in);
				if (options.NATIVE_LOGGING_ENABLED) {
					messageLogger.logNativeMessage("Recieved message: " + nativeMessage);
				}
				String message = NativeMessage.getJSONValue(nativeMessage, "message");
				if (message.equalsIgnoreCase(STOP_MONITORING_PHRASE)) {
					System.err.println("Monitoring stopped by Native Message.");
					break;
				}
			}
			
			// Update power and resource monitoring files.
			Monitor.updateData();
			TimeUnit.SECONDS.sleep(2);
			
			// Create new MonitorRecord object and convert it to JSON.
			MonitorRecord newRecord = 
					new MonitorRecord( Monitor.getPowerSensorValue("CPU Package"),
									   Monitor.getCPUTotal(),
									   Monitor.getMemoryTotal(), 
									   Monitor.getGPUMemory());
			String recordAsJSON = newRecord.toJSON();
			
			// Append record data to log file.
			log.appendRecord(newRecord, recordAsJSON);
			
			if (options.NATIVE_OUTPUT_ENABLED) {
				// Send Native Message to Standard Output
				NativeMessage.send(recordAsJSON);
				if (options.NATIVE_LOGGING_ENABLED) {
					messageLogger.logNativeMessage("Sent message: " + recordAsJSON);
				}
			}
			
			i--;
		}
		
		// Sleep before attempting to delete the files, to give the last run of
		// updateData() enough time to finish executing.
		TimeUnit.SECONDS.sleep(5);
		Monitor.deleteOutputFiles();
		
		if (options.NATIVE_LOGGING_ENABLED) {
			messageLogger.closeNativeLog();
		}
		
		// Send session log to the server.
		// Server: IP = "52.91.154.176", Port = "8000"
		if (options.SERVER_MESSAGING_ENABLED) {
			String serverURL = "http://52.91.154.176:8000/data/";
			ServerHandler server = new ServerHandler(serverURL);
			String serverResponse = server.postJSONMessage(log.toJSON());
			if (!serverResponse.equals("OK")) {
				System.err.println();
				System.err.println(serverResponse);
			}
			
			// TODO : GET list of suggestions
			
			TimeUnit.SECONDS.sleep(5);
			server.closeConnection();
		}

	}

}
