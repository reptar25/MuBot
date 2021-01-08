package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.Permissions.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.util.CommandUtil;
import com.github.MudPitBot.command.util.Emoji;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class SkipCommand extends Command {

	public SkipCommand() {
		super("skip");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel))
				.flatMap(scheduler -> skip(scheduler, args));
	}

	/**
	 * Stops the current song and plays the next in queue if there is any
	 * 
	 * @param event The message event
	 * @return The message event
	 */
	public Mono<CommandResponse> skip(@NonNull TrackScheduler scheduler, @NonNull String[] args) {
		if (scheduler.getNowPlaying() != null) {

			if (args.length > 0 && !args[0].isBlank()) {
				try {
					int element = Integer.parseInt(args[0]);
					return CommandResponse.create(
							Emoji.NEXT_TRACK + " Skipping to " + scheduler.skipQueue(element) + " " + Emoji.NEXT_TRACK);
				} catch (NumberFormatException ignored) {
					// if there isn't an int as the args than just skip the current song
				}
			}

			String response = Emoji.NEXT_TRACK + " Skipping " + CommandUtil.trackInfoString(scheduler.getNowPlaying())
					+ " " + Emoji.NEXT_TRACK;
			scheduler.nextTrack();
			return CommandResponse.create(response);
		} else {
			return CommandResponse.create("No song is currently playing");
		}
	}

	@Override
	public Mono<CommandResponse> getHelp() {
		return createCommandHelpEmbed(
				s -> s.setDescription("Skips the currently playing song and plays the next song in the queue."));
	}
}
