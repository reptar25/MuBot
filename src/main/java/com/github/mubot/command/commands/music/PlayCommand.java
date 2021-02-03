package com.github.mubot.command.commands.music;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.GuildMusicManager;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.NonNull;

public class PlayCommand extends MusicPermissionCommand {

	private static final Logger LOGGER = Loggers.getLogger(PlayCommand.class);

	public PlayCommand() {
		super("play", Arrays.asList("p", "add"), Permission.SPEAK);
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return play(event, args, scheduler);
	}

	/**
	 * Attempts to play the link in the message
	 * 
	 * @param event The message event
	 * @param args  The link of the audio
	 * @return null
	 */
	public Mono<CommandResponse> play(MessageCreateEvent event, @NonNull String[] args,
			@NonNull TrackScheduler scheduler) {
		// unpause
		if (args.length == 0 || args[0].isEmpty()) {
			if (scheduler.getNowPlaying() != null) {
				scheduler.pause(!scheduler.isPaused());
				return CommandResponse.empty();
			}
			return getHelp(event);

		}

		if (args.length <= 0 || args[0].isEmpty()) {
			return getHelp(event);
		}

		// if its a search recombine the args that were split by space
		if (args[0].startsWith("ytsearch:"))
			args[0] = recombineArgs(args);

		GuildMusicManager.loadItemOrdered(args[0], scheduler, event);
		LOGGER.info("Loaded music item: " + args[0]);
		return CommandResponse.empty();
	}

	private String recombineArgs(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (String param : args) {
			sb.append(param.trim()).append(" ");
		}
		return sb.toString();
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec
				.setDescription("Plays the song(s) from the given url.").addArg("url",
						"Url of the song/playlist to be played from YouTube/SoundCloud/Bandcamp/Twitch/ect.", false)
				.addExample("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
	}

}
