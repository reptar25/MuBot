package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.PlayerManager;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class PlayCommand extends Command {

	private static final Logger LOGGER = Loggers.getLogger(PlayCommand.class);

	public PlayCommand() {
		super("play");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> {
			return getScheduler(channel).flatMap(scheduler -> {
				return play(scheduler, params);
			});
		});
	}

	/**
	 * Attempts to play the link in the message
	 * 
	 * @param event  The message event
	 * @param params The link of the audio
	 * @return null
	 */
	public Mono<CommandResponse> play(TrackScheduler scheduler, String[] params) {
		if (scheduler != null && params != null) {
			// unpause
			if (params.length == 0 || params[0].isEmpty()) {
				if (scheduler.getNowPlaying() != null)
					scheduler.pause(!scheduler.isPaused());
				return Mono.empty();
			}

			if (params.length <= 0 || params.length > 1 || params[0].isEmpty()) {
				// LOGGER.error("Too many or few params for play");
				return Mono.empty();
			}
			PlayerManager.loadItem(params[0], scheduler);
			if (!scheduler.getQueue().isEmpty()) {
				return Mono.just(new CommandResponse(
						"New track added to the queue (#" + (scheduler.getQueue().size() + 1) + ")"));
			}
			LOGGER.info("Loaded music item: " + params[0]);
		}
		return Mono.empty();
	}

}
