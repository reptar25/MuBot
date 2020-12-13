package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class RemoveCommand extends Command {

	public RemoveCommand() {
		super("remove");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return getScheduler(event).flatMap(scheduler -> {
			return remove(scheduler, params);
		});

	}

	public Mono<CommandResponse> remove(TrackScheduler scheduler, String[] params) {
		if (scheduler != null) {
			if (params != null && params.length >= 1) {
				try {
					int index = Integer.parseInt(params[0]);
					AudioTrack removed = scheduler.removeFromQueue(index - 1);
					if (removed != null)
						return Mono
								.just(new CommandResponse("Removed \"" + removed.getInfo().title + "\" from the queue."));
					else
						return Mono.empty();
				} catch (NumberFormatException ignored) {
					return Mono.empty();
				}
			}
		}
		return Mono.empty();
	}

}
