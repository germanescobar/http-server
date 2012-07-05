package net.gescobar.httpserver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Future;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ning.http.client.AsyncHttpClient;

public class HttpServerTest {

	@Test
	public void shouldHandleGetRequest() throws Exception {
		
		HttpServer server = new HttpServer().start();
		
		try { 
			AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		    Future<com.ning.http.client.Response> f = asyncHttpClient.prepareGet("http://localhost:3000/").execute();
		    com.ning.http.client.Response r = f.get();
			
		    Assert.assertEquals(r.getStatusCode(), 200);
		    
		    asyncHttpClient.close();
		} finally {
			if (server != null) { server.stop(); }
		}
		
	}
	
	@Test(dependsOnMethods="shouldHandleGetRequest")
	public void shouldCallCustomHandler() throws Exception {
		
		Handler handler = mock(Handler.class);
		HttpServer server = new HttpServer(handler).start();
		
		try { 
			AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		    Future<com.ning.http.client.Response> f = asyncHttpClient.prepareGet("http://localhost:3000/").execute();
		    f.get();
		    
		    verify(handler).handle( any(Request.class), any(Response.class) );
		    
		} finally {
			if (server != null) { server.stop(); }
		}
		
	}
	
	@Test(dependsOnMethods="shouldCallCustomHandler")
	public void shouldSetPathAndMethodInRequest() throws Exception {
		
		TestHandler handler = new TestHandler();
		HttpServer server = new HttpServer(handler).start();
		
		try { 
			AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		    Future<com.ning.http.client.Response> f = asyncHttpClient.prepareGet("http://localhost:3000/").execute();
		    f.get();
		    
		    Request request = handler.getRequest();
		    Assert.assertNotNull(request);
		    Assert.assertEquals(request.getPath(), "/");
		    Assert.assertEquals(request.getMethod(), "GET");
		    
		} finally {
			if (server != null) { server.stop(); }
		}
		
	}
	
	@Test(dependsOnMethods="shouldCallCustomHandler")
	public void shouldSetHeadersInRequest() throws Exception {
		
		TestHandler handler = new TestHandler();
		HttpServer server = new HttpServer(handler).start();
		
		try { 
			AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		    Future<com.ning.http.client.Response> f = asyncHttpClient.prepareGet("http://localhost:3000/")
		    	.addHeader("Host", "localhost")
		    	.addHeader("Accepts", "application/json")
		    	.execute();
		    f.get();
		    
		    Request request = handler.getRequest();
		    Assert.assertNotNull(request);
		    Assert.assertEquals(request.getHost(), "localhost");
		    Assert.assertEquals(request.getHeader("Accepts"), "application/json");
		    
		} finally {
			if (server != null) { server.stop(); }
		}
		
	}
	
	@Test
	public void shouldWriteContentTypeInResponse() throws Exception {
		
		TestHandler handler = new TestHandler() {
			@Override
			public void doHandle(Request request, Response response) {
				response.contentType("application/json");
			}
		};
		HttpServer server = new HttpServer(handler).start();
		
		try { 
			AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		    Future<com.ning.http.client.Response> f = asyncHttpClient.prepareGet("http://localhost:3000/").execute();
		    com.ning.http.client.Response response = f.get();
		    
		    Assert.assertNotNull(response);
		    Assert.assertEquals(response.getContentType(), "application/json");
		    
		} finally {
			if (server != null) { server.stop(); }
		}
		
	}
	
	
	private class TestHandler implements Handler {
		
		private Request request;

		@Override
		public final void handle(Request request, Response response) {
			this.request = request;
			
			doHandle(request, response);
		}
		
		public void doHandle(Request request, Response response) {
			
		}
		
		public Request getRequest() {
			return request;
		}
		
	}
	
}
