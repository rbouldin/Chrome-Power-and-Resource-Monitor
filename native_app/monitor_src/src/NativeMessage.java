/** 
 *  NativeMessage.java
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
import java.io.InputStream;
import java.io.InterruptedIOException;

public class NativeMessage {
	
	public static void send(String jsonString) throws IOException {
		System.out.write(getBytes(jsonString.length()));
		System.out.write(jsonString.getBytes("UTF-8"));
		System.out.flush();
	}
	
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

	private static byte[] getBytes(int length) {
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (length & 0xFF);
		bytes[1] = (byte) ((length >> 8) & 0xFF);
		bytes[2] = (byte) ((length >> 16) & 0xFF);
		bytes[3] = (byte) ((length >> 24) & 0xFF);
		return bytes;
	}
	
	private static int getInt(byte[] bytes) {
		return (bytes[3] << 24) & 0xff000000 | (bytes[2] << 16) & 0x00ff0000 | (bytes[1] << 8) & 0x0000ff00
				| (bytes[0] << 0) & 0x000000ff;
	}

}
