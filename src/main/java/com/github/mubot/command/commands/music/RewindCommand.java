package com.github.mubot.command.commands.music;

import java.util.Arrays;
import java.util.function.Consumer;

import com.github.mubot.command.CommandResponse;
import com.github.mubot.command.help.CommandHelpSpec;
import com.github.mubot.music.TrackScheduler;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.VoiceChannel;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

public class RewindCommand extends MusicCommand {

	public RewindCommand() {
		super("rewind", Arrays.asList("rw"));
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return rewind(event, args, scheduler);
	}

	/**
	 * @param event
	 * @param event The message event
	 * @param args  The amount of time in seconds to rewind
	 * @return null
	 */
	public Mono<CommandResponse> rewind(MessageCreateEvent event, @NonNull String[] args,
			@NonNull TrackScheduler scheduler) {
		if (args.length > 0) {
			try {
				int amountInSeconds = Integer.parseInt(args[0]);
				scheduler.rewind(amountInSeconds);
				return Mono.empty();
			} catch (NumberFormatException e) {
				// just ignore commands with improper number
				return Mono.empty();
			}
		}
		return getHelp(event);
	}

	@Override
	public Consumer<? super CommandHelpSpec> createHelpSpec() {
		return spec -> spec.setDescription("Rewinds the currently playing song by the given amount of seconds.")
				.addArg("time", "amount of time in seconds to rewind", false).addExample("60");
	}
}