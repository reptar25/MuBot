package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.util.Emoji;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class SkipCommand extends Command {

	public SkipCommand() {
		super("skip");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> {
			return getScheduler(channel).flatMap(scheduler -> {
				return skip(scheduler);
			});
		});
	}

	/**
	 * Stops the current song and plays the next in queue if there is any
	 * 
	 * @param event The message event
	 * @return The message event
	 */
	public Mono<CommandResponse> skip(TrackScheduler scheduler) {
		if (scheduler != null) {
			if (scheduler.getNowPlaying() != null) {
				String response = Emoji.NEXT_TRACK + " Skipping \"" + scheduler.getNowPlaying().getInfo().title
						+ "\" by " + scheduler.getNowPlaying().getInfo().author + " " + Emoji.NEXT_TRACK;
				scheduler.nextTrack();
				return CommandResponse.create(response);
			} else {
				return CommandResponse.create("No song is currently playing");
			}
		}
		return CommandResponse.empty();
	}
}
