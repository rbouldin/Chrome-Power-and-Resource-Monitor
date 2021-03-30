/** 
 *  Main.java
 *
 *  VERSION: 2021.03.30
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

import com.google.gson.Gson;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {

		// The initial updateData() will create the CSV output files and 
		// sleeping for 4 seconds will ensure the files have enough time to 
		// generate. Not sleeping for long enough may create FileNotFound 
		// Exceptions.
		Monitor.updateData();
		TimeUnit.SECONDS.sleep(4);
		
		int i = 60;
		while (i > 0) {
			Monitor.updateData();
			TimeUnit.SECONDS.sleep(1);
			sendMonitorRecord();
			i--;
		}
		
		// Sleep before attempting to delete the files, to give the last run of
		// updateData() enough time to finish executing.
		TimeUnit.SECONDS.sleep(4);
		Monitor.deleteOutputFiles();

	}
	
	private static void sendMonitorRecord() throws IOException {
		// Create new MonitorRecord object
		MonitorRecord newRecord = 
				new MonitorRecord( Monitor.getPowerSensorValue("CPU Package"),
								   Monitor.getCPUTotal(),
								   Monitor.getMemoryTotal(), 
								   Monitor.getGPUMemory());
		// Convert object to JSON using GSON Lib
		Gson gson = new Gson();
		String jsonRecord = gson.toJson(newRecord);
		// Send the JSON to Standard Output in formatted bytes so it can be 
		// read by the Chrome extension.
		sendMessage(jsonRecord);
	}

	private static void sendMessage(String message) throws IOException {
		System.out.write(getBytes(message.length()));
		System.out.write(message.getBytes("UTF-8"));
		System.out.flush();
	}

	private static byte[] getBytes(int length) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (length & 0xFF);
		bytes[1] = (byte) ((length >> 8) & 0xFF);
		bytes[2] = (byte) ((length >> 16) & 0xFF);
		bytes[3] = (byte) ((length >> 24) & 0xFF);
		return bytes;
	}

}
