package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class SeekCommand extends Command {

	public SeekCommand() {
		super("seek");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> {
			return getScheduler(channel).flatMap(scheduler -> {
				return seek(scheduler, params);
			});
		});
	}

	/**
	 * @param event  The message event
	 * @param params The position to move the current song to in seconds
	 * @return null
	 */
	public Mono<CommandResponse> seek(TrackScheduler scheduler, String[] params) {
		if (scheduler != null && params != null) {
			if (params.length > 0) {
				try {
					int positionInSeconds = Integer.parseInt(params[0]);
					scheduler.seek(positionInSeconds);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return Mono.empty();
	}

}
