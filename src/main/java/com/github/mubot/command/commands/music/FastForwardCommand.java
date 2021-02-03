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

public class FastForwardCommand extends MusicCommand {

	public FastForwardCommand() {
		super("fastforward", Arrays.asList("ff"));
	}

	@Override
	protected Mono<CommandResponse> action(MessageCreateEvent event, String[] args, TrackScheduler scheduler,
			VoiceChannel channel) {
		return fastForward(event, args, scheduler);
	}

	/**
	 * @param event
	 * @param event The message event
	 * @param args  The amount of time in seconds to fast forward
	 * @return null
	 */
	public Mono<CommandResponse> fastForward(MessageCreateEvent event, @NonNull String[] args,
			@NonNull TrackScheduler scheduler) {
		if (args.length > 0) {
			try {
				int amountInSeconds = Integer.parseInt(args[0]);
				scheduler.fastForward(amountInSeconds);
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
		return spec -> spec.setDescription("Fast fowards the currently playing song by the given amount of seconds.")
				.addArg("time", "amount of time in seconds to fast foward", false).addExample("60");
	}

}
