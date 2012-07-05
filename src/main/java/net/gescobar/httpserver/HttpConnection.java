package net.gescobar.httpserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles an HTTP client connection. It parses the request creating an {@link Request} and a {@link Response} 
 * implementations that are passed to the {@link Handler} implementation.
 * 
 * @author German Escobar
 */
public class HttpConnection implements Runnable {
	
	private Logger log = LoggerFactory.getLogger(HttpConnection.class);
	
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
		
		log.trace("handling HTTP request ... ");
		
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
			
			log.trace("http request finished");
			
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
		 * The Host header.
		 */
		private String host;
		
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
				
				// headers come in the form "name: value"
				String name = headerLine.split(":")[0].trim();
				String value = headerLine.split(":")[1].trim();
				
				// add to the headers only if there is no corresponding field (e.g. "Host" header is mapped to the 
				// *host* field of the request)
				if ( !isKnownHeader(name, value) ) {
					headers.put(name, value);
				}
				
				// read next line
				headerLine = reader.readLine();
			}
			
			return headers;
			
		}
		
		/**
		 * Checks if it is a known header and sets the corresponding field.
		 * 
		 * @param name the name of the header to check.
		 * @param value the value of the header to check.
		 * 
		 * @return true if it is a known header, false otherwise
		 */
		private boolean isKnownHeader(String name, String value) {
			
			boolean ret = false;
			
			if (name.equalsIgnoreCase("host")) {
				host = value;
				return true;
			}
			
			return ret;
			
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
			return host;
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
		public Response status(HttpStatus status) {
			this.status = status;
			
			return this;
		}

		@Override
		public Response ok() {
			return status(HttpStatus.OK);
		}

		@Override
		public Response notFound() {
			return status(HttpStatus.NOT_FOUND);
		}

		@Override
		public Response contentType(String contentType) {
			this.contentType = contentType;
			
			return this;
		}

		public String toString() {
			String ret = "HTTP/1.1 " + status.getCode() + " " + status.getReason() + "\r\n";
			
			if (contentType != null) {
				ret += "Content-Type: " + contentType + "\r\n";
			}
			
			return ret + "\r\n";
		}
		
	}
}
