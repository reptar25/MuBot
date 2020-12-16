package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.util.CommandUtil.requireSameVoiceChannel;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class LeaveVoiceCommand extends Command {

	private static final Logger LOGGER = Loggers.getLogger(LeaveVoiceCommand.class);

	public LeaveVoiceCommand() {
		super("leave");
	};

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireSameVoiceChannel(event).flatMap(channel -> leave(channel));
	}

	/**
	 * Bot leaves any voice channel it is connected to in the same guild. Also
	 * clears the queue of items.
	 * 
	 * @param event The message event
	 * @return null
	 */
	public Mono<CommandResponse> leave(VoiceChannel channel) {
		return channel.getVoiceConnection().flatMap(botVoiceConnection -> {
			LOGGER.info("Leaving channel " + channel.getId().asLong());
			TrackScheduler.removeFromMap(channel.getId().asLong());
			return botVoiceConnection.disconnect();
		}).then(CommandResponse.empty());
	}

}
