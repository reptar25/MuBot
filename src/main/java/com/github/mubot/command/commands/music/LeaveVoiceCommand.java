package com.github.mubot.command.commands.music;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

public class LeaveVoiceCommand extends MusicCommand {

	// private static final Logger LOGGER =
	// Loggers.getLogger(LeaveVoiceCommand.class);

	public LeaveVoiceCommand() {
		super("leave", Arrays.asList("quit", "q", "l"));
	};

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return leave(channel);
	}

	/**
	 * Bot leaves any voice channel it is connected to in the same guild. Also
	 * clears the queue of items.
	 * 
	 * @param channel the channel to leave
	 * @return
	 */
	public Mono<CommandResponse> leave(VoiceChannel channel) {
		return channel.getVoiceConnection().flatMap(VoiceConnection::disconnect).then(CommandResponse.empty());
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Requests the bot to leave its' current voice channel.");
	}

}
