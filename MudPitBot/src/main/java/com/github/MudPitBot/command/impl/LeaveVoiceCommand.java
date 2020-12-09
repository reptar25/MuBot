package com.github.MudPitBot.command.impl;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.sound.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
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
	public CommandResponse execute(MessageCreateEvent event, String[] params) {
		return leave(event);
	}

	/**
	 * Bot leaves any voice channel it is connected to in the same guild. Also
	 * clears the queue of items.
	 * 
	 * @param event The message event
	 * @return null
	 */
	public CommandResponse leave(MessageCreateEvent event) {
		// get the voice channel the bot is connected to
		Mono.just(event.getMessage()).flatMap(Message::getGuild).flatMap(Guild::getVoiceConnection)
				.flatMap(botConnection -> {
					return Mono.just(botConnection.getChannelId().flatMap(botChannelId -> {
						// get member who sent the command voice channel
						return Mono.justOrEmpty(event.getMember()).flatMap(Member::getVoiceState)
								.flatMap(VoiceState::getChannel).map(VoiceChannel::getId).flatMap(memberChannelId -> {
									// if the members voice channel is one the bot is in
									if (memberChannelId.equals(botChannelId)) {
										botConnection.disconnect().subscribe(null,
												error -> LOGGER.error(error.getMessage()));
										TrackScheduler.remove(memberChannelId);
									}
									return Mono.empty();
								});
					}));
				}).subscribe(null, error -> LOGGER.error(error.getMessage(), error));
		return null;
	}

}
