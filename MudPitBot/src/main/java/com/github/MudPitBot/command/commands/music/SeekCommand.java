package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class SeekCommand extends Command {

	public SeekCommand() {
		super("seek");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel))
				.flatMap(scheduler -> seek(scheduler, args));
	}

	/**
	 * @param event  The message event
	 * @param args The position to move the current song to in seconds
	 * @return null
	 */
	public Mono<CommandResponse> seek(TrackScheduler scheduler, String[] args) {
		if (scheduler != null && args != null) {
			if (args.length > 0) {
				try {
					int positionInSeconds = Integer.parseInt(args[0]);
					scheduler.seek(positionInSeconds);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return CommandResponse.empty();
	}

}
