package com.github.MudPitBot.Heroku;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HerokuServer {

	private int port;
	HttpServer server;

	public HerokuServer(int port) {
		this.port = port;

		try {
			server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
			server.createContext("/", new ResponseHandler());
			server.setExecutor(null);
			server.start();
			System.out.println(" Server started on port " + port);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

class ResponseHandler implements HttpHandler {
	public void handle(HttpExchange t) throws IOException {
		InputStream is = t.getRequestBody();
		String response = "<h1 style='font-family: sans-serif;'>pong</h1>";
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.flush();
		os.close();
		is.close();
	}
}
