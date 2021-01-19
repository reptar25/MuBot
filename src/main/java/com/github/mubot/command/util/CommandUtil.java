package com.github.mubot.command.util;

import static com.github.mubot.command.util.PermissionsHelper.requireBotPermissions;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.exceptions.SendMessagesException;
import com.github.mubot.database.DatabaseManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;

public final class CommandUtil {

	private static Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

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
				exception -> Mono.justOrEmpty(memberOpt)
						.flatMap(member -> sendPrivateReply(exception, member.getPrivateChannel(), response)))
				.thenReturn(response);
	}

	private static Mono<Void> sendPrivateReply(SendMessagesException exception, Mono<PrivateChannel> privateChannelMono,
			CommandResponse response) {
		return privateChannelMono.flatMap(privateChannel -> {
			// no menus in private messages
			if (response.getMenu() == null)
				return privateChannel.createMessage(response.getSpec()).then();
			else
				return privateChannel
						.createMessage(EmojiHelper.NO_ENTRY + " " + exception.getMessage() + " " + EmojiHelper.NO_ENTRY)
						.then();
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

		if (duration.toMinutesPart() >= 0) {
			sb.append(String.format(minuteFormat, duration.toMinutesPart())).append(":");
			secondFormat = "%02d";
		}

		if (duration.toSecondsPart() >= 0) {
			sb.append(String.format(secondFormat, duration.toSecondsPart()));
			secondFormat = "%02d";
		}

		return sb.toString();
	}

	public static String trackInfo(AudioTrack track) {
		return "**" + track.getInfo().title + "** by " + track.getInfo().author;
	}

	public static String trackCurrentTime(AudioTrack track) {
		return "[" + convertMillisToTime(track.getPosition()) + "/" + convertMillisToTime(track.getDuration()) + "]";
	}

	public static String trackInfoWithCurrentTime(AudioTrack track) {
		return trackInfo(track) + " " + trackCurrentTime(track);
	}

	public static String getGuildPrefixFromEvent(MessageCreateEvent event) {
		return getGuildPrefixFromId(event.getGuildId().orElse(Snowflake.of(0)).asLong());
	}

	public static String getGuildPrefixFromId(long guildId) {
		return escapeSpecialRegexChars(DatabaseManager.getPrefixCollection().getPrefix(guildId));
	}

	public static String escapeSpecialRegexChars(String str) {

		return SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0");
	}

}
