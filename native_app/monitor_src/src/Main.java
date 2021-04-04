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
		
		boolean NATIVE_MESSAGING_ENABLED = true;
		boolean SERVER_MESSAGING_ENABLED = false;
		
		boolean EXIT_SIGNAL = false;
//		String EXIT_PHRASE = "EXIT_NATIVE";
		
		int num_records = 60;
		
		// See if the user has used any options
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-records")) {
				if (i+1 < args.length && Character.isDigit(args[i+1].charAt(0))) {
					try {
						int input = Integer.parseInt(args[i+1]);
						num_records = input;
						i++;
					} catch (NumberFormatException e) {
						System.err.println("Invalid args at \"" + args[0] + "\".");
						System.exit(1);
					}
				}
			}
			else if (args[i].equals("-native")) {
            	if (i+1 < args.length) {
            		String op = args[i+1];
	            	if (op.equalsIgnoreCase("off")) {
	            		NATIVE_MESSAGING_ENABLED = false;
	            		i++;
	            	}
	            	else if (op.equalsIgnoreCase("on")) {
	            		NATIVE_MESSAGING_ENABLED = true;
	            		i++;
	            	}
	            	else {
	            		System.err.println("Unknown args at \"" + args[i] + "\".");
	            		System.exit(1);
	            	}
            	} 
            	else {
            		NATIVE_MESSAGING_ENABLED = true;
            	}
            }
            else if (args[i].equals("-server")) {
            	if (i+1 < args.length) {
            		String op = args[i+1];
	            	if (op.equalsIgnoreCase("off")) {
	            		SERVER_MESSAGING_ENABLED = false;
	            		i++;
	            	}
	            	else if (op.equalsIgnoreCase("on")) {
	            		SERVER_MESSAGING_ENABLED = true;
	            		i++;
	            	}
	            	else {
	            		System.err.println("Unknown args at \"" + args[i] + "\".");
	            		System.exit(1);
	            	}
            	} 
            	else {
            		SERVER_MESSAGING_ENABLED = true;
            	}
            }
            else if (args[i].equals("-clean")) {
                Monitor.deleteOutputFiles();
                System.exit(0);
            } else {
                System.err.println("Unknown args at \"" + args[i] + "\".");
//                System.exit(1);
            }
        }
		
		
		// The initial updateData() will create the CSV output files and 
		// sleeping for 4 seconds will ensure the files have enough time to 
		// generate. Not sleeping for long enough may create FileNotFound 
		// Exceptions.
		Monitor.updateData();
		TimeUnit.SECONDS.sleep(5);
		
		MonitorLog log = new MonitorLog("bobby", "3", "[]", "2");
		int i = num_records;
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
			if (NATIVE_MESSAGING_ENABLED) {
				NativeMessage.send(recordAsJSON);
			}
			
			// Read Native Message from Standard Input
//			String jsonRequest = NativeMessage.read(System.in);
			
			i--;
		}
		
		// Sleep before attempting to delete the files, to give the last run of
		// updateData() enough time to finish executing.
		TimeUnit.SECONDS.sleep(5);
		Monitor.deleteOutputFiles();
		
		// Send session log to the server.
		// Server: IP = "52.91.154.176", Port = "8000"
		if (SERVER_MESSAGING_ENABLED) {
			String serverURL = "http://52.91.154.176:8000/data/";
			ServerHandler server = new ServerHandler(serverURL);
			String serverResponse = server.postJSONMessage(log.toJSON());
			if (!serverResponse.equals("OK")) {
				System.err.println();
				System.err.println(serverResponse);
			}
			
			TimeUnit.SECONDS.sleep(5);
			server.closeConnection();
		}

	}

}
