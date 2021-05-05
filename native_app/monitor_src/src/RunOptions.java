/** 
 *  RunOptions.java
 *
 *  VERSION: 2021.05.04
 *  AUTHORS: Rae Bouldin
 *  
 *  DESCRIPTION:
 *    Keeps track of the run options which are enabled for a program instance. 
 *    Run options can be set as program arguments when the program is launched,
 *    or later using the parseOptions method.
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

public class RunOptions {
	
	public int NUM_RECORDS = 60;
	
	public boolean OPEN_HARDWARE_MONITOR_DETECTED = false;
	
	public boolean NATIVE_INPUT_ENABLED = true;
	public boolean NATIVE_OUTPUT_ENABLED = true;
	public boolean NATIVE_LOGGING_ENABLED = false;
	
	public boolean SERVER_MESSAGING_ENABLED = false;
	public boolean SERVER_LOGGING_ENABLED = false;
	
	public boolean LOGGING_ENABLED = false;
	public boolean ERROR_LOGGING_ENABLED = false;
	public boolean USER_LOGGING_ENABLED = false;
	
	public RunOptions(String[] args) {
		parseOptions(args);
	}
	
	/** args should be formatted the same way you would expect in a regular 
	 *  main method. */
	public void parseOptions(String[] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-records")) {
				if (i+1 < args.length && Character.isDigit(args[i+1].charAt(0))) {
					try {
						int input = Integer.parseInt(args[i+1]);
						NUM_RECORDS = input;
						i++;
					} catch (NumberFormatException e) {
						System.err.println("Invalid args at \"" + args[0] + "\".");
						System.exit(1);
					}
				}
			}
			else if (args[i].equals("-OHM")) {
            	if (i+1 < args.length) {
            		String op = args[i+1];
	            	if (op.equalsIgnoreCase("off")) {
	            		OPEN_HARDWARE_MONITOR_DETECTED = false;
	            		i++;
	            	}
	            	else if (op.equalsIgnoreCase("on")) {
	            		OPEN_HARDWARE_MONITOR_DETECTED = true;
	            		i++;
	            	}
	            	else {
	            		System.err.println("Error parsing '-OHM'");
	            	}
            	} 
            	else {
            		System.err.println("Error parsing '-OHM'");
            	}
            } 
			else if (args[i].equals("-native")) {
            	if (i+1 < args.length) {
            		String op = args[i+1];
	            	if (op.equalsIgnoreCase("off")) {
	            		NATIVE_INPUT_ENABLED = false;
	            		NATIVE_OUTPUT_ENABLED = false;
	            		i++;
	            	}
	            	else if (op.equalsIgnoreCase("on")) {
	            		NATIVE_INPUT_ENABLED = true;
	            		NATIVE_OUTPUT_ENABLED = true;
	            		i++;
	            	}
	            	else {
	            		System.err.println("Error parsing '-native'");
	            	}
            	} 
            	else {
            		System.err.println("Error parsing '-native'");
            	}
            }
			else if (args[i].equals("-nativeInput")) {
				if (i+1 < args.length) {
            		String op = args[i+1];
	            	if (op.equalsIgnoreCase("off")) {
	            		NATIVE_INPUT_ENABLED = false;
	            		i++;
	            	}
	            	else if (op.equalsIgnoreCase("on")) {
	            		NATIVE_INPUT_ENABLED = true;
	            		i++;
	            	}
	            	else {
	            		System.err.println("Error parsing '-nativeInput'");
	            	}
            	} 
            	else {
            		System.err.println("Error parsing '-nativeInput'");
            	}
			}
			else if (args[i].equals("-nativeOutput")) {
				if (i+1 < args.length) {
            		String op = args[i+1];
	            	if (op.equalsIgnoreCase("off")) {
	            		NATIVE_OUTPUT_ENABLED = false;
	            		i++;
	            	}
	            	else if (op.equalsIgnoreCase("on")) {
	            		NATIVE_OUTPUT_ENABLED = true;
	            		i++;
	            	}
	            	else {
	            		System.err.println("Error parsing '-nativeOutput'");
	            	}
            	} 
            	else {
            		System.err.println("Error parsing '-nativeOutput'");
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
	            		System.err.println("Error parsing '-server'");
	            	}
            	} 
            	else {
            		System.err.println("Error parsing '-server'");
            	}
            } 
            else if (args[i].equals("-log")) {
            	LOGGING_ENABLED = true;
            }
            else if (args[i].equals("-logErrors")) {
            	ERROR_LOGGING_ENABLED = true;
            }
            else if (args[i].equals("-logNative")) {
            	NATIVE_LOGGING_ENABLED = true;
            }
            else if (args[i].equals("-logServer")) {
            	SERVER_LOGGING_ENABLED = true;
            }
            else if (args[i].equals("-logForUser")) {
            	USER_LOGGING_ENABLED = true;
            }
            else {
				System.err.println("Unknown parameter at \"" + args[i] + "\".");
			}
		}
	}

}
