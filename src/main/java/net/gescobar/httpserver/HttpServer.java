package net.gescobar.httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>An HTTP Server that receives and parses HTTP requests. When a connection is received, a {@link HttpConnection} 
 * is created to handle the request in a separate thread - using a thread pool.</p>
 * 
 * <p>Starting the HTTP Server is as simple as instantiating this class and calling the {@link #start} method:</p>
 * 
 * <code>
 * 	HttpServer server = new HttpServer();
 * 	server.start();
 * 	...
 * 
 *  // somewhere else
 *  server.stop();
 * </code>
 * 
 * <p>To process HTTP requests you will need to provide an implementation of the {@link Handler} interface using the 
 * constructor {@link #HttpServer(int, Handler)} or the {@link #setHandler(Handler)} method. If no {@link Handler} 
 * is specified, a default implementation is provided that always returns a status 200 (OK) with no content. 
 * 
 * @author German Escobar
 */
public class HttpServer {
	
	private Logger log = LoggerFactory.getLogger(HttpServer.class);
	
	/**
	 * The default port to use unless other is specified.
	 */
	private static final int DEFAULT_PORT = 3000;
	
	/**
	 * The executor in which the requests are handled.
	 */
	private ExecutorService executor = Executors.newFixedThreadPool(10);
	
	/**
	 * Used to listen in the specified port.
	 */
	private ServerSocket serverSocket;
	
	/**
	 * The thread that is constantly accepting connections. It delegates them to a {@link HttpConnection}.
	 */
	private ServerDaemon serverDaemon;
	
	/**
	 * The port in which this server is going to listen.
	 */
	private int port;
	
	/**
	 * The implementation that will handle requests.
	 */
	private Handler handler;
	
	/**
	 * Tells if the server is running or not.
	 */
	private boolean running;
	
	/**
	 * Constructor. Initializes the server with the default port and {@link Handler} implementation.
	 */
	public HttpServer() {
		this(DEFAULT_PORT);
	}
	
	/**
	 * Constructor. Initializes the server with the specified port and default {@link Handler} implementation.
	 * 
	 * @param port the port in which the server should listen.
	 */
	public HttpServer(int port) {
		this(new Handler() {

			@Override
			public void handle(Request request, Response response) {}
			
		}, port);
	}
	
	/**
	 * Constructor. Initializes the server with the specified {@link Handler} implementation and default port.
	 * 
	 * @param handler the {@link Handler} implementation that will handle the requests.
	 */
	public HttpServer(Handler handler) {
		this(handler, DEFAULT_PORT);
	}
	
	/**
	 * Constructor. Initializes the server with the specified port and {@link Handler} implementation.
	 *  
	 * @param handler the {@link Handler} implementation that will handle the requests.
	 * @param port the port in which the server should listen.
	 */
	public HttpServer(Handler handler, int port) {
		this.port = port;
		this.handler = handler;
	}
	
	/**
	 * Starts the HTTP Server.
	 * 
	 * @return itself for method chaining
	 * @throws IOException if an error occurs in the underlying connection.
	 */
	public HttpServer start() throws IOException {
		
		if (running) {
			log.warn("HTTP Server already running ... ");
			return this;
		}
		
		log.debug("starting the HTTP Server ... ");
		
		running = true;
		
		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(500);
		
		serverDaemon = new ServerDaemon();
		serverDaemon.start();
		
		log.info("<< HTTP Server running on port " + port + " >>");
		
		return this;
		
	}
	
	/**
	 * Stops the server gracefully.
	 */
	public void stop() {
		
		// signals the daemon thread to stop the loop and terminate the execution.
		log.debug("stopping the HTTP Server ... ");
		running = false;
	
		// wait until it actually stops
		try { serverDaemon.join(); } catch (InterruptedException e) {}
	}
	
	/**
	 * This is the main thread listening for new connections and delegating them to {@link HttpConnection} objects.
	 * 
	 * @author German Escobar
	 */
	private class ServerDaemon extends Thread {

		@Override
		public void run() {
			
			while (running) {
				try {
					
					Socket socket = serverSocket.accept();
					
					// this is what actually process the request
					executor.execute( new HttpConnection(socket, handler) );
					
				} catch (SocketTimeoutException e) {
					// no connection received this time
				} catch (IOException e) {
					log.error("IOException while accepting connection: " + e.getMessage(), e);
				}
			}	
			
			try {
				serverSocket.close();
			} catch (IOException e) {
				log.error("IOException while stopping server: " + e.getMessage(), e);
			}
			
			log.trace("daemon thread is exiting ...");
		}
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public static void main(String...args) throws IOException {
		final HttpServer server = new HttpServer().start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public synchronized void start() {
				server.stop();
			}
			
		});
	}

}