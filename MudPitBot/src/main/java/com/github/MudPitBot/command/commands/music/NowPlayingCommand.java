package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.util.Emoji;
import com.github.MudPitBot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class NowPlayingCommand extends Command {

	public NowPlayingCommand() {
		super("nowplaying");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel))
				.flatMap(scheduler -> nowPlaying(scheduler));
	}

	/**
	 * Return the info for the currently playing song
	 * 
	 * @param event The message event
	 * @return Info of song currently playing
	 */
	public Mono<CommandResponse> nowPlaying(@NonNull TrackScheduler scheduler) {
		// get the track that's currently playing
		AudioTrack track = scheduler.getNowPlaying();
		if (track != null) {
			String response = Emoji.NOTES + " Now playing **" + track.getInfo().title + "** by "
					+ track.getInfo().author + " " + Emoji.NOTES;
			return CommandResponse.create(response);
		}
		return CommandResponse.create("No track is currently playing");
	}

}
