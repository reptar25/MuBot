package com.github.mubot.main;

import java.io.IOException;

import com.github.mubot.database.DatabaseManager;
import com.github.mubot.eventlistener.CommandListener;
import com.github.mubot.eventlistener.EventListener;
import com.github.mubot.eventlistener.MessageLogger;
import com.github.mubot.eventlistener.MuteOnJoinListener;
import com.github.mubot.eventlistener.ReadyListener;
import com.github.mubot.eventlistener.VoiceStateUpdateListener;
import com.github.mubot.heroku.HerokuServer;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import reactor.util.Logger;
import reactor.util.Loggers;

public class MuBot {

	private static final Logger LOGGER = Loggers.getLogger(MuBot.class);

	public MuBot(GatewayDiscordClient client) {
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
			// MessageLogger.create(client);
			registerListener(client, new MessageLogger());
		}

		registerListener(client, new ReadyListener());
		registerListener(client, new VoiceStateUpdateListener());
		registerListener(client, new CommandListener());
		registerListener(client, new MuteOnJoinListener());

		DatabaseManager.create();
		// ReadyListener.create(client);
		// VoiceStateUpdateListener.create(client);
		// CommandListener.create(client);
		// MuteOnJoinListener.create(client);
	}

	private <T extends Event> void registerListener(GatewayDiscordClient client, EventListener<T> listener) {
		if (client.getEventDispatcher() != null) {
			client.getEventDispatcher().on(listener.getEventType()).flatMap(listener::consume).subscribe(null,
					error -> LOGGER.error(error.getMessage(), error));
		}

	}

}
