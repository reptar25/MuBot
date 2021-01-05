package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class RewindCommand extends Command {

	public RewindCommand() {
		super("rewind");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel))
				.flatMap(scheduler -> rewind(scheduler, args));
	}

	/**
	 * @param event The message event
	 * @param args  The amount of time in seconds to rewind
	 * @return null
	 */
	public Mono<CommandResponse> rewind(@NonNull TrackScheduler scheduler, @NonNull String[] args) {
		if (args.length > 0) {
			try {
				int amountInSeconds = Integer.parseInt(args[0]);
				scheduler.rewind(amountInSeconds);
			} catch (NumberFormatException e) {
				// just ignore commands with improper number
			}
		}
		return CommandResponse.empty();
	}

}
