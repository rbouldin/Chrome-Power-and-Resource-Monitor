/** 
 *  Main.java
 *
 *  VERSION: 2021.04.01
 *  AUTHORS: Rae Bouldin, Zinan Guo
 *
 *  DESCRIPTION:
 *    ...
 * 
 *  (Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech)
 */
package src;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		String EXIT_PHRASE = "EXIT_NATIVE";
		boolean EXIT_SIGNAL = false;
		MonitorLog log = new MonitorLog("001", "1.0", "[]", "0");

		// The initial updateData() will create the CSV output files and 
		// sleeping for 4 seconds will ensure the files have enough time to 
		// generate. Not sleeping for long enough may create FileNotFound 
		// Exceptions.
		Monitor.updateData();
		TimeUnit.SECONDS.sleep(3);
		
		int i = 10;
		while (i > 0 && !EXIT_SIGNAL) {
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
			
			// Send Native Message to Standard Output
			NativeMessage.send(recordAsJSON);
			
			// Read Native Message from Standard Input
//			String jsonRequest = NativeMessage.read(System.in);
			
			i--;
		}
		
		// Sleep before attempting to delete the files, to give the last run of
		// updateData() enough time to finish executing.
		TimeUnit.SECONDS.sleep(5);
		Monitor.deleteOutputFiles();
		
		// Send session log to the server.
////		String sessionLog = log.toJSON();
//		String message = "{\"user\":\"robbie\",\"avg_cpu_power\":\"69\",\"avg_cpu_usage\":\"5\",\"avg_gpu_usage\":\"34\",\"avg_mem_usage\":\"19\",\"batch\":\"2\"}";  //"{\"batch\":\"3\"}";
//		System.out.println(message);
//		ServerHandler server = new ServerHandler("http://52.91.154.176:8000/data/");
//		server.postJSONMessage(message);
////		server.postJSONMessage(sessionLog);
//		server.closeConnection();

	}

}
