package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.Permissions.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class ClearCommand extends Command {

	public ClearCommand() {
		super("clear");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel))
				.flatMap(scheduler -> clearQueue(scheduler));
	}

	/**
	 * Clears the current queue of all objects
	 * 
	 * @param event The message event
	 * @return "Queue cleared" if successful, null if not
	 */
	public Mono<CommandResponse> clearQueue(@NonNull TrackScheduler scheduler) {
		scheduler.clearQueue();
		return CommandResponse.create("Queue cleared");
	}

	public Mono<CommandResponse> getHelp() {
		return createCommandHelpEmbed(s -> s.setDescription("Clears the queue of all songs."));
	}

}
