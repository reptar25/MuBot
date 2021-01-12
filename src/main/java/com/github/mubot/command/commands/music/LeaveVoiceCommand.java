package com.github.mubot.command.commands.music;

import static com.github.mubot.command.util.PermissionsHelper.requireSameVoiceChannel;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.Command;
import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.voice.VoiceConnection;
import reactor.core.publisher.Mono;

public class LeaveVoiceCommand extends Command {

	// private static final Logger LOGGER =
	// Loggers.getLogger(LeaveVoiceCommand.class);

	public LeaveVoiceCommand() {
		super("leave", Arrays.asList("quit", "q", "l"));
	};

	@Override
	public Mono<CommandResponse> execute(MessageCreateEvent event, String[] args) {
		return requireSameVoiceChannel(event).flatMap(channel -> leave(channel));
	}

	/**
	 * Bot leaves any voice channel it is connected to in the same guild. Also
	 * clears the queue of items.
	 * 
	 * @param event The message event
	 * @return null
	 */
	public Mono<CommandResponse> leave(VoiceChannel channel) {
		return channel.getVoiceConnection().flatMap(VoiceConnection::disconnect).then(CommandResponse.empty());
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Requests the bot to leave its' current voice channel.");
	}
}
