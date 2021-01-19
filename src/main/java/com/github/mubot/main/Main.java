package com.github.mubot.main;

import java.io.IOException;

import com.github.mubot.command.CommandListener;
import com.github.mubot.command.util.MuteHelper;
import com.github.mubot.database.DatabaseManager;
import com.github.mubot.eventlistener.MessageLogger;
import com.github.mubot.eventlistener.ReadyListener;
import com.github.mubot.eventlistener.VoiceStateUpdateListener;
import com.github.mubot.heroku.HerokuServer;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import reactor.util.Logger;
import reactor.util.Loggers;

public class Main {

	private static final Logger LOGGER = Loggers.getLogger(Main.class);

	public static void main(String[] args) {
		String discordApiToken = null;

		discordApiToken = args[0];

		if (discordApiToken == null)
			discordApiToken = System.getenv("token");

		// no token found
		if (discordApiToken == null) {
			LOGGER.error(
					"No Discord api token found. Your Discord api token needs to be first argument or an env var named \"token\"");
			return;
		}

		final GatewayDiscordClient client = DiscordClientBuilder.create(discordApiToken).build().login().block();

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
			// only log messages on the local client
			MessageLogger.create(client);
		}

		DatabaseManager.create();
		ReadyListener.create(client);
		VoiceStateUpdateListener.create(client);
		CommandListener.create(client);
		MuteHelper.create(client);

		LOGGER.info("Bot is ready");

		client.onDisconnect().block();
	}

}
