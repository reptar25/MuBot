package com.github.MudPitBot.command;

import com.github.MudPitBot.core.CommandReceiver;
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

	private final static int maxRetries = 12;

	/**
	 * Gets the {@link TrackScheduler} that was mapped when the bot joined a voice
	 * channel of the guild the message was sent in.
	 * 
	 * @param event The message event
	 * @return The {@link TrackScheduler} that is mapped to the voice channel of the
	 *         bot in the guild the message was sent from.
	 */
	private volatile static TrackScheduler scheduler;
	private volatile static int retries;

	protected static TrackScheduler getScheduler(MessageCreateEvent event) {
		retries = 0;
		scheduler = null;
		// MessageChannel messageChannel = event.getMessage().getChannel().block();
		while (scheduler == null && retries <= maxRetries) {
			if (event.getGuildId().isPresent()) {
				Snowflake guildId = event.getGuildId().get();
				event.getClient().getSelf().flatMap(user -> user.asMember(guildId)).flatMap(Member::getVoiceState)
						.subscribe(vs -> {
							if (vs.getChannelId().isPresent()) {
								scheduler = CommandReceiver.getScheduler(vs.getChannelId().get());
							}
							if (scheduler == null) {
								try {
									Thread.sleep(100);
									System.out.println("scheduler is null, retrying");
									retries++;
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						});
			}
		}
		return scheduler;
	}
}
