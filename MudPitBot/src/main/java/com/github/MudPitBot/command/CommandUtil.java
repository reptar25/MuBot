package com.github.MudPitBot.command;

import java.time.Duration;
import java.util.Optional;

import com.github.MudPitBot.command.exceptions.BotPermissionException;
import com.github.MudPitBot.command.exceptions.CommandException;
import com.github.MudPitBot.command.exceptions.SendMessagesException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

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
import reactor.core.publisher.Flux;
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
			Mono<PermissionSet> permissions = channel instanceof PrivateChannel ? Mono.just(PermissionSet.all())
					: requireBotPermissions((GuildChannel) channel, Permission.SEND_MESSAGES);

			return permissions.flatMap(ignored -> {
				if (response.getSpec() != null) {
					return channel.createMessage(response.getSpec()).flatMap(message -> {
						// if the response contains a menu
						if (response.getMenu() != null) {
							// set message on the menu
							response.getMenu().setMessage(message);
						}
						return Mono.just(response);
					});
				}
				return Mono.empty();
			});
		}).onErrorResume(SendMessagesException.class, ignored -> Mono.justOrEmpty(memberOpt)
				.flatMap(member -> sendPrivateReply(member.getPrivateChannel(), response)));
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

		return Mono.justOrEmpty(event.getMember())
				.switchIfEmpty(Mono.error(new CommandException("Voice command in private message",
						"You can't use this command in a private message")))
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

	/**
	 * Convert milliseconds to hours:minutes:seconds format
	 * 
	 * @param millis the time in milliseconds
	 * @return a String of the millisecond time in hours:minutes:seconds
	 */
	public static String convertMillisToTime(long millis) {
		StringBuilder sb = new StringBuilder();
		Duration duration = Duration.ofMillis(millis);
		String minuteFormat = "%d";
		String secondFormat = "%d";

		if (duration.toHoursPart() > 0) {
			sb.append(String.format("%d", duration.toHoursPart())).append(":");
			minuteFormat = "%02d";
		}

		if (duration.toMinutesPart() > 0) {
			sb.append(String.format(minuteFormat, duration.toMinutesPart())).append(":");
			secondFormat = "%02d";
		}

		if (duration.toSecondsPart() > 0) {
			sb.append(String.format(secondFormat, duration.toSecondsPart()));
		}

		return sb.toString();
	}

	public static String trackInfoString(AudioTrack track) {
		return "**" + track.getInfo().title + "** by " + track.getInfo().author;
	}

}
