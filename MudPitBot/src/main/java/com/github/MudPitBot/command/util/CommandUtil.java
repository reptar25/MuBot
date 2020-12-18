package com.github.MudPitBot.command.util;

import java.util.Optional;

import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.exceptions.BotPermissionException;
import com.github.MudPitBot.command.exceptions.CommandException;
import com.github.MudPitBot.command.exceptions.SendMessagesException;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public final class CommandUtil {

	/**
	 * Sends a reply to either the channel the command was sent in or in a private
	 * message to the user who sent the command if the bot isn't allowed to speak in
	 * the channel the command was used in
	 * 
	 * @param event    the event to send a reply
	 * @param response the command response of the reply
	 * @return the command response
	 */
	public static Mono<CommandResponse> sendReply(MessageCreateEvent event, CommandResponse response) {
		// send reply, on CommandException error send the message to the member in a
		// private message
		return sendReply(event.getMember(), event.getMessage().getChannel(), response);
	}

	private static Mono<CommandResponse> sendReply(Optional<Member> memberOpt, Mono<MessageChannel> channelMono,
			CommandResponse response) {
		return channelMono.flatMap(channel -> {
			Mono<PermissionSet> permissions = Mono.just(PermissionSet.all());
			if (!(channel instanceof PrivateChannel))
				permissions = requireBotPermissions((GuildChannel) channel, Permission.SEND_MESSAGES);

			return permissions.flatMap(ignored -> {
				if (response.getSpec() != null) {
					return channel.createMessage(response.getSpec()).flatMap(message -> {
						// if the response contained a poll
						if (response.getPoll() != null) {
							// add reactions as vote tickers, number of reactions depends on number of
							// answers
							response.getPoll().addReactions(message);
						} else if (response.getPaginator() != null) {
							response.getPaginator().addReactions(message);
						}
						return Mono.just(response);
					});
				}
				return Mono.empty();
			});
		}).onErrorResume(SendMessagesException.class, ignored -> {
			return Mono.justOrEmpty(memberOpt).flatMap(member -> {
				return sendPrivateReply(member.getPrivateChannel(), response);
			});
		});
	}

	private static Mono<CommandResponse> sendPrivateReply(Mono<PrivateChannel> privateChannelMono,
			CommandResponse response) {
		return privateChannelMono.flatMap(privateChannel -> {
			return privateChannel.createMessage(response.getSpec()).thenReturn(response);
		});
	}

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

		return Mono.justOrEmpty(event.getMember())
				.switchIfEmpty(Mono.error(new CommandException("Voice command in private message",
						"You can't use this command in a private message")))
				.then(Mono.zip(getBotVoiceChannelId, getUserVoiceChannelId).map(tuple -> {
					final Optional<Snowflake> botVoiceChannelId = tuple.getT1();
					final Optional<Snowflake> userVoiceChannelId = tuple.getT2();

					// If the user and the bot are not in a voice channel
					if (botVoiceChannelId.isEmpty() || userVoiceChannelId.isEmpty()) {
						throw new CommandException("User not in voice channel",
								"You have to be in the a voice channel as the bot to use this command");
					}

					// If the user and the bot are not in the same voice channel
					if (botVoiceChannelId.isPresent()
							&& !userVoiceChannelId.map(botVoiceChannelId.get()::equals).orElse(false)) {
						throw new CommandException("User and bot not in same channel",
								"You have to be in the same voice channel as the bot to use this command");
					}

					return userVoiceChannelId.get();
				})).flatMap(event.getClient()::getChannelById).cast(VoiceChannel.class);
	}

	/**
	 * 
	 * @param channel              the guild channel
	 * @param requestedPermissions the permission the bot will need
	 * @return the permissions the bot has in this channel or an error if the bot
	 *         does not have the requested permissions
	 */
	public static Mono<PermissionSet> requireBotPermissions(GuildChannel channel, Permission... requestedPermissions) {
		return channel.getEffectivePermissions(channel.getClient().getSelfId()).flatMap(permissions -> {
			for (Permission permission : requestedPermissions) {
				if (!permissions.contains(permission)) {
					StringBuilder sb = new StringBuilder("I don't have permission to");
					RuntimeException exception = new BotPermissionException(
							new StringBuilder("Bot missing ").append(permission).append(" for command").toString());
					switch (permission) {
					case CONNECT:
						((BotPermissionException) exception)
								.setUserFriendlyMessage(sb.append(" connect to ").append(channel.getName()).toString());
						break;
					case SPEAK:
						((BotPermissionException) exception)
								.setUserFriendlyMessage(sb.append(" speak in ").append(channel.getName()).toString());
						break;
					case VIEW_CHANNEL:
						((BotPermissionException) exception)
								.setUserFriendlyMessage(sb.append(" view ").append(channel.getName()).toString());
						break;
					case MUTE_MEMBERS:
						((BotPermissionException) exception).setUserFriendlyMessage(
								sb.append(" mute members in ").append(channel.getName()).toString());
						break;
					case SEND_MESSAGES:
						exception = new SendMessagesException(
								new StringBuilder("Bot missing ").append(permission).append(" for command").toString());
						break;
					default:
						return Mono.error(new BotPermissionException(
								sb.append(" do that in ").append(channel.getName()).toString()));
					}

					return Mono.error(exception);
				}
			}
			return Mono.just(permissions);
		});
	}

}
