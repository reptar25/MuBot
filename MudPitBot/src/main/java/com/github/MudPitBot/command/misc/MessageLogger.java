package com.github.MudPitBot.command.misc;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
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
			client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(event -> {
				final String content = event.getMessage().getContent();

				// print out new message to logs
				logMessage(event, content);
			});
		}
	}

	/**
	 * Formats and logs the message content and author
	 * 
	 * @param event   even of the message
	 * @param content content of the message
	 */
	public void logMessage(MessageCreateEvent event, String content) {
		Mono.just(event.getMessage()).map(Message::getAuthor).filter(user -> user.isPresent()).subscribe(userOpt -> {
			Mono.just(event.getGuild()).flatMap(g -> g).map(Guild::getName).subscribe(guildName -> {
				StringBuilder sb = new StringBuilder("New message created: ");
				sb.append("(").append(guildName).append(") ");
				sb.append(userOpt.get().getUsername());
				sb.append(" - \"").append(content).append("\"");
				LOGGER.info(sb.toString());
			});
		});
	}

}
