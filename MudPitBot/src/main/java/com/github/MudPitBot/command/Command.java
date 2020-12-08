package com.github.MudPitBot.command;

import java.util.Optional;

import com.github.MudPitBot.CommandCore.CommandReceiver;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;

/**
 * Implementation of the Command design pattern.
 */

public abstract class Command implements CommandInterface {

	protected CommandReceiver receiver;
	protected String commandTrigger;

	public Command(CommandReceiver receiver) {
		this.receiver = receiver;
	}

	private final static int MAX_RETRIES = 15;

	/**
	 * Gets the {@link TrackScheduler} that was mapped when the bot joined a voice
	 * channel of the guild the message was sent in.
	 * 
	 * @param event The message event
	 * @return The {@link TrackScheduler} that is mapped to the voice channel of the
	 *         bot in the guild the message was sent from.
	 */

	// retries allow commands to still work while bot is joining channel and setting
	// up scheduler eg "!join !play"
	protected static TrackScheduler getScheduler(MessageCreateEvent event) {
		int retries = 0;
		TrackScheduler scheduler = null;
		// MessageChannel messageChannel = event.getMessage().getChannel().block();
		while (scheduler == null && retries <= MAX_RETRIES)
			if (event.getGuildId().isPresent()) {
				Snowflake guildId = event.getGuildId().get();
				Optional<Snowflake> channelIdSnowflake = event.getClient().getSelf()
						.flatMap(user -> user.asMember(guildId)).flatMap(Member::getVoiceState)
						.map(VoiceState::getChannelId).block();
				if (channelIdSnowflake != null) {
					if (channelIdSnowflake.isPresent()) {
						scheduler = TrackScheduler.getScheduler(channelIdSnowflake.get());
					}
				}
				if (scheduler == null) {
					try {
						Thread.sleep(100);
						retries++;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		return scheduler;
	}
}
