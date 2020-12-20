package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.CommandUtil.requireBotPermissions;
import static com.github.MudPitBot.command.CommandUtil.requireVoiceChannel;

import java.time.Duration;
import java.util.Optional;

import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.exceptions.CommandException;
import com.github.MudPitBot.music.GuildMusicManager;
import com.github.MudPitBot.music.LavaPlayerAudioProvider;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import discord4j.voice.VoiceConnection;
import discord4j.voice.VoiceConnection.State;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class JoinVoiceCommand extends Command {

	private static final Logger LOGGER = Loggers.getLogger(JoinVoiceCommand.class);

	public JoinVoiceCommand() {
		super("join");
	};

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireVoiceChannel(event)
				.flatMap(channel -> requireBotPermissions(channel, Permission.CONNECT, Permission.VIEW_CHANNEL)
						.flatMap(ignored -> join(channel)));
	}

	/**
	 * Bot joins the same voice channel as the user who uses the command.
	 * 
	 * @param event The message event
	 * @return null
	 */
	public Mono<CommandResponse> join(VoiceChannel channel) {

		final Mono<Snowflake> getBotVoiceChannelId = channel.getClient().getSelf()
				.flatMap(user -> user.asMember(channel.getGuildId())).flatMap(Member::getVoiceState)
				.map(VoiceState::getChannelId).defaultIfEmpty(Optional.empty()).flatMap(s -> Mono.justOrEmpty(s))
				.flatMap(channel.getClient()::getChannelById).cast(VoiceChannel.class).map(VoiceChannel::getId)
				.defaultIfEmpty(Snowflake.of(-1l));

		Mono<Void> disconnect = getBotVoiceChannelId.filter(channelId -> !channelId.equals(channel.getId()))
				.flatMap(botVoiceChannelId -> channel.getGuild()).flatMap(Guild::getVoiceConnection)
				.flatMap(botVoiceConnection -> botVoiceConnection.disconnect());

		Mono<VoiceConnection> joinChannel = getBotVoiceChannelId.flatMap(id -> {
			if (id.equals(channel.getId()))
				return Mono.error(new CommandException("Bot already connected to channel",
						"I'm already connected your voice channel"));

			return Mono.just(channel.getId());
		}).flatMap(channelId -> {

			if (!GuildMusicManager.containsTrackScheduler(channel.getGuildId().asLong()))
				GuildMusicManager.createTrackScheduler(channel.getGuildId().asLong());

			return channel
					.join(spec -> spec.setProvider(new LavaPlayerAudioProvider(
							GuildMusicManager.getScheduler(channel.getGuildId().asLong()).getPlayer())))
					.doOnNext(vc -> {
						// subscribe to connected/disconnected events
						vc.onConnectOrDisconnect().subscribe(newState -> {
							if (newState.equals(State.CONNECTED)) {
								LOGGER.info("Bot connected to channel with id " + channelId.asLong());
							} else if (newState.equals(State.DISCONNECTED)) {
								// remove the scheduler from the map.
								// This doesn't ever seem to happen when the bot
								// disconnects, though, so also remove it from map
								// during leave command
								GuildMusicManager.removeFromMap(channel.getGuildId().asLong());
								LOGGER.info("Bot disconnected from channel with id " + channelId.asLong());
							}
						});
					}).delaySubscription(Duration.ofMillis(1));// allow disconnect first if already connected delay to
		});

		return disconnect.then(joinChannel).thenReturn(CommandResponse.emptyResponse());
	}

}
