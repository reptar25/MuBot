package com.github.mubot.heroku;

import java.io.IOException;

import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRoutes;
import reactor.util.Logger;
import reactor.util.Loggers;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HerokuServer {

	private static final Logger LOGGER = Loggers.getLogger(HerokuServer.class);

	private static HerokuServer instance;

	private static final String INVITE_LINK = "https://discord.com/api/oauth2/authorize?client_id=776639160164941824&permissions=8&scope=bot";
	private static final String homeHtml = "<a href=\"" + INVITE_LINK + "\">Invite!</a>";

	private static final String inviteHtml = "<html>\r\n" + "   <head>\r\n" + "      <title>HTML Meta Tag</title>\r\n"
			+ "      <meta http-equiv = \"refresh\" content = \"0; url = " + INVITE_LINK + "\" />\r\n"
			+ "   </head>\r\n" + "   <body>\r\n" + "\r\n" + "   </body>\r\n" + "</html>";

	public static HerokuServer create(int port) throws IOException {
		if (instance == null)
			instance = new HerokuServer(port);

		return instance;
	}

	private HerokuServer(int port) throws IOException {
		HttpServer.create().host("0.0.0.0").port(port).route(this::routes).bindNow();
		LOGGER.info("Server started on port " + port);
	}

	private void routes(HttpServerRoutes routes) {
		routeIndex(routes);
		routeInvite(routes);
	}

	private void routeIndex(HttpServerRoutes routes) {
		routes.get("/",
				(request, response) -> response.status(HttpResponseStatus.OK)
						.header(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(homeHtml.length()))
						.sendString(Mono.just(homeHtml)))
				.head("/", (request, response) -> response.status(HttpResponseStatus.OK));
	}

	private void routeInvite(HttpServerRoutes routes) {
		routes.get("/invite",
				(request, response) -> response.status(HttpResponseStatus.OK)
						.header(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(inviteHtml.length()))
						.sendString(Mono.just(inviteHtml)));
	}
}