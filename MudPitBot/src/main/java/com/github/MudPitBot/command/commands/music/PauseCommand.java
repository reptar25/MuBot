package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
public class PauseCommand extends Command {

	public PauseCommand() {
		super("pause");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> {
			return getScheduler(channel).flatMap(scheduler -> {
				return pause(scheduler);
			});
		});
	}

	/**
	 * Pauses/unpauses the player
	 * 
	 * @param event The message event
	 * @return null
	 */
	public Mono<CommandResponse> pause(TrackScheduler scheduler) {
		if (scheduler != null)
			scheduler.pause(!scheduler.isPaused());

		return CommandResponse.empty();
	}
}
