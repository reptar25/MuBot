package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class ShuffleCommand extends Command {

	public ShuffleCommand() {
		super("shuffle");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return getScheduler(event).flatMap(scheduler -> {
			return shuffleQueue(scheduler);
		});
	}

	/**
	 * Shuffles the songs currently in the queue
	 * 
	 * @param event The message event
	 * @return null
	 */
	public Mono<CommandResponse> shuffleQueue(TrackScheduler scheduler) {
		if (scheduler != null) {
			scheduler.shuffleQueue();
			return Mono.just(new CommandResponse("Queue shuffled"));
		}
		return Mono.empty();
	}

}
