package com.github.MudPitBot.command.misc;

import java.util.ArrayList;
import java.util.HashMap;

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
	 * A list of channel ids of channels that should be muted
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
		client.getEventDispatcher().on(VoiceStateUpdateEvent.class).subscribe(event -> {
			if (event.isJoinEvent() || event.isMoveEvent() || event.isLeaveEvent()) {
				Snowflake oldId = null;
				if (event.getOld().isPresent()) {
					oldId = event.getOld().get().getChannelId().orElse(null);
				}
				Snowflake newId = event.getCurrent().getChannelId().orElse(null);
				if (MuteHelper.mutedChannels.containsKey(event.getCurrent().getGuildId())) {
					ArrayList<Snowflake> channelIds = MuteHelper.mutedChannels.get(event.getCurrent().getGuildId());
					// if we are joining or leaving a muted channel
					if (channelIds.contains(newId) || channelIds.contains(oldId)) {
						// Checks whether a member should be muted on joining a voice channel and mutes
						// them if so
						muteOnJoin(event);
					}
				}
			}
		});
	}

	/**
	 * Checks if the new voice channel is the channel the bot currently has muted,
	 * and mutes the member if it is
	 * 
	 * @param event event of the channel change
	 */
	private void muteOnJoin(VoiceStateUpdateEvent event) {
		Snowflake newChannelId = event.getCurrent().getChannelId().orElse(null);
		// if its a bot who joined, do not execute any more code
		if (event.getCurrent().getMember().block().isBot()) {
			return;
		}

		// if the newly joined channel is null just stop
		if (event.getCurrent().getChannelId() == null) {
			return;
		}
		// if user join same channel as mute channel
		if (MuteHelper.mutedChannels.containsKey(event.getCurrent().getGuildId())) {
			if (newChannelId != null) {
				if (MuteHelper.mutedChannels.get(event.getCurrent().getGuildId()).contains(newChannelId)) {
					// mute the member that just joined
					muteUser(event);
				} else {
					unmuteUser(event);
				}
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
	}

	private void muteUser(VoiceStateUpdateEvent event) {
		Mono.just(event.getCurrent()).flatMap(VoiceState::getMember).subscribe(member -> {
			member.edit(spec -> spec.setMute(true)).block();
			LOGGER.info("Muting " + member.getUsername());
		});
	}

	private void unmuteUser(VoiceStateUpdateEvent event) {
		Mono.just(event.getCurrent()).flatMap(VoiceState::getMember).subscribe(member -> {
			Mono.just(member).flatMap(Member::getVoiceState).subscribe(vs -> {
				if (vs.isMuted()) {
					if (!vs.getChannelId().get()
							.equals(event.getCurrent().getGuild().block().getAfkChannelId().orElse(null))) {
						member.edit(spec -> spec.setMute(false)).block();
						LOGGER.info("Unmuting " + member.getUsername());
					}
				}
			});
		});
	}

}
