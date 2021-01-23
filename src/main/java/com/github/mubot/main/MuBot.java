package com.github.mubot.main;

import java.io.IOException;

import com.github.mubot.database.DatabaseManager;
import com.github.mubot.eventlistener.CommandListener;
import com.github.mubot.eventlistener.EventListener;
import com.github.mubot.eventlistener.GuildCreateListener;
import com.github.mubot.eventlistener.GuildDeleteListener;
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

	GatewayDiscordClient client;

	public MuBot(GatewayDiscordClient client) {
		this.client = client;
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
			registerListener(new MessageLogger());
		}

		DatabaseManager.create();
		if (client.getEventDispatcher() != null) {
			registerListener(new ReadyListener());
			registerListener(new VoiceStateUpdateListener());
			registerListener(new CommandListener());
			registerListener(new MuteOnJoinListener());
			registerListener(new GuildCreateListener());
			registerListener(new GuildDeleteListener());
		}

		// ReadyListener.create(client);
		// VoiceStateUpdateListener.create(client);
		// CommandListener.create(client);
		// MuteOnJoinListener.create(client);
	}

	private <T extends Event> void registerListener(EventListener<T> listener) {
		client.getEventDispatcher().on(listener.getEventType()).flatMap(listener::consume).subscribe(null,
				error -> LOGGER.error(error.getMessage(), error));
	}

}
