package com.github.mudpitbot.command.commands.music;

import static com.github.mudpitbot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.function.Consumer;

import com.github.mudpitbot.command.Command;
import com.github.mudpitbot.command.CommandResponse;
import com.github.mudpitbot.command.help.CommandHelpSpec;
import com.github.mudpitbot.command.util.CommandUtil;
import com.github.mudpitbot.command.util.EmojiHelper;
import com.github.mudpitbot.music.GuildMusicManager;
import com.github.mudpitbot.music.TrackScheduler;
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
			String response = EmojiHelper.NOTES + " Now playing " + CommandUtil.trackInfoWithCurrentTime(track) + " " + EmojiHelper.NOTES;
			return CommandResponse.create(response);
		}
		return CommandResponse.create("No track is currently playing");
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Displays currently playing song.");
	}

}
