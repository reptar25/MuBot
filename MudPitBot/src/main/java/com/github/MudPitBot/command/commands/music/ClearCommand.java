package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class ClearCommand extends Command {

	public ClearCommand() {
		super("clear");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel)).flatMap(scheduler -> clearQueue(scheduler));
	}

	/**
	 * Clears the current queue of all objects
	 * 
	 * @param event The message event
	 * @return "Queue cleared" if successful, null if not
	 */
	public Mono<CommandResponse> clearQueue(TrackScheduler scheduler) {
		if (scheduler != null) {
			scheduler.clearQueue();
			return CommandResponse.create("Queue cleared");
		}

		return CommandResponse.empty();
	}

}
