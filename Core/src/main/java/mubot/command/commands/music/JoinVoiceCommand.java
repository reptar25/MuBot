package mubot.command.commands.music;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.Permission;
import discord4j.voice.VoiceConnection;
import mubot.command.CommandResponse;
import mubot.command.exceptions.CommandException;
import mubot.command.help.CommandHelpSpec;
import mubot.music.GuildMusicManager;
import mubot.music.TrackScheduler;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Consumer;

import static mubot.command.util.PermissionsHelper.requireBotChannelPermissions;
import static mubot.command.util.PermissionsHelper.requireVoiceChannel;

public class JoinVoiceCommand extends MusicCommand {

	public JoinVoiceCommand() {
		super("join", Collections.singletonList("j"));
	}

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireVoiceChannel(event)
				.flatMap(channel -> requireBotChannelPermissions(channel, Permission.CONNECT, Permission.VIEW_CHANNEL)
						.flatMap(ignored -> action(event, args, null, channel)));
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return join(channel);
	}

	/**
	 * Bot joins the same voice channel as the user who uses the command.
	 * 
	 * @param channel the channel to join
	 * @return the response to joining
	 */
	public Mono<CommandResponse> join(VoiceChannel channel) {

		/*
		 * Work around for disconnect when moving channels. Disconnect from any channel
		 * first and then connect to new channel. Should be able to just join new
		 * channel but D4J 3.1.x is bugged. Fixed in 3.2.x
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

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec
				.setDescription("Requests the bot to join the same voice channel as user who used the command.");
	}

}
