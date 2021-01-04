package com.github.MudPitBot.command.commands.music;

import static com.github.MudPitBot.command.CommandUtil.requireBotPermissions;
import static com.github.MudPitBot.command.CommandUtil.requireVoiceChannel;

import java.time.Duration;
import com.github.MudPitBot.command.Command;
import com.github.MudPitBot.command.CommandResponse;
import com.github.MudPitBot.command.exceptions.CommandException;
import com.github.MudPitBot.music.GuildMusicManager;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

public class JoinVoiceCommand extends Command {

	// private static final Logger LOGGER =
	// Loggers.getLogger(JoinVoiceCommand.class);

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

		/*
		 * Work around for disconnect when moving channels. Disconnect from any channel
		 * first and then connect to new channel. Should be able to just join new
		 * channel but D4J is bugged.
		 */
		final Mono<Snowflake> checkSameVoiceChannel = channel.getClient().getSelf()
				.flatMap(user -> user.asMember(channel.getGuildId())).flatMap(Member::getVoiceState)
				.flatMap(vs -> Mono.justOrEmpty(vs.getChannelId())).defaultIfEmpty(Snowflake.of(0L))
				.filter(channelId -> !channelId.equals(channel.getId()))
				.switchIfEmpty(Mono.error(new CommandException("Bot already connected to channel",
						"I'm already connected to your voice channel")));

		Mono<Void> disconnect = channel.getGuild().flatMap(Guild::getVoiceConnection)
				.flatMap(VoiceConnection::disconnect);

		Mono<VoiceConnection> joinChannel = GuildMusicManager.getOrCreate(channel.getGuildId())
				.flatMap(guildMusic -> channel.join(spec -> spec.setProvider(guildMusic.getAudioProvider())))
				.delaySubscription(Duration.ofMillis(1));// delay to allow disconnect first if already connected

		return checkSameVoiceChannel.then(disconnect).then(joinChannel).thenReturn(CommandResponse.emptyFlat());
	}

}
