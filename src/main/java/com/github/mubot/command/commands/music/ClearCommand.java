package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.GuildMusicManager;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class ClearCommand extends Command {

	public ClearCommand() {
		super("clear");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
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

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Clears the queue of all songs.");
	}

}
