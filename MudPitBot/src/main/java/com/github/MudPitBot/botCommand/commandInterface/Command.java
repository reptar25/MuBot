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
	 * This enforces users to implement what the command trigger should be when
	 * making a subclass. If we just used a protected variable then there would be
	 * no way to enforce it being set. Command names should always be in lower-case
	 * here since we do .toLowerCase() when checking the command to make them non
	 * case sensitive.
	 * 
	 * @return the literal String of what triggers this command.
	 */
	public abstract String getCommandTrigger();

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
