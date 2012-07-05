[![Build Status](https://buildhive.cloudbees.com/job/germanescobar/job/http-server/badge/icon)](https://buildhive.cloudbees.com/job/germanescobar/job/http-server/)

This is a super simple HTTP Server written in Java for fun and *learning*. It accepts HTTP requests and allows you to easily handle them.

## Starting and stopping

To start the server you need to instantiate the `net.gescobar.httpserver.HttpServer` class and call the `start()` method:

```java
HttpServer server = new HttpServer(3000); // 3000 is the port to listen from, change it as needed
server.start();
		
// somewhere else
server.stop();
```

## Handling HTTP requests

To handle HTTP requests you will need to provide an implementation of the `net.gescobar.httpserver.Handler` interface. For example:

```java
public class MyHandler implements Handler {
  
    @Override
    public void handle(Request request, Response response) {
  
        if (request.getPath().equals("/")) {
            response.ok().write("<h1>Hola Mundo</h1>");
        } else {
            response.notFound();
        }
     
    }	
}
```

To use your implementation, set it in the `HttpServer` instance:

```java
HttpServer server = new HttpServer(3000, new MyHandler());
		
// or
		
server.setHandler( new MyHandler() );
````

If you don't provide a `net.gescobar.httpserver.Handler` implementation, the default one (that returns status 200 OK with no content) will be used.

That's it! As you can see, it's a simple, yet powerful design that will allow you to handle HTTP requests. You can extend it to provide new services such as static resources handling, session management and routing.