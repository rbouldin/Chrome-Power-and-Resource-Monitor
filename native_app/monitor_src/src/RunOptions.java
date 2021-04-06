/** 
 *  RunOptions.java
 *
 *  VERSION: 2021.04.05
 *  AUTHORS: Rae Bouldin
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

public class RunOptions {
	
	public int NUM_RECORDS = 60;
	
	public boolean NATIVE_INPUT_ENABLED = true;
	public boolean NATIVE_OUTPUT_ENABLED = true;
	public boolean NATIVE_LOGGING_ENABLED = false;
	
	public boolean SERVER_MESSAGING_ENABLED = false;
	public boolean SERVER_LOGGING_ENABLED = false;
	
	public boolean LOGGING_ENABLED = false;
	public boolean ERROR_LOGGING_ENABLED = false;
	
	private String[] args;
	
	public RunOptions(String[] program_args) {
		this.args = program_args;
		parseOptions();
	}
	
	private void parseOptions() {
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
            else if (args[i].equals("-clean")) {
                Monitor.deleteOutputFiles();
                System.exit(0);
			} else {
				System.err.println("Unknown parameter at \"" + args[i] + "\".");
			}
		}
	}

}
