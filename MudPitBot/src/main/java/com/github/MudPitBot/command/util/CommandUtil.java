package com.github.MudPitBot.command.util;

import java.time.Duration;
import java.util.Optional;

import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.exceptions.SendMessagesException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;
import static com.github.MudPitBot.command.util.Permissions.requireBotPermissions;

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
		if (response.getSpec() == null)
			return CommandResponse.empty();
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
			}).then();
		}).onErrorResume(SendMessagesException.class,
				ignored -> Mono.justOrEmpty(memberOpt)
						.flatMap(member -> sendPrivateReply(member.getPrivateChannel(), response)))
				.thenReturn(response);
	}

	private static Mono<Void> sendPrivateReply(Mono<PrivateChannel> privateChannelMono, CommandResponse response) {
		return privateChannelMono.flatMap(privateChannel -> {
			return privateChannel.createMessage(response.getSpec()).flatMap(message -> {
				// if the response contains a menu
				if (response.getMenu() != null) {
					// set message on the menu
					response.getMenu().setMessage(message);
				}
				return Mono.just(response);
			}).then();
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
