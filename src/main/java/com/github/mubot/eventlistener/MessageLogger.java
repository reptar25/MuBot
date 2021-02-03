package com.github.mubot.eventlistener;

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
public class MessageLogger implements EventListener<MessageCreateEvent> {

	private static final Logger LOGGER = Loggers.getLogger(MessageLogger.class);

	@Override
	public Class<MessageCreateEvent> getEventType() {
		return MessageCreateEvent.class;
	}

	@Override
	public Mono<Void> consume(MessageCreateEvent e) {
		return Mono.just(e).filter(event -> !event.getMessage().getAuthor().map(User::isBot).orElse(true))
				.flatMap(event -> {
					final String content = event.getMessage().getContent();
					// print out new message to logs
					return logMessage(event, content);
				});
	}

	/**
	 * Formats and logs the message content and author
	 * 
	 * @param event   even of the message
	 * @param content content of the message
	 * @return
	 */
	private Mono<Void> logMessage(MessageCreateEvent event, String content) {
		Mono<User> getUser = Mono.justOrEmpty(event.getMessage().getAuthor()).defaultIfEmpty(null);
		Mono<String> getGuildName = event.getGuild().map(Guild::getName).defaultIfEmpty("Private Message");
		Mono<String> getGuildChannelName = event.getMessage().getChannel().flatMap(channel -> {
			if (channel instanceof PrivateChannel)
				return Mono.empty();
			return Mono.just((GuildChannel) channel);
		}).map(GuildChannel::getName).defaultIfEmpty("Private Message");

		return zipMessage(content, getUser, getGuildName, getGuildChannelName);
	}

	private Mono<Void> zipMessage(String content, Mono<User> getUser, Mono<String> getGuildName,
			Mono<String> getGuildChannelName) {

		return Mono.zip(getUser, getGuildName, getGuildChannelName).map(tuple -> {
			User user = tuple.getT1();
			String guildName = tuple.getT2();
			String channelName = tuple.getT3();
			String username;

			username = user == null ? "Uknown Author" : user.getUsername();

			StringBuilder sb = new StringBuilder("New message: ");
			sb.append("(").append(guildName).append(")");
			sb.append("(").append(channelName).append(")");
			sb.append(" ").append(username);
			sb.append(" - \"").append(content).append("\"");
			LOGGER.info(sb.toString());
			return Mono.empty();
		}).then();
	}

}
