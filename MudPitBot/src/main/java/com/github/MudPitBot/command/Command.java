package com.github.MudPitBot.command;

import com.github.MudPitBot.music.GuildMusicManager;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.object.entity.channel.VoiceChannel;
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

	protected static Mono<TrackScheduler> getScheduler(VoiceChannel channel) {
		return Mono.justOrEmpty(GuildMusicManager.getScheduler(channel.getGuildId().asLong())).repeatWhenEmpty(RETRY_AMOUNT,
				Flux::repeat);
	}



}
