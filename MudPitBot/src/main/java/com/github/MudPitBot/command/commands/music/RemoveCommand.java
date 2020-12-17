package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.util.Emoji;
import com.github.MudPitBot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class RemoveCommand extends Command {

	public RemoveCommand() {
		super("remove");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> getScheduler(channel))
				.flatMap(scheduler -> remove(scheduler, params));
	}

	public Mono<CommandResponse> remove(TrackScheduler scheduler, String[] params) {
		if (scheduler != null) {
			if (params != null && params.length >= 1) {
				try {
					int index = Integer.parseInt(params[0]);
					AudioTrack removed = scheduler.removeFromQueue(index - 1);
					if (removed != null)
						return CommandResponse.create(Emoji.RED_X + " Removed \"" + removed.getInfo().title
								+ "\" from the queue " + Emoji.RED_X);
					else
						return CommandResponse.empty();
				} catch (NumberFormatException ignored) {
					return CommandResponse.empty();
				}
			}
		}
		return CommandResponse.empty();
	}

}
