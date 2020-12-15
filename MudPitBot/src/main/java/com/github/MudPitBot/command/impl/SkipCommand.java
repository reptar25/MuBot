package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class SkipCommand extends Command {

	public SkipCommand() {
		super("skip");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> {
			return getScheduler(channel).flatMap(scheduler -> {
				return skip(scheduler);
			});
		});
	}

	/**
	 * Stops the current song and plays the next in queue if there is any
	 * 
	 * @param event The message event
	 * @return The message event
	 */
	public Mono<CommandResponse> skip(TrackScheduler scheduler) {
		if (scheduler != null) {
			StringBuilder sb = new StringBuilder("Skipping ").append(scheduler.getNowPlaying().getInfo().title);
			CommandResponse.create(sb.toString());
			scheduler.nextTrack();
		}

		return CommandResponse.empty();
	}

}
