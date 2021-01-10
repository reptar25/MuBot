package com.github.mudpitbot.eventlistener;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
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
					.filter(event -> !event.getMessage().getAuthor().map(User::isBot).orElse(true)).flatMap(event -> {
						final String content = event.getMessage().getContent();
						// print out new message to logs
						return logMessage(event, content);
					}).subscribe(null, error -> LOGGER.error(error.getMessage(), error));
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
		Mono<User> getUser = Mono.justOrEmpty(event.getMessage().getAuthor()).defaultIfEmpty(null);
		Mono<String> getGuildName = event.getGuild().map(Guild::getName).defaultIfEmpty("Private Message");
		Mono<String> getGuildChannelName = event.getMessage().getChannel().flatMap(channel -> {
			if (channel instanceof PrivateChannel)
				return Mono.empty();
			return Mono.just((GuildChannel) channel);
		}).map(GuildChannel::getName).defaultIfEmpty("Private Message");

		return Mono.zip(getUser, getGuildName, getGuildChannelName).map(tuple -> {
			User user = tuple.getT1();
			String guildName = tuple.getT2();
			String channelName = tuple.getT3();
			String username;

			if (user == null)
				username = "Uknown Author";
			else
				username = user.getUsername();

			StringBuilder sb = new StringBuilder("New message: ");
			sb.append("(").append(guildName).append(")");
			sb.append("(").append(channelName).append(")");
			sb.append(" ").append(username);
			sb.append(" - \"").append(content).append("\"");
			LOGGER.info(sb.toString());
			return Mono.empty();
		});
	}
}
