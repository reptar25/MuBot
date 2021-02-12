package com.github.mubot.command.commands.music;

import java.util.List;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;

public abstract class TrackSeekingCommand extends MusicCommand {

	public TrackSeekingCommand(String commandTrigger) {
		super(commandTrigger);
	}

	public TrackSeekingCommand(String commandTrigger, List<String> aliases) {
		super(commandTrigger, aliases);
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		if (args.length > 0) {
			try {
				int amountInSeconds = Integer.parseInt(args[0]);
				this.doSeeking(scheduler, amountInSeconds);
				return Mono.empty();
			} catch (NumberFormatException e) {
				// just ignore commands with improper number
				return Mono.empty();
			}
		}
		return getHelp(event);
	}

	protected abstract void doSeeking(TrackScheduler scheduler, int amountInSeconds);

}
