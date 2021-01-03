package com.github.MudPitBot.command.util;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.MudPitBot.command.CommandUtil;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

/**
 * Helper class for the mute function
 *
 */
public class MuteHelper {

	/**
	 * Map of guild ids and list of channel ids of channels that should be muted in
	 * that guild
	 */
	public static HashMap<Snowflake, ArrayList<Snowflake>> mutedChannels = new HashMap<Snowflake, ArrayList<Snowflake>>();

	private static MuteHelper instance;
	private GatewayDiscordClient client;
	private static final Logger LOGGER = Loggers.getLogger(MuteHelper.class);

	public static MuteHelper create(GatewayDiscordClient client) {
		if (instance == null)
			instance = new MuteHelper(client);

		return instance;
	}

	private MuteHelper(GatewayDiscordClient client) {
		this.client = client;
		setupMuteListener();
	}

	/*
	 * Add listener for members joining/changing voice channels.
	 */
	private void setupMuteListener() {
		if (client.getEventDispatcher() != null) {
			client.getEventDispatcher().on(VoiceStateUpdateEvent.class)
					// filter out bots' events
					.filterWhen(event -> event.getCurrent().getMember().map(m -> !m.isBot())).flatMap(event -> {
						if (event.isJoinEvent() || event.isMoveEvent() || event.isLeaveEvent()) {
							if (MuteHelper.mutedChannels.containsKey(event.getCurrent().getGuildId())) {
								// Checks whether a member should be muted on joining a voice channel and mutes
								// them if so
								return muteOnJoin(event.getCurrent());
							}
						}
						return Mono.empty();
					}).subscribe(null, error -> LOGGER.error(error.getMessage()));
		}

	}

	/**
	 * Checks if the new voice channel is the channel the bot currently has muted,
	 * and mutes the member if it is
	 * 
	 * @param voiceState event of the channel change
	 * @return
	 */
	private Mono<Void> muteOnJoin(VoiceState voiceState) {
		return voiceState.getChannel()
				.flatMap(voiceChannel -> CommandUtil.requireBotPermissions(voiceChannel, Permission.MUTE_MEMBERS))
				.flatMap(ignored -> voiceState.getChannel().map(VoiceChannel::getId)).flatMap(currentChannelId -> {
					// if user join same channel as mute channel
					if (MuteHelper.mutedChannels.containsKey(voiceState.getGuildId())) {
						if (MuteHelper.mutedChannels.get(voiceState.getGuildId()).contains(currentChannelId)) {
							// mute the member that just joined
							return muteUser(voiceState.getMember());
						} else {
							return unmuteUser(voiceState.getMember(), voiceState.getGuild());
						}
					}
					// if user joined another channel, make sure they arent muted still
					else {
						// We cant unmute a users when they leave because the Discord api doesnt allow
						// modifying a user's server mute if that user isn't in a voice channel. So if
						// that person disconnects from voice then we can't unmute them. For now we just
						// check if it's a non-muted channel when they join and unmute them then. This
						// has the side effect of users staying muted if they are muted by the bot and
						// leave, but rejoin a voice channel when the bot is not running
						return unmuteUser(voiceState.getMember(), voiceState.getGuild());
					}
				});
	}

	/**
	 * Mutes the user of the given VoiceStateUpdateEvent
	 * 
	 * @param memeberMono the VoiceStateUpdateEvent of the user
	 * @return
	 */
	private Mono<Void> muteUser(Mono<Member> memberMono) {
		return memberMono.flatMap(member -> {
			LOGGER.info("Muting " + member.getUsername());
			return member.edit(spec -> spec.setMute(true));
		});
	}

	/**
	 * Unmutes the user of the given VoiceStateUpdateEvent
	 * 
	 * @param event the VoiceStateUpdateEvent of the user
	 * @return
	 */
	private Mono<Void> unmuteUser(Mono<Member> getMember, Mono<Guild> getGuild) {
		Mono<VoiceState> getVoiceState = getMember.flatMap(Member::getVoiceState);

		return Mono.zip(getMember, getGuild, getVoiceState).flatMap(tuple -> {
			Member member = tuple.getT1();
			Guild guild = tuple.getT2();
			VoiceState vs = tuple.getT3();
			if (vs.isMuted()) {
				if (!vs.getChannelId().orElse(null).equals(guild.getAfkChannelId().orElse(null))) {
					LOGGER.info("Unmuting " + member.getUsername());
					return member.edit(spec -> spec.setMute(false));
				}
			}
			return Mono.empty();
		});
	}
}
