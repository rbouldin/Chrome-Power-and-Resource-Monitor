/** 
 *  NativeMessage.java
 *
 *  VERSION: 2021.04.29
 *  AUTHORS: Rae Bouldin, Zinan Guo
 * 
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 */
package src;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;

public class NativeMessage {
	
	
	/**
	 *  Sends a JSON formatted String to Standard Output in byte representation
	 *  so that it can be accepted through Native Messaging by a Google Chrome 
	 *  extension.
	 */
	public static void send(String jsonString) throws IOException {
		System.out.write(getBytes(jsonString.length()));
		System.out.write(jsonString.getBytes("UTF-8"));
		System.out.flush();
	}
	
	
	/**
	 *  Reads the byte representation sent from Standard Input through Native 
	 *  Messaging and translates it into a JSON formatted String.
	 */
	public static String read(InputStream in) throws IOException {
		byte[] b = new byte[4];
		in.read(b); // Read the size of message

		int size = getInt(b);

		if (size == 0) {
			throw new InterruptedIOException("Blocked communication");
		}

		b = new byte[size];
		in.read(b);

		return new String(b, "UTF-8");
	}
	
	
	public static String getJSONValue(String jsonString, String id) {
		
		// Check for invalid input
		if (jsonString == null || id == null) {
			return null;
		}
		if (jsonString.charAt(0) != '{' || jsonString.charAt(jsonString.length()-1) != '}') {
			System.err.println(
					"The jsonString input into getJSONValue was not a JSON formatted String.");
			return null;
		}
		
		// Format ID to be surrounded in quotes
		String jsonID = "\"" + id + "\"";
		
		// Get the index of the ID from the JSON String, if it exists.
		int beginIndex = jsonString.indexOf(jsonID, 0);
		if (beginIndex > 0) {
			// Trim the jsonString so that it starts at the element with the 
			// specified jsonID.
			String offsetStr = jsonString.substring(beginIndex);
			// Find the end of the element with the specified jsonID
			int arrayElemOffset = 0;
			if (offsetStr.length() > (jsonID.length() + 1) 
					&& offsetStr.charAt(jsonID.length()+1) == '[') {
				// Check if the JSON element is an array, because we need to 
				// offset by it's end brace if it is.
				arrayElemOffset = offsetStr.indexOf(']');
			}
			int offset = offsetStr.indexOf(",\"", arrayElemOffset);
			if (offset < 0) { offset = offsetStr.indexOf("}"); }
			if (offset > 0) {
				int endIndex = beginIndex + offset;
				// Extract the full JSON element from the jsonString.
				String elementString = jsonString.substring(beginIndex, endIndex);
				// Split the element into its "key":value pair
				String[] elementSplit = elementString.split("\":");
				// something went wrong if the elementSplit size isn't 2
				if (elementSplit.length == 2) {
					String value = elementSplit[1];
					// Remove extraneous quotes if the value was a String
					if (value.charAt(0) == '\"') {
						value = value.substring(1);
					}
					if (value.charAt(value.length()-1) == '\"') {
						value = value.substring(0, value.length()-1);
					}
					// Return the value
					return value;
				}
			}
		}
		
		return null;
		
	}

	
	/**
	 *  Gets the byte array representation of an integer.
	 */
	private static byte[] getBytes(int length) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (length & 0xFF);
		bytes[1] = (byte) ((length >> 8) & 0xFF);
		bytes[2] = (byte) ((length >> 16) & 0xFF);
		bytes[3] = (byte) ((length >> 24) & 0xFF);
		return bytes;
	}
	
	
	/**
	 * Get the integer representation of a byte array.
	 */
	private static int getInt(byte[] bytes) {
		return (bytes[3] << 24) & 0xff000000 | (bytes[2] << 16) & 0x00ff0000 | (bytes[1] << 8) & 0x0000ff00
				| (bytes[0] << 0) & 0x000000ff;
	}

	
}
