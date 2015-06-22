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
	
	private final Logger log = LoggerFactory.getLogger(HttpConnection.class);
	
	/**
	 * The socket with the underlying connection.
	 */
	private final Socket socket;
	
	/**
	 * The implementation that will handle requests.
	 */
	private final Handler handler;
	
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
		
		try (InputStream is = socket.getInputStream();
                     BufferedReader reader = new BufferedReader( new InputStreamReader(is) );
                     OutputStream os = socket.getOutputStream();) {	

			// build the request and response object
			RequestImpl request = new RequestImpl(reader); 
			ResponseImpl response = new ResponseImpl();
			
			handler.handle(request, response);
			
			os.write(response.toString().getBytes());
			os.flush();
			os.close();
			
			log.trace("http request finished");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
