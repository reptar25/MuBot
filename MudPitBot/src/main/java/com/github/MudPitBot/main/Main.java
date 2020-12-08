package com.github.MudPitBot.main;

import java.io.IOException;

import com.github.MudPitBot.command.core.CommandClient;
import com.github.MudPitBot.heroku.HerokuServer;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import reactor.util.Logger;
import reactor.util.Loggers;

public class Main {

	private static final Logger LOGGER = Loggers.getLogger(Main.class);

	public static void main(String[] args) {
		String token = null;

		try {
			token = args[0];
		} catch (Exception e) {

		}

		if (token == null)
			token = System.getenv("token");

		// no token found
		if (token == null) {
			LOGGER.error("No token found. Dicord token needs to be first argument or an env var named \"token\"");
			return;
		}

		final GatewayDiscordClient client = DiscordClientBuilder.create(token).build().login().block();

		// we should only find this when running on Heroku
		String port = System.getenv("PORT");
		if (port != null) {
			// bind PORT for Heroku integration
			try {
				HerokuServer.create(Integer.parseInt(port));
			} catch (NumberFormatException | IOException e) {
				LOGGER.error(e.getMessage(), e);
			}
		} else {
			LOGGER.info("Not running on Heroku");
		}

		CommandClient.create(client);

		client.onDisconnect().block();
	}

}
