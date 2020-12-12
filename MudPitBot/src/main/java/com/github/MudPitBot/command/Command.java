package com.github.MudPitBot.command;

import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
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
	protected static Mono<TrackScheduler> getScheduler(MessageCreateEvent event) {
		// MessageChannel messageChannel = event.getMessage().getChannel().block();
		return Mono.justOrEmpty(event.getGuildId()).flatMap(guildId -> {
			return event.getClient().getMemberById(guildId, event.getClient().getSelfId()).flatMap(Member::getVoiceState)
					.map(VoiceState::getChannelId).flatMap(s -> Mono.justOrEmpty(s.get())).flatMap(channelId -> {
						Mono<TrackScheduler> scheduler = Mono.justOrEmpty(TrackScheduler.getScheduler(channelId))
								.repeatWhenEmpty(Integer.MAX_VALUE, Flux::repeat);
						return scheduler;
					});
		});
	}
}
