package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.Permissions.requireBotPermissions;
import static com.github.MudPitBot.command.util.Permissions.requireSameVoiceChannel;

import java.util.function.Consumer;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.help.CommandHelpSpec;
import com.github.MudPitBot.music.GuildMusicManager;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.NonNull;

public class PlayCommand extends Command {

	private static final Logger LOGGER = Loggers.getLogger(PlayCommand.class);

	public PlayCommand() {
		super("play");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event)
				.flatMap(channel -> requireBotPermissions(channel, Permission.SPEAK).thenReturn(channel))
				.flatMap(channel -> GuildMusicManager.getScheduler(channel))
				.flatMap(scheduler -> play(event, scheduler, args));
	}

	/**
	 * Attempts to play the link in the message
	 * 
	 * @param event The message event
	 * @param args  The link of the audio
	 * @return null
	 */
	public Mono<CommandResponse> play(MessageCreateEvent event, @NonNull TrackScheduler scheduler,
			@NonNull String[] args) {
		// unpause
		if (args.length == 0 || args[0].isEmpty()) {
			if (scheduler.getNowPlaying() != null)
				scheduler.pause(!scheduler.isPaused());
			return CommandResponse.empty();
		}

		if (args.length <= 0 || args[0].isEmpty()) {
			return CommandResponse.empty();
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
