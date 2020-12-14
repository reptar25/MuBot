package com.github.MudPitBot.command.impl;

import java.util.ArrayList;
import java.util.function.Predicate;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.misc.MuteHelper;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class MuteCommand extends Command {

	private static final Logger LOGGER = Loggers.getLogger(StopCommand.class);

	public MuteCommand() {
		super("mute");
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] params) {
		return requireVoiceChannel(event).flatMap(channel -> mute(channel));
	}

	/**
	 * Mutes all {@link Member} in the channel besides bots
	 * 
	 * @param event The message event
	 * @return null
	 */

	public Mono<CommandResponse> mute(VoiceChannel channel) {
		/*
		 * gets the member's channel who sent the message, and then all the VoiceStates
		 * connected to that channel. From there we can get the Member of the VoiceState
		 */
		StringBuilder sb = new StringBuilder();
		final Snowflake guildId = channel.getGuildId();
		final Snowflake id = channel.getId();
		Flux<VoiceState> users = channel.getVoiceStates();

		boolean mute = true;
		ArrayList<Snowflake> channelIds = MuteHelper.mutedChannels.get(guildId);
		if (channelIds != null) {
			// channel is muted, so unmute
			if (channelIds.contains(id)) {
				mute = false;
				channelIds.remove(id);
			} else {
				channelIds.add(id);
			}
		} else {
			// channel should be muted
			ArrayList<Snowflake> ids = new ArrayList<Snowflake>();
			ids.add(id);
			MuteHelper.mutedChannels.put(guildId, ids);
		}

		if (mute) {
			users.flatMap(VoiceState::getMember).filter(Predicate.not(Member::isBot)).flatMap(member -> {
				LOGGER.info(new StringBuilder("Muting ").append(member.getUsername()).toString());
				return member.edit(spec -> spec.setMute(true));
			}).subscribe();
			sb.append("Muting ");
		} else {
			users.flatMap(VoiceState::getMember).filter(Predicate.not(Member::isBot)).flatMap(member -> {
				LOGGER.info(new StringBuilder("Unmuting ").append(member.getUsername()).toString());
				return member.edit(spec -> spec.setMute(false));
			}).subscribe();
			sb.append("Unmuting ");
		}

		sb.append(channel.getName());
		return Mono.just(new CommandResponse(sb.toString()));
	}

}
