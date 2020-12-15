package com.github.MudPitBot.command;

import java.util.Optional;

import com.github.MudPitBot.sound.TrackScheduler;

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

/**
 * Implementation of the Command design pattern.
 */

public abstract class Command implements CommandInterface {

	protected String commandTrigger;

	public Command(String commandTrigger) {
		this.commandTrigger = commandTrigger;
	}

	/**
	 * The String that would cause this command to trigger if typed in a message to
	 * a channel the bot can see
	 * 
	 * @return the literal String of what triggers this command.
	 */
	public String getCommandTrigger() {
		return commandTrigger;
	}

	/**
	 * Gets the {@link TrackScheduler} that was mapped when the bot joined a voice
	 * channel of the guild the message was sent in.
	 * 
	 * @param event The message event
	 * @return The {@link TrackScheduler} that is mapped to the voice channel of the
	 *         bot in the guild the message was sent from.
	 */
	private static final int RETRY_AMOUNT = 100;

	protected static Mono<TrackScheduler> getScheduler(VoiceChannel channel) {
		return Mono.justOrEmpty(TrackScheduler.getScheduler(channel.getId().asLong())).repeatWhenEmpty(RETRY_AMOUNT,
				Flux::repeat);
	}

	/**
	 * Returns the voice channel the message sender is in or empty if they are not
	 * in a voice channel
	 * 
	 * @param event the message event
	 * @return the voice channel of the message sender
	 */
	protected static Mono<VoiceChannel> requireVoiceChannel(MessageCreateEvent event) {
		return Mono.justOrEmpty(event.getMember())
				.switchIfEmpty(Mono.error(new CommandException("You can't use this command in a private message")))
				.flatMap(Member::getVoiceState).map(VoiceState::getChannelId).defaultIfEmpty(Optional.empty())
				.flatMap(s -> {
					if (s.isPresent())
						return Mono.just(s.get());

					return Mono.error(new CommandException("You have to be in a voice channel to use this command"));
				}).flatMap(event.getClient()::getChannelById).cast(VoiceChannel.class);
	}

	/**
	 * 
	 * @param event the message event
	 * @return the voice channel the bot and message sender share or empty if they
	 *         do not share a voice channel
	 */
	protected static Mono<VoiceChannel> requireSameVoiceChannel(MessageCreateEvent event) {
		Mono<MessageChannel> getMessageChannel = event.getMessage().getChannel();
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

		return Mono.zip(getBotVoiceChannelId, getUserVoiceChannelId, getMessageChannel).map(tuple -> {
			final Optional<Snowflake> botVoiceChannelId = tuple.getT1();
			final Optional<Snowflake> userVoiceChannelId = tuple.getT2();
			final MessageChannel channel = tuple.getT3();

			if (channel instanceof PrivateChannel) {
				throw new CommandException("You can't use this command in a private message");
			}

			// If the user and the bot are not in a voice channel
			if (botVoiceChannelId.isEmpty() || userVoiceChannelId.isEmpty()) {
				throw new CommandException("You have to be in the same voice channel as the bot to use this command");
			}

			// If the user and the bot are not in the same voice channel
			if (botVoiceChannelId.isPresent()
					&& !userVoiceChannelId.map(botVoiceChannelId.get()::equals).orElse(false)) {
				throw new CommandException("You have to be in the same voice channel as the bot to use this command");
			}

			return userVoiceChannelId.get();
		}).flatMap(event.getClient()::getChannelById).cast(VoiceChannel.class);
	}

	/**
	 * 
	 * @param channel              the voice channel
	 * @param requestedPermissions the permission the bot will need
	 * @return the permissions the bot has in this channel or an error if the bot
	 *         does not have the requested permissions
	 */
	protected static Mono<PermissionSet> requireBotPermissions(GuildChannel channel,
			Permission... requestedPermissions) {
		return channel.getEffectivePermissions(channel.getClient().getSelfId()).flatMap(permissions -> {
			for (Permission permission : requestedPermissions) {
				if (!permissions.contains(permission)) {
					StringBuilder sb = new StringBuilder("I don't have permission to ");
					boolean sendMessageError = false;
					switch (permission) {
					case CONNECT:
						sb.append("connect to");
						break;
					case SPEAK:
						sb.append("speak in");
						break;
					case VIEW_CHANNEL:
						sb.append("view");
						break;
					case MUTE_MEMBERS:
						sb.append("mute members in");
						break;
					case SEND_MESSAGES:
						sb.append("send messages in");
						sendMessageError = true;
						break;
					default:
						sb.append("do that in");
						break;
					}
					sb.append(" ").append(channel.getName());
					return Mono.error(new CommandException(sb.toString(), sendMessageError));
				}
			}
			return Mono.just(permissions);
		});
	}

}
