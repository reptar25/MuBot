package com.github.MudPitBot.heroku;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HerokuServer {

	private int port;
	private HttpServer server;
	private static HerokuServer instance;

	public static HerokuServer create(int port) throws IOException {
		if (instance == null)
			instance = new HerokuServer(port);

		return instance;
	}

	private HerokuServer(int port) throws IOException {
		this.port = port;

//		try {
		server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
		server.createContext("/", new RootResponseHandler());
		server.setExecutor(null);
		server.start();
		System.out.println("Server started on port " + port);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}

}

class RootResponseHandler implements HttpHandler {
	private static final String homeHtml = "<h1 style='font-family: sans-serif;'>Hey</h1>"
			+ "<img style=\"-webkit-user-select: none;margin: auto;cursor: zoom-in;\""
			+ " src=\"https://i.ytimg.com/vi/4Qto049GEkA/maxresdefault.jpg\" width=\"235\" height=\"132\">";

	public void handle(HttpExchange exchange) throws IOException {
//		Headers h = exchange.getRequestHeaders();
//		h.add("Content-Type", "text/html");

		InputStream is = exchange.getRequestBody();
		exchange.sendResponseHeaders(200, homeHtml.length());
		OutputStream os = exchange.getResponseBody();
		os.write(homeHtml.getBytes());
		os.flush();
		os.close();
		is.close();
	}
}
