package com.github.mubot.command.commands.music;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.command.util.CommandUtil;
import com.github.mubot.command.util.EmojiHelper;
import com.github.mubot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class NowPlayingCommand extends MusicCommand {

	public NowPlayingCommand() {
		super("nowplaying", Arrays.asList("np", "playing"));
	}
	
	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return nowPlaying(scheduler);
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
			String response = EmojiHelper.NOTES + " Now playing " + CommandUtil.trackInfoWithCurrentTime(track) + " "
					+ EmojiHelper.NOTES;
			return CommandResponse.create(response);
		}
		return CommandResponse.create("No track is currently playing");
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Displays currently playing song.");
	}

}
