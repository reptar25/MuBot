package com.github.MudPitBot.command.util;

import java.util.Optional;

import com.github.MudPitBot.command.exceptions.BotPermissionException;
import com.github.MudPitBot.command.exceptions.CommandException;
import com.github.MudPitBot.command.exceptions.SendMessagesException;

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

public final class Permissions {

	/**
	 * Returns the voice channel the message sender is in or empty if they are not
	 * in a voice channel
	 * 
	 * @param event the message event
	 * @return the voice channel of the message sender
	 */
	public static Mono<VoiceChannel> requireVoiceChannel(MessageCreateEvent event) {
		return Mono.justOrEmpty(event.getMember())
				.switchIfEmpty(Mono.error(new CommandException("Voice command in private message",
						"You can't use this command in a private message")))
				.flatMap(Member::getVoiceState).map(VoiceState::getChannelId)
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

		return requireNotPrivateMessage(event)
				.then(Mono.zip(getBotVoiceChannelId, getUserVoiceChannelId).flatMap(tuple -> {
					final Optional<Snowflake> botVoiceChannelId = tuple.getT1();
					final Optional<Snowflake> userVoiceChannelId = tuple.getT2();

					// If the user is not in a voice channel throw an error
					if (userVoiceChannelId.isEmpty()) {
						throw new CommandException("Voice command used without voice channel",
								"You have to be in a voice channel to use this command");
					}

					// if the bot is not in a voice channel give a chance to join before erring
					if (botVoiceChannelId.isEmpty()) {
						return Mono.empty();
					}

					// If the user and the bot are not in the same voice channel
					if (!userVoiceChannelId.map(botVoiceChannelId.get()::equals).orElse(false)) {
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

	public static Mono<PermissionSet> requireBotPermissions(GuildChannel channel, Permission... requestedPermissions) {
		return channel.getEffectivePermissions(channel.getClient().getSelfId()).flatMap(permissions -> {
			return checkPermissions(channel.getName(), permissions, requestedPermissions);
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

	public static Mono<PermissionSet> requireMemberPermissions(GuildChannel channel, Snowflake memberId,
			Permission... requestedPermissions) {
		return channel.getEffectivePermissions(memberId).flatMap(permissions -> {
			return checkPermissions(channel.getName(), permissions, requestedPermissions);
		});
	}

	/**
	 * Checks the given permissions set agains the requested permission
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

	private static Mono<PermissionSet> checkPermissions(String channelName, PermissionSet permissions,
			Permission... requestedPermissions) {
		for (Permission permission : requestedPermissions) {
			if (!permissions.contains(permission)) {
				StringBuilder sb = new StringBuilder("I don't have permission to");
				RuntimeException exception = new BotPermissionException(
						new StringBuilder("Bot missing ").append(permission).append(" for command").toString());
				switch (permission) {
				case CONNECT:
					((BotPermissionException) exception)
							.setUserFriendlyMessage(sb.append(" connect to ").append(channelName).toString());
					break;
				case SPEAK:
					((BotPermissionException) exception)
							.setUserFriendlyMessage(sb.append(" speak in ").append(channelName).toString());
					break;
				case VIEW_CHANNEL:
					((BotPermissionException) exception)
							.setUserFriendlyMessage(sb.append(" view ").append(channelName).toString());
					break;
				case MUTE_MEMBERS:
					((BotPermissionException) exception)
							.setUserFriendlyMessage(sb.append(" mute members in ").append(channelName).toString());
					break;
				case SEND_MESSAGES:
					exception = new SendMessagesException(
							new StringBuilder("Bot missing ").append(permission).append(" for command").toString());
					break;
				case ADD_REACTIONS:
					((BotPermissionException) exception).setUserFriendlyMessage(
							sb.append(" add message reactions in ").append(channelName).toString());
					break;
				case MANAGE_MESSAGES:
					((BotPermissionException) exception)
							.setUserFriendlyMessage(sb.append(" manage messages in ").append(channelName).toString());
					break;
				case ADMINISTRATOR:
				case ATTACH_FILES:
				case BAN_MEMBERS:
				case CHANGE_NICKNAME:
				case CREATE_INSTANT_INVITE:
				case DEAFEN_MEMBERS:
				case EMBED_LINKS:
				case KICK_MEMBERS:
				case MANAGE_CHANNELS:
				case MANAGE_EMOJIS:
				case MANAGE_GUILD:
				case MANAGE_NICKNAMES:
				case MANAGE_ROLES:
				case MANAGE_WEBHOOKS:
				case MENTION_EVERYONE:
				case MOVE_MEMBERS:
				case PRIORITY_SPEAKER:
				case READ_MESSAGE_HISTORY:
				case SEND_TTS_MESSAGES:
				case STREAM:
				case USE_EXTERNAL_EMOJIS:
				case USE_VAD:
				case VIEW_AUDIT_LOG:
				case VIEW_GUILD_INSIGHTS:
				default:
					return Mono.error(
							new BotPermissionException(sb.append(" do that in ").append(channelName).toString()));
				}
				return Mono.error(exception);
			}
		}
		return Mono.just(permissions);
	}
}