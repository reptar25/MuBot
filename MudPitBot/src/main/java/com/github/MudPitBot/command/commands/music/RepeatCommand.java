package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.util.Emoji;
import com.github.MudPitBot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class RepeatCommand extends Command {

	public RepeatCommand() {
		super("repeat");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> {
			return getScheduler(channel).flatMap(scheduler -> {
				return loop(scheduler);
			});
		});
	}

	private Mono<CommandResponse> loop(TrackScheduler scheduler) {
		boolean repeatEnabled = scheduler.repeatEnabled();
		String response;

		if (repeatEnabled)
			response = Emoji.NO_ENTRY + " Repeat disabled " + Emoji.NO_ENTRY;
		else
			response = Emoji.REPEAT + " Repeat enabled " + Emoji.REPEAT;

		scheduler.setRepeat(!repeatEnabled);
		return CommandResponse.create(response);
	}

}