package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class RewindCommand extends Command {

	public RewindCommand() {
		super("rewind");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel))
				.flatMap(scheduler -> rewind(scheduler, params));
	}

	/**
	 * @param event  The message event
	 * @param params The amount of time in seconds to rewind
	 * @return null
	 */
	public Mono<CommandResponse> rewind(TrackScheduler scheduler, String[] params) {
		if (scheduler != null && params != null) {
			if (params.length > 0) {
				try {
					int amountInSeconds = Integer.parseInt(params[0]);
					scheduler.rewind(amountInSeconds);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return CommandResponse.empty();
	}

}
