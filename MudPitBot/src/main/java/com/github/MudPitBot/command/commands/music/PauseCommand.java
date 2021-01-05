package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class PauseCommand extends Command {

	public PauseCommand() {
		super("pause");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel))
				.flatMap(scheduler -> pause(scheduler));
	}

	/**
	 * Pauses/unpauses the player
	 * 
	 * @param event The message event
	 * @return null
	 */
	public Mono<CommandResponse> pause(@NonNull TrackScheduler scheduler) {
		scheduler.pause(!scheduler.isPaused());
		return CommandResponse.empty();
	}
}
