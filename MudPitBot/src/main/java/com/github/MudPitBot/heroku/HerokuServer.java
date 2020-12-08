package com.github.MudPitBot.heroku;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HerokuServer {

	private HttpServer server;
	private static HerokuServer instance;

	public static HerokuServer create(int port) throws IOException {
		if (instance == null)
			instance = new HerokuServer(port);

		return instance;
	}

	private HerokuServer(int port) throws IOException {
//		try {
		server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
		server.createContext("/", new RootResponseHandler());
		server.createContext("/invite", new InviteResponseHandler());
		server.setExecutor(null);
		server.start();
		System.out.println("Server started on port " + port);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

	}

}

class RootResponseHandler implements HttpHandler {
	private static final String INVITE_LINK = "https://discord.com/api/oauth2/authorize?client_id=776639160164941824&permissions=8&scope=bot";
	private static final String homeHtml = "<a href=\"" + INVITE_LINK + "\">Invite!</a>"
			+ "<img style=\"-webkit-user-select: none;margin: auto;\""
			+ " src=\"https://i.ytimg.com/vi/4Qto049GEkA/maxresdefault.jpg\" width=\"235\" height=\"132\">";

	@Override
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

class InviteResponseHandler implements HttpHandler {
	private static final String INVITE_LINK = "https://discord.com/api/oauth2/authorize?client_id=776639160164941824&permissions=8&scope=bot";
	private static final String inviteHtml = "<html>\r\n" + "   <head>\r\n" + "      <title>HTML Meta Tag</title>\r\n"
			+ "      <meta http-equiv = \"refresh\" content = \"0; url = " + INVITE_LINK + "\" />\r\n"
			+ "   </head>\r\n" + "   <body>\r\n" + "\r\n" + "   </body>\r\n" + "</html>";

	@Override
	public void handle(HttpExchange exchange) throws IOException {
//			Headers h = exchange.getRequestHeaders();
//			h.add("Content-Type", "text/html");

		InputStream is = exchange.getRequestBody();
		exchange.sendResponseHeaders(200, inviteHtml.length());
		OutputStream os = exchange.getResponseBody();
		os.write(inviteHtml.getBytes());
		os.flush();
		os.close();
		is.close();
	}
}
