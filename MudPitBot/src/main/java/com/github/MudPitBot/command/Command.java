package com.github.MudPitBot.command;

import com.github.MudPitBot.core.CommandReceiver;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;

/**
 * Implementation of the Command design pattern.
 */

public abstract class Command implements CommandInterface {

	protected CommandReceiver receiver;
	protected String commandTrigger;

	public Command(CommandReceiver receiver) {
		this.receiver = receiver;
	}

	/**
	 * Gets the {@link TrackScheduler} that was mapped when the bot joined a voice
	 * channel of the guild the message was sent in.
	 * 
	 * @param event The message event
	 * @return The {@link TrackScheduler} that is mapped to the voice channel of the
	 *         bot in the guild the message was sent from.
	 */
	protected static TrackScheduler getScheduler(MessageCreateEvent event) {
		TrackScheduler scheduler = null;
		// MessageChannel messageChannel = event.getMessage().getChannel().block();
		if (event.getGuildId().isPresent()) {
			Snowflake guildId = event.getGuildId().get();
			VoiceState vs = event.getClient().getSelf().block().asMember(guildId).block().getVoiceState().block();
			if (vs != null) {
				if (vs.getChannelId().isPresent()) {
						scheduler = CommandReceiver.getScheduler(vs.getChannelId().get());
				}
			}
		}

		return scheduler;
	}
}
