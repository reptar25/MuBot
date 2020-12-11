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
		return mute(event);
	}

	/**
	 * Mutes all {@link Member} in the channel besides bots
	 * 
	 * @param event The message event
	 * @return null
	 */

	public Mono<CommandResponse> mute(MessageCreateEvent event) {
		/*
		 * gets the member's channel who sent the message, and then all the VoiceStates
		 * connected to that channel. From there we can get the Member of the VoiceState
		 */
		Mono.justOrEmpty(event.getMember()).map(Member::getGuildId).flatMap(guildId -> {
			return Mono.justOrEmpty(event.getMember()).flatMap(Member::getVoiceState).flatMap(VoiceState::getChannel)
					.map(VoiceChannel::getVoiceStates).flatMap(users -> {
						// gets the channel id of the member if present
						return Mono.justOrEmpty(event.getMember()).flatMap(Member::getVoiceState)
								.map(VoiceState::getChannelId).filter(id -> id.isPresent()).flatMap(idOpt -> {
									boolean mute = true;
									Snowflake id = idOpt.get();
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

									if (mute)
										users.flatMap(VoiceState::getMember).filter(Predicate.not(Member::isBot))
												.subscribe(member -> {
													LOGGER.info(new StringBuilder("Muting ")
															.append(member.getUsername()).toString());
													member.edit(spec -> spec.setMute(true)).subscribe();
												});
									else
										users.flatMap(VoiceState::getMember).filter(Predicate.not(Member::isBot))
												.subscribe(member -> {
													LOGGER.info(new StringBuilder("Unmuting ")
															.append(member.getUsername()).toString());
													member.edit(spec -> spec.setMute(false)).subscribe();
												});
									return Mono.empty();
								});
					});
		}).subscribe(null, error -> LOGGER.error(error.getMessage(), error));

		return Mono.empty();
	}

}
