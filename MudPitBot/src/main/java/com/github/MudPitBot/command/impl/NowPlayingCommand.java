package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class NowPlayingCommand extends Command {

	public NowPlayingCommand() {
		super("nowplaying");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return getScheduler(event).flatMap(scheduler -> {
			return nowPlaying(scheduler);
		});
	}

	/**
	 * Return the info for the currently playing song
	 * 
	 * @param event The message event
	 * @return Info of song currently playing
	 */
	public Mono<CommandResponse> nowPlaying(TrackScheduler scheduler) {
		if (scheduler != null) {
			// get the track that's currently playing
			AudioTrack track = scheduler.getNowPlaying();
			if (track != null) {
				StringBuilder sb = new StringBuilder("Now playing: ");
				// add track title and author
				sb.append("\"").append(track.getInfo().title).append("\"").append(" by ")
						.append(track.getInfo().author);
				return Mono.just(new CommandResponse(sb.toString()));
			}
			return Mono.just(new CommandResponse("No track is currently playing"));
		}
		return Mono.empty();
	}

}
