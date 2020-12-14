package com.github.MudPitBot.command;

import java.util.Optional;

import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * Implementation of the Command design pattern.
 */

public abstract class Command implements CommandInterface {
	private static final Logger LOGGER = Loggers.getLogger(Command.class);
	protected String commandTrigger;

	public Command(String commandTrigger) {
		this.commandTrigger = commandTrigger;
	}

	/**
	 * The String that would cause this command to trigger if typed in a message to
	 * a channel the bot can see
	 * 
	 * @return the literal String of what triggers this command.
	 */
	public String getCommandTrigger() {
		return commandTrigger;
	}

	/**
	 * Gets the {@link TrackScheduler} that was mapped when the bot joined a voice
	 * channel of the guild the message was sent in.
	 * 
	 * @param event The message event
	 * @return The {@link TrackScheduler} that is mapped to the voice channel of the
	 *         bot in the guild the message was sent from.
	 */
	private static final int RETRY_AMOUNT = 100;

	protected static Mono<TrackScheduler> getScheduler(VoiceChannel channel) {
		return Mono.justOrEmpty(TrackScheduler.getScheduler(channel.getId().asLong())).repeatWhenEmpty(RETRY_AMOUNT,
				Flux::repeat);
	}

	/**
	 * Returns the voice channel the message sender is in or empty if they are not
	 * in a voice channel
	 * 
	 * @param event the message event
	 * @return the voice channel of the message sender
	 */
	protected static Mono<VoiceChannel> requireVoiceChannel(MessageCreateEvent event) {
		return Mono.justOrEmpty(event.getMember()).flatMap(Member::getVoiceState).map(VoiceState::getChannelId)
				.defaultIfEmpty(Optional.empty()).flatMap(s -> {
					if (s.isPresent())
						return Mono.just(s.get());

					LOGGER.info("User is not in a voice channel");
					return Mono.empty();
				}).flatMap(event.getClient()::getChannelById).cast(VoiceChannel.class);
	}

	/**
	 * 
	 * @param event the message event
	 * @return the voice channel the bot and message sender share or empty if they
	 *         do not share a voice channel
	 */
	protected static Mono<VoiceChannel> requireSameVoiceChannel(MessageCreateEvent event) {
		// id of the bot's voice channel id or empty
		final Mono<Optional<Snowflake>> getBotVoiceChannelId = event.getClient().getSelf()
				.flatMap(user -> user.asMember(event.getGuildId().orElseThrow())).flatMap(Member::getVoiceState)
				.map(VoiceState::getChannelId).defaultIfEmpty(Optional.empty());

		// id of the user's voice channel id or empty
		final Mono<Optional<Snowflake>> getUserVoiceChannelId = Mono.justOrEmpty(event.getMember())
				.flatMap(Member::getVoiceState).map(VoiceState::getChannelId).defaultIfEmpty(Optional.empty());

		return Mono.zip(getBotVoiceChannelId, getUserVoiceChannelId).map(tuple -> {
			final Optional<Snowflake> botVoiceChannelId = tuple.getT1();
			final Optional<Snowflake> userVoiceChannelId = tuple.getT2();

			// If the user and the bot are not in a voice channel
			if (botVoiceChannelId.isEmpty() && userVoiceChannelId.isEmpty()) {
				LOGGER.info("User and bot are not in a voice channel");
				return Snowflake.of(-1l);
			}

			// If the user and the bot are not in the same voice channel
			if (botVoiceChannelId.isPresent()
					&& !userVoiceChannelId.map(botVoiceChannelId.get()::equals).orElse(false)) {
				LOGGER.info("User and bot are not in the same voice channel");
				return Snowflake.of(-1l);
			}

			return userVoiceChannelId.get();
		}).filter(snowflake -> snowflake.asLong() != -1).flatMap(event.getClient()::getChannelById)
				.cast(VoiceChannel.class);
	}
}
