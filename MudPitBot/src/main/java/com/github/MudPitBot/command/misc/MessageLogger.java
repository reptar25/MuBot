package com.github.MudPitBot.command.misc;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * This class will log any messages that the can see
 */
public class MessageLogger {

	private static MessageLogger instance;
	private GatewayDiscordClient client;
	private static final Logger LOGGER = Loggers.getLogger(MessageLogger.class);

	static public MessageLogger create(GatewayDiscordClient client) {
		if (instance == null)
			instance = new MessageLogger(client);

		return instance;
	}

	private MessageLogger(GatewayDiscordClient client) {
		this.client = client;

		setupListener();
	}

	/**
	 * setup listener on event dispatcher for message create events
	 */
	private void setupListener() {
		if (client.getEventDispatcher() != null) {
			client.getEventDispatcher().on(MessageCreateEvent.class)
					// ignore messages from bots
					.filter(event -> event.getMessage().getAuthor().map(User::isBot).orElse(true)).subscribe(event -> {
						final String content = event.getMessage().getContent();
						// print out new message to logs
						logMessage(event, content).subscribe(null, error -> LOGGER.error(error.getMessage(), error));
					});
		}
	}

	/**
	 * Formats and logs the message content and author
	 * 
	 * @param event   even of the message
	 * @param content content of the message
	 * @return
	 */
	public Mono<Object> logMessage(MessageCreateEvent event, String content) {
		return Mono.justOrEmpty(event.getMessage().getAuthor()).flatMap(user -> {
			return event.getGuild().map(Guild::getName).flatMap(guildName -> {
				StringBuilder sb = new StringBuilder("New message: ");
				sb.append("(").append(guildName).append(") ");
				sb.append(user.getUsername());
				sb.append(" - \"").append(content).append("\"");
				LOGGER.info(sb.toString());

				return Mono.empty();
			});
		});
	}

}
