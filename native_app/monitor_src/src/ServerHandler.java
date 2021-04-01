/** 
 *  ServerMessage.java
 *
 *  VERSION: 2021.04.01
 *  AUTHORS: Rae Bouldin
 *
 *  DESCRIPTION:
 *    ...
 *    Modified from: https://www.baeldung.com/httpurlconnection-post
 * 
 *  (Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech)
 */
package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerHandler {

	private URL url;
	private HttpURLConnection con;

	/**
	 * Tries to open a connection to the server at the specified httpURL.
	 * 
	 * @param httpURL e.g. "http://example.com"
	 * @throws IOException
	 */
	public ServerHandler(String httpURL) throws IOException {
		
		this.url = new URL(httpURL);
		this.con = (HttpURLConnection) url.openConnection();
		
	}
	
	public void closeConnection() throws IOException {
		con.getInputStream().close();
		con.getOutputStream().close();
		con.disconnect();
	}

	public void postJSONMessage(String jsonInputString) throws IOException {
		
		// Set request method to POST
		con.setRequestMethod("POST");
		// Set the content-type to send the request body in JSON format.
		con.setRequestProperty("Content-Type", "application/json; utf-8");
		// Set the "Accept" request header to read the response in JSON format.
		con.setRequestProperty("Accept", "application/json");
		// Set doOutput to true so that we are able to write content to the connection
		// output stream.
		con.setDoOutput(true);

		// Create request body
		try (OutputStream os = con.getOutputStream()) {
			byte[] input = jsonInputString.getBytes("utf-8");
			os.write(input, 0, input.length);
		}

		// Read the Response from Input Stream
//		try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
//			StringBuilder response = new StringBuilder();
//			String responseLine = null;
//			while ((responseLine = br.readLine()) != null) {
//				response.append(responseLine.trim());
//			}
//			System.out.println(response.toString());
//		}
	}

}
