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

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class SkipCommand extends Command {

	public SkipCommand() {
		super("skip");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> GuildMusicManager.getScheduler(channel))
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
							EmojiHelper.NEXT_TRACK + " Skipping to " + scheduler.skipQueue(element) + " " + EmojiHelper.NEXT_TRACK);
				} catch (NumberFormatException ignored) {
					// if there isn't an int as the args than just skip the current song
				}
			}

			String response = EmojiHelper.NEXT_TRACK + " Skipping " + CommandUtil.trackInfo(scheduler.getNowPlaying()) + " "
					+ EmojiHelper.NEXT_TRACK;
			scheduler.nextTrack();
			return CommandResponse.create(response);
		} else {
			return CommandResponse.create("No song is currently playing");
		}
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Skips the currently playing song and plays the next song in the queue.");
	}
}
