package com.github.mubot.command.util;

import java.util.Optional;

import com.github.mubot.command.exceptions.BotPermissionException;
import com.github.mubot.command.exceptions.CommandException;
import com.github.mubot.command.exceptions.SendMessagesException;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class PermissionsHelper {

	/**
	 * Returns the voice channel the message sender is in or empty if they are not
	 * in a voice channel
	 * 
	 * @param event the message event
	 * @return the voice channel of the message sender
	 */
	public static Mono<VoiceChannel> requireVoiceChannel(MessageCreateEvent event) {
		return requireNotPrivateMessage(event).flatMap(Member::getVoiceState).map(VoiceState::getChannelId)
				.switchIfEmpty(Mono.error(new CommandException("Voice command used without voice channel",
						"You have to be in a voice channel to use this command")))
				.flatMap(Mono::justOrEmpty).flatMap(event.getClient()::getChannelById).cast(VoiceChannel.class);
	}

	/**
	 * 
	 * @param event the message event
	 * @return the voice channel the bot and message sender share or empty if they
	 *         do not share a voice channel
	 */
	private final static int MAX_RETRIES = 25000;

	public static Mono<VoiceChannel> requireSameVoiceChannel(MessageCreateEvent event) {
		// id of the bot's voice channel id or empty
		final Mono<Optional<Snowflake>> getBotVoiceChannelId = event.getClient().getSelf().flatMap(user -> {
			if (event.getGuildId().isPresent())
				return user.asMember(event.getGuildId().get());
			else
				return Mono.empty();
		}).flatMap(Member::getVoiceState).map(VoiceState::getChannelId).defaultIfEmpty(Optional.empty());

		// id of the user's voice channel id or empty
		final Mono<Optional<Snowflake>> getUserVoiceChannelId = Mono.justOrEmpty(event.getMember())
				.flatMap(Member::getVoiceState).map(VoiceState::getChannelId).defaultIfEmpty(Optional.empty());

		return checkSameVoiceChannel(event, getBotVoiceChannelId, getUserVoiceChannelId);
	}

	private static Mono<VoiceChannel> checkSameVoiceChannel(MessageCreateEvent event,
			Mono<Optional<Snowflake>> getBotVoiceChannelId, Mono<Optional<Snowflake>> getUserVoiceChannelId) {
		return requireNotPrivateMessage(event)
				.then(Mono.zip(getBotVoiceChannelId, getUserVoiceChannelId).flatMap(tuple -> {
					final Optional<Snowflake> botVoiceChannelId = tuple.getT1();
					final Optional<Snowflake> userVoiceChannelId = tuple.getT2();

					// If the user is not in a voice channel throw an error
					if (userVoiceChannelId.isEmpty()) {
						throw new CommandException("Voice command used without voice channel",
								"You have to be in a voice channel to use this command");
					}

					// if the bot is not in a voice channel or If the user and the bot are not in
					// the same voice channel, give it a chance to join before erring
					else if (botVoiceChannelId.isEmpty()
							|| !userVoiceChannelId.map(botVoiceChannelId.get()::equals).orElse(false)) {
						return Mono.empty();
					}

					return Mono.just(userVoiceChannelId.get());
					// retries allow the bot to connect to channel before throwing error, such as
					// "!join !play"
				}).repeatWhenEmpty(MAX_RETRIES, Flux::repeat)).onErrorResume(IllegalStateException.class, error -> {
					return Mono.error(new CommandException("User and bot not in same channel",
							"You have to be in the same voice channel as the bot to use this command"));
				}).flatMap(event.getClient()::getChannelById).cast(VoiceChannel.class);
	}

	/**
	 * Checks if the message was sent in a private message or not
	 * 
	 * @param event the MessageCreateEvent
	 * @return returns the Member if not a private message, or errors if it is
	 */
	public static Mono<Member> requireNotPrivateMessage(MessageCreateEvent event) {
		return Mono.justOrEmpty(event.getMember())
				.switchIfEmpty(Mono.error(new CommandException("Voice command in private message",
						"You can't use this command in a private message")));
	}

	/**
	 * 
	 * @param channel              the guild channel
	 * @param requestedPermissions the permissions the bot will need
	 * @return the permissions the bot has in this channel or an error if the bot
	 *         does not have the requested permissions
	 */

	public static Mono<PermissionSet> requireBotChannelPermissions(GuildChannel channel,
			Permission... requestedPermissions) {
		return channel.getEffectivePermissions(channel.getClient().getSelfId()).flatMap(permissions -> {
			return checkChannelPermissions(true, channel.getName(), permissions, requestedPermissions);
		});
	}

	/**
	 * 
	 * @param channel              the guild channel
	 * @param memberId             the Snowflake id of the member
	 * @param requestedPermissions the permissions the bot will need
	 * @return the permissions the member has in this channel or an error if the
	 *         member does not have the requested permissions
	 */

	public static Mono<PermissionSet> requireMemberChannelPermissions(GuildChannel channel, Snowflake memberId,
			Permission... requestedPermissions) {
		return channel.getEffectivePermissions(memberId).flatMap(permissions -> {
			return checkChannelPermissions(false, channel.getName(), permissions, requestedPermissions);
		});
	}

	/**
	 * Checks the given permissions set against the requested permission
	 * 
	 * @param channelName          the name of the channel permissions are being
	 *                             checked for
	 * @param permissions          the set of permissions that the user has for the
	 *                             given channel
	 * @param requestedPermissions the permissions the user has requested for this
	 *                             channel
	 * @return the PermissionSet of the user for this channel, or error if the user
	 *         doesn't have one of the requested permissions for this channel
	 */

	private static Mono<PermissionSet> checkChannelPermissions(boolean bot, String channelName,
			PermissionSet permissions, Permission... requestedPermissions) {
		for (Permission permission : requestedPermissions) {
			if (!permissions.contains(permission)) {
				RuntimeException exception;
				String exceptionMessage = getChannelPermissionErrorMessage(bot, permission, channelName);
				if (permission.equals(Permission.SEND_MESSAGES)) {
					exception = new SendMessagesException(exceptionMessage);
				} else {
					exception = new BotPermissionException(exceptionMessage);
				}
				return Mono.error(exception);
			}
		}
		return Mono.just(permissions);
	}

	public static Mono<PermissionSet> requireBotGuildPermissions(MessageCreateEvent event,
			Permission... requestedPermissions) {
		return event.getClient().getMemberById(event.getGuildId().get(), event.getClient().getSelfId())
				.flatMap(member -> requireGuildPermissions(member, requestedPermissions));
	}

	public static Mono<PermissionSet> requireUserGuildPermissions(MessageCreateEvent event,
			Permission... requestedPermissions) {
		return event.getMessage().getAuthorAsMember()
				.flatMap(member -> requireGuildPermissions(member, requestedPermissions));
	}

	public static Mono<PermissionSet> requireGuildPermissions(Member m, Permission... requestedPermissions) {
		return m.getBasePermissions().flatMap(memberPermissions -> {
			for (Permission permission : requestedPermissions) {
				if (!memberPermissions.contains(permission)) {
					RuntimeException exception;
					String exceptionMessage = getGuildPermissionErrorMessage(m.isBot(), permission);
					if (permission.equals(Permission.SEND_MESSAGES)) {
						exception = new SendMessagesException(exceptionMessage);
					} else {
						exception = new BotPermissionException(exceptionMessage);
					}
					return Mono.error(exception);
				}
			}
			return Mono.just(memberPermissions);
		});

	}

	public static String getGuildPermissionErrorMessage(boolean bot, Permission permission) {
		StringBuilder sb;
		if (bot)
			sb = new StringBuilder("I don't have permission to ");
		else
			sb = new StringBuilder("You don't have permission to ");

		sb.append(permission.toString());

		return sb.toString();
	}

	public static String getChannelPermissionErrorMessage(boolean bot, Permission permission, String channelName) {
		StringBuilder sb;
		if (bot)
			sb = new StringBuilder("I don't have permission to ");
		else
			sb = new StringBuilder("You don't have permission to ");

		sb.append(permission.toString());

		sb.append(" in " + channelName);
		return sb.toString();
	}
}