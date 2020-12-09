package com.github.MudPitBot.command.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
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

	private void setupMuteListener() {
		/*
		 * Add listener for members joining/changing voice channels.
		 */
		if (client.getEventDispatcher() != null) {
			client.getEventDispatcher().on(VoiceStateUpdateEvent.class).subscribe(event -> {
				if (event.isJoinEvent() || event.isMoveEvent() || event.isLeaveEvent()) {
					if (MuteHelper.mutedChannels.containsKey(event.getCurrent().getGuildId())) {
						// Checks whether a member should be muted on joining a voice channel and mutes
						// them if so
						muteOnJoin(event);
					}
				}
			});
		}

	}

	/**
	 * Checks if the new voice channel is the channel the bot currently has muted,
	 * and mutes the member if it is
	 * 
	 * @param event event of the channel change
	 */
	private void muteOnJoin(VoiceStateUpdateEvent event) {
		Mono.justOrEmpty(event.getCurrent()).map(VoiceState::getChannelId)
				.filter(currentChannelId -> currentChannelId.isPresent()).flatMap(currentChannelId -> {
					// join/switch channels
					return Mono.just(event.getCurrent()).flatMap(VoiceState::getMember)
							.filter(Predicate.not(Member::isBot)).flatMap(member -> {
								// if user join same channel as mute channel
								if (MuteHelper.mutedChannels.containsKey(event.getCurrent().getGuildId())) {
									if (MuteHelper.mutedChannels.get(event.getCurrent().getGuildId())
											.contains(currentChannelId.get())) {
										// mute the member that just joined
										muteUser(event);
									} else {
										unmuteUser(event);
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
									unmuteUser(event);
								}

								return Mono.empty();
							});
				}).subscribe(null, error -> LOGGER.error(error.getMessage(), error));

	}

	/**
	 * Mutes the user of the given VoiceStateUpdateEvent
	 * 
	 * @param event the VoiceStateUpdateEvent of the user
	 */
	private void muteUser(VoiceStateUpdateEvent event) {
		Mono.just(event.getCurrent()).flatMap(VoiceState::getMember).flatMap(member -> {
			member.edit(spec -> spec.setMute(true)).subscribe(null, error -> LOGGER.error(error.getMessage()));
			LOGGER.info("Muting " + member.getUsername());
			return Mono.empty();
		}).subscribe(null, error -> LOGGER.error(error.getMessage()));
	}

	/**
	 * Unmutes the user of the given VoiceStateUpdateEvent
	 * 
	 * @param event the VoiceStateUpdateEvent of the user
	 */
	private void unmuteUser(VoiceStateUpdateEvent event) {
		Mono.just(event.getCurrent()).flatMap(VoiceState::getMember).flatMap(member -> {
			return Mono.just(event.getCurrent()).flatMap(VoiceState::getGuild).flatMap(guild -> {
				return Mono.just(member).flatMap(Member::getVoiceState).flatMap(vs -> {
					if (vs.isMuted()) {
						if (!vs.getChannelId().orElse(null).equals(guild.getAfkChannelId().orElse(null))) {
							member.edit(spec -> spec.setMute(false)).subscribe(null,
									error -> LOGGER.error(error.getMessage()));
							LOGGER.info("Unmuting " + member.getUsername());
						}
					}

					return Mono.empty();
				});
			});
		}).subscribe(null, error -> LOGGER.error(error.getMessage(), error));
	}

}
