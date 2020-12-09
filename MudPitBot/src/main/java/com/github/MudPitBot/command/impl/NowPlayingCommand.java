package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class NowPlayingCommand extends Command {

	public NowPlayingCommand() {
		super("nowplaying");
	}

	@Override
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return nowPlaying(getScheduler(event));
	}

	/**
	 * Return the info for the currently playing song
	 * 
	 * @param event The message event
	 * @return Info of song currently playing
	 */
	public CommandResponse nowPlaying(TrackScheduler scheduler) {
		if (scheduler != null) {
			StringBuilder sb = new StringBuilder("Now playing: ");
			// get the track that's currently playing
			AudioTrack track = scheduler.getNowPlaying();
			if (track != null) {
				// add track title and author
				sb.append("\"").append(track.getInfo().title).append("\"").append(" by ")
						.append(track.getInfo().author);
			}

			return new CommandResponse(sb.toString());
		}
		return null;
	}

}
