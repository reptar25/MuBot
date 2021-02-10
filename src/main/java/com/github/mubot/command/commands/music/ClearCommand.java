package com.github.mubot.command.commands.music;

import java.util.function.Consumer;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class ClearCommand extends MusicCommand {

	public ClearCommand() {
		super("clear");
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return clearQueue(scheduler);
	}


	/**
	 * Clears the current queue of all tracks
	 * @param scheduler the track scheduler to clear
	 * @return
	 */
	public Mono<CommandResponse> clearQueue(@NonNull TrackScheduler scheduler) {
		scheduler.clearQueue();
		return CommandResponse.create("Queue cleared");
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Clears the queue of all songs.");
	}

}
