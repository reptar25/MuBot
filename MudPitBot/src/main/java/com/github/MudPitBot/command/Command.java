package com.github.MudPitBot.command;

import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementation of the Command design pattern.
 */

public abstract class Command implements CommandInterface {

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

	protected static Mono<TrackScheduler> getScheduler(MessageCreateEvent event) {
		// MessageChannel messageChannel = event.getMessage().getChannel().block();
		return Mono.justOrEmpty(event.getGuildId()).flatMap(guildId -> {
			return event.getClient().getMemberById(guildId, event.getClient().getSelfId())
					.flatMap(Member::getVoiceState).flatMap(s -> Mono.justOrEmpty(s.getChannelId()))
					.flatMap(channelId -> {
						// repeat until we find the scheduler
						// allows things like !join !play to work
						return Mono.justOrEmpty(TrackScheduler.getScheduler(channelId.asLong()))
								.repeatWhenEmpty(RETRY_AMOUNT, Flux::repeat);
					});
		});
	}
}
