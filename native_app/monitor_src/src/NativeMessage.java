/** 
 *  NativeMessage.java
 *
 *  VERSION: 2021.04.03
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
		String[] json = jsonString.split("\"");
		for (int i = 0; i < json.length; i++) {
			if (json[i].equalsIgnoreCase(id) 
					&& i+2 < json.length && json[i+1].equals(":")) {
				return json[i+2];
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
