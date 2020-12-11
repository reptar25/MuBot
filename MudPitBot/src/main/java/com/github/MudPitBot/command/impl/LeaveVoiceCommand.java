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
import discord4j.voice.VoiceConnection;
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
		return leave(event);
	}

	/**
	 * Bot leaves any voice channel it is connected to in the same guild. Also
	 * clears the queue of items.
	 * 
	 * @param event The message event
	 * @return null
	 */
	public Mono<CommandResponse> leave(MessageCreateEvent event) {
		// get the voice channel the bot is connected to
		return event.getMessage().getGuild().flatMap(Guild::getVoiceConnection).flatMap(botConnection -> {
			// get the channel id of the bot's voice connection
			return botConnection.getChannelId().flatMap(botChannelId -> {
				// get member who sent the command voice channel
				return Mono.justOrEmpty(event.getMember()).flatMap(Member::getVoiceState)
						.flatMap(VoiceState::getChannel).map(VoiceChannel::getId).flatMap(memberChannelId -> {
							// if the members voice channel is one the bot is in
							if (memberChannelId.equals(botChannelId)) {
								TrackScheduler.remove(memberChannelId);
								return botConnection.disconnect();
							}
							return Mono.empty();
						});
			});
		}).then(Mono.empty());
	}

}
