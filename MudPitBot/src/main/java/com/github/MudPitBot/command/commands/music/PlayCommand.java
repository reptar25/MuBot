package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.CommandUtil.requireBotPermissions;
import static com.github.MudPitBot.command.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.music.GuildMusicManager;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class PlayCommand extends Command {

	private static final Logger LOGGER = Loggers.getLogger(PlayCommand.class);

	public PlayCommand() {
		super("play");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event)
				.flatMap(channel -> requireBotPermissions(channel, Permission.SPEAK).thenReturn(channel))
				.flatMap(channel -> getScheduler(channel)).flatMap(scheduler -> play(event, scheduler, args));
	}

	/**
	 * Attempts to play the link in the message
	 * 
	 * @param event  The message event
	 * @param args The link of the audio
	 * @return null
	 */
	public Mono<CommandResponse> play(MessageCreateEvent event, TrackScheduler scheduler, String[] args) {
		if (scheduler != null && args != null) {
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

			GuildMusicManager.loadItem(args[0], scheduler, event);
//			if (!scheduler.getQueue().isEmpty() || scheduler.getPlayer().getPlayingTrack() != null) {
//				return CommandResponse
//						.create("New track added to the queue (#" + (scheduler.getQueue().size() + 1) + ")");
//			}
			LOGGER.info("Loaded music item: " + args[0]);
		}
		return CommandResponse.empty();
	}

	private String recombineArgs(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (String param : args) {
			sb.append(param.trim()).append(" ");
		}
		return sb.toString();
	}

}
