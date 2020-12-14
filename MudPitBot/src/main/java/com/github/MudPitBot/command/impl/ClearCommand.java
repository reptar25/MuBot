package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class ClearCommand extends Command {

	public ClearCommand() {
		super("clear");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> {
			return getScheduler(channel).flatMap(scheduler -> {
				return clearQueue(scheduler);
			});
		});
		// getScheduler(event).flatMap(scheduler -> return
		// Mono.just(clearQueue(scheduler));
		// return clearQueue(getScheduler(event));
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
			return Mono.just(new CommandResponse("Queue cleared"));
		}

		return Mono.empty();
	}

}
