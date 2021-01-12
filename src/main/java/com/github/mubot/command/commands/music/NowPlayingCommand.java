package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.command.util.CommandUtil;
import com.github.mubot.command.util.EmojiHelper;
import com.github.mubot.music.GuildMusicManager;
import com.github.mubot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class NowPlayingCommand extends Command {

	public NowPlayingCommand() {
		super("nowplaying", Arrays.asList("np", "playing"));
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
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
