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

		while (true) {
			Monitor.updateData();
			sendMonitorRecord();
			TimeUnit.SECONDS.sleep(1);
		}

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
