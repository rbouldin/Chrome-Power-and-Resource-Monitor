/** 
 *  ServerHandler.java
 *
 *  VERSION: 2021.04.03
 *  AUTHORS: Rae Bouldin
 *
 *  Written for Dr. Cameron's Systems & Networking Capstone at Virginia Tech.
 *  Modified from: https://www.baeldung.com/httpurlconnection-post.
 */
package src;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerHandler {

	private URL url;
	private HttpURLConnection connection;

	/**
	 *  Tries to open a HttpURLConnection to the server at the specified httpURL.
	 * 
	 *  @param httpURL e.g. "http://example.com" or "http://01.01.001.001:1000/"
	 */
	public ServerHandler(String httpURL) throws IOException {
		
		this.url = new URL(httpURL);
		this.connection = (HttpURLConnection) url.openConnection();
		
	}
	
	/**
	 *  Closes the connection to the server.
	 */
	public void closeConnection() throws IOException {
//		connection.getOutputStream().close();
//		connection.getInputStream().close();
		connection.disconnect();
	}

	/**
	 *  Sends a JSON formatted String to the server in a HTTP POST request.
	 *  
	 *  @return The response from the server. Should be "OK" if the message was
	 *          sent with no problems.
	 */
	public String postJSONMessage(String jsonInputString) throws IOException {
		
		// Set request method to POST
		connection.setRequestMethod("POST");
		// Set the content-type to send the request body in JSON format.
		connection.setRequestProperty("Content-Type", "application/json; utf-8");
		// Set the "Accept" request header to read the response in JSON format.
		connection.setRequestProperty("Accept", "application/json");
		// Set doOutput to true so that we are able to write content to the connection
		// output stream.
		connection.setDoOutput(true);

		// Create request body
		try (OutputStream os = connection.getOutputStream()) {
			byte[] input = jsonInputString.getBytes("utf-8");
			os.write(input, 0, input.length);
		} catch (ConnectException e) {
			System.err.println(e.toString());
			return e.getMessage();
		} catch (IOException e) {
			System.err.println(e.toString());
			return e.getMessage();
		}

		// Read the Response from Input Stream
		String serverResponse = "";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
			StringBuilder response = new StringBuilder();
			String responseLine = null;
			while ((responseLine = br.readLine()) != null) {
				response.append(responseLine.trim());
			}
			serverResponse = response.toString();
		} catch (ConnectException e) {
			System.err.println(e.toString());
			return e.getMessage();
		} catch (IOException e) {
			System.err.println(e.toString());
			return e.getMessage();
		}
		
		return serverResponse;
		
	}

}
