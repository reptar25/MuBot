package com.github.MudPitBot.botCommand.commandInterface;

import com.github.MudPitBot.botCommand.CommandReceiver;
import com.github.MudPitBot.botCommand.sound.TrackScheduler;

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
	 * The String that would cause this command to trigger if typed in a message to
	 * a channel the bot can see
	 * 
	 * @return the literal String of what triggers this command.
	 */
	/*
	 * This enforces users to implement what the command trigger should be when
	 * making a subclass. If we just used a protected variable then there would be
	 * no way to enforce it being set.
	 */
	public abstract String getCommandTrigger();

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
		if (event != null) {
			if (event.getClient() != null) {
				if (event.getGuildId() != null) {
					// MessageChannel messageChannel = event.getMessage().getChannel().block();
					Snowflake guildId = event.getGuildId().orElse(null);
					if (guildId != null) {
						VoiceState vs = event.getClient().getSelf().block().asMember(guildId).block().getVoiceState()
								.block();
						if (vs != null) {
							Snowflake channelId = null;
							channelId = vs.getChannelId().orElse(null);
							if (channelId != null) {
								scheduler = CommandReceiver.getScheduler(channelId);
							}
						}
					}
				}
			}
		}
		return scheduler;
	}
}
