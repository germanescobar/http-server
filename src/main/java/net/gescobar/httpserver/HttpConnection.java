package net.gescobar.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles an HTTP client connection. It parses the request creating an {@link Request} and a {@link Response} 
 * implementations that are passed to the {@link Handler} implementation.
 * 
 * @author German Escobar
 */
public class HttpConnection implements Runnable {
	
	/**
	 * The socket with the underlying connection.
	 */
	private Socket socket;
	
	/**
	 * The implementation that will handle requests.
	 */
	private Handler handler;
	
	/**
	 * Constructor.
	 * 
	 * @param socket
	 * @param handler
	 */
	public HttpConnection(Socket socket, Handler handler) {
		this.socket = socket;
		this.handler = handler;
	}

	@Override
	public void run() {
		
		try {
			
			InputStream is = socket.getInputStream();
			BufferedReader reader = new BufferedReader( new InputStreamReader(is) );
			
			// build the request and response object
			RequestImpl request = new RequestImpl(reader); 
			ResponseImpl response = new ResponseImpl();
			
			handler.handle(request, response);
			
			OutputStream os = socket.getOutputStream();
			os.write(response.toString().getBytes());
			os.flush();
			os.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * This is an internal implementation of the {@link Request} interface.
	 * 
	 * @author German Escobar
	 */
	private class RequestImpl implements Request {
		
		/**
		 * The HTTP method
		 */
		private String method;
		
		/**
		 * The request path
		 */
		private String path;
		
		/**
		 * The request headers
		 */
		private Map<String,String> headers;
		
		/**
		 * Constructor.
		 * 
		 * @param reader from which we are reading the headers. 
		 * 
		 * @throws IOException if an I/O error occurs in the underlying connection.
		 */
		public RequestImpl(BufferedReader reader) throws IOException {
			
			String request = reader.readLine();

			// get the method and the path
			method = request.split(" ")[0];
			path = request.split(" ")[1];
			
			// get the headers
			headers = retrieveHeaders(reader);
			
		}
		
		/**
		 * Helper method. Retrieves the headers of the request.
		 * 
		 * @param reader the reader from which we are retrieving the request information.
		 * 
		 * @return a Map<String,String> object with the headers of the request.
		 * @throws IOException if an I/O error occurs in the underlying communication.
		 */
		private Map<String,String> retrieveHeaders(BufferedReader reader) throws IOException {
			
			Map<String,String> headers = new HashMap<String,String>();
			
			// iterate through the headers
			String headerLine = reader.readLine();
			while( !headerLine.equals("") ) {
				
				String name = headerLine.split(":")[0].trim();
				String value = headerLine.split(":")[1].trim();
				
				headers.put(name, value);
				
				// read next line
				headerLine = reader.readLine();
			}
			
			return headers;
			
		}

		@Override
		public String getMethod() {
			return method;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public String getHost() {
			return null;
		}

		@Override
		public Map<String, String> getHeaders() {
			return headers;
		}

		@Override
		public String getHeader(String name) {
			return headers.get(name);
		}
		
	}
	
	/**
	 * This is a private implementation of the {@link Response interface}
	 * 
	 * @author German Escobar
	 */
	private class ResponseImpl implements Response {
		
		private HttpStatus status = HttpStatus.OK;
		
		private String contentType;

		@Override
		public void status(HttpStatus status) {
			this.status = status;
		}

		@Override
		public void contentType(String contentType) {
			this.contentType = contentType;
		}

		public String toString() {
			String ret = "HTTP/1.1 " + status.getCode() + " " + status.getReason() + "\n\r";
			
			if (contentType != null) {
				ret += "Content-Type: " + contentType + "\n\r";
			}
			
			return ret;
		}
		
	}
}
