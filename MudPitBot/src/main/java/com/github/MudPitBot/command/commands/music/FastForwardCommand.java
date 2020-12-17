package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class FastForwardCommand extends Command {

	public FastForwardCommand() {
		super("fastforward");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel))
				.flatMap(scheduler -> fastForward(scheduler, params));
	}

	/**
	 * @param event  The message event
	 * @param params The amount of time in seconds to fast forward
	 * @return null
	 */
	public Mono<CommandResponse> fastForward(TrackScheduler scheduler, String[] params) {
		if (scheduler != null && params != null) {
			if (params.length > 0) {
				try {
					int amountInSeconds = Integer.parseInt(params[0]);
					scheduler.fastForward(amountInSeconds);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return CommandResponse.empty();
	}

}
